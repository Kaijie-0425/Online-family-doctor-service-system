package com.kaijie.service.impl;

import com.kaijie.entity.HealthVitalSigns;
import com.kaijie.mapper.HealthVitalSignsMapper;
import com.kaijie.service.IHealthVitalSignsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.kaijie.service.IUserService;
import com.kaijie.entity.User;
import com.kaijie.dto.SymptomAssessDTO;
import com.kaijie.dto.VitalSignRecordDTO;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.kaijie.exception.UnauthenticatedException;
import com.kaijie.exception.PermissionDeniedException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.math.BigDecimal;


/**
 * <p>
 * 居民体征数据表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-30
 */
@Service
public class HealthVitalSignsServiceImpl extends ServiceImpl<HealthVitalSignsMapper, HealthVitalSigns> implements IHealthVitalSignsService {
	@Autowired
	private IUserService userService;

	@Autowired(required = false)
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static final String SILICONFLOW_API_KEY = "sk-vdoxaybbvtytxugvfunyofvjlpcdejccknlajgwptbypezfr";
	private static final String SILICONFLOW_URL = "https://api.siliconflow.cn/v1/chat/completions";

	@Override
	public String assessSymptoms(String username, SymptomAssessDTO dto) {
		if (username == null) {
			throw new UnauthenticatedException("未登录，请先登录");
		}
		if (dto == null || dto.getSymptoms() == null || dto.getSymptoms().trim().isEmpty()) {
			return "请输入症状描述";
		}
		try {
			Map<String, Object> body = new HashMap<>();
			body.put("model", "deepseek-ai/DeepSeek-V3");
			Map<String, String> system = new HashMap<>();
			system.put("role", "system");
			system.put("content", "你是一名专业的全科家庭医生。请根据患者的症状描述给出初步的健康评估。请直接输出判断的紧急程度（【紧急】或【普通】）以及医疗建议。");
			Map<String, String> user = new HashMap<>();
			user.put("role", "user");
			user.put("content", dto.getSymptoms());
			body.put("messages", new Object[]{system, user});

			org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
			headers.add("Content-Type", "application/json");
			headers.add("Authorization", "Bearer " + SILICONFLOW_API_KEY); // <- 请在此替换为你的 API KEY

			org.springframework.http.HttpEntity<String> req = new org.springframework.http.HttpEntity<>(objectMapper.writeValueAsString(body), headers);

			org.springframework.http.ResponseEntity<String> resp = restTemplate.postForEntity(SILICONFLOW_URL, req, String.class);
			if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
				JsonNode root = objectMapper.readTree(resp.getBody());
				JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
				if (!contentNode.isMissingNode() && !contentNode.isNull()) {
					String content = contentNode.asText();
					if (content != null && !content.isEmpty()) return content;
				}
			}
		} catch (Exception ex) {
			// ignore and return fallback
		}
		return "AI 医生暂时离线，请直接预约在线问诊。";
	}

	@Override
	public String recordVitalSigns(String username, VitalSignRecordDTO dto) {
		if (username == null) throw new UnauthenticatedException("未登录，请先登录");
		User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
		if (user == null || user.getId() == null) throw new UnauthenticatedException("用户不存在或未登录");
		Integer roleType = null;
		if (user.getRoleType() != null) roleType = (int) user.getRoleType();
		if (roleType == null || roleType != 2) throw new PermissionDeniedException("只有居民用户可以录入体征");

		StringBuilder warn = new StringBuilder();
		if (dto.getSystolicBp() != null && dto.getSystolicBp() > 140) {
			if (warn.length() > 0) warn.append("; ");
			warn.append("血压偏高");
		}
		if (dto.getDiastolicBp() != null && dto.getDiastolicBp() > 90) {
			if (warn.length() > 0) warn.append("; ");
			warn.append("血压偏高");
		}
		if (dto.getBloodSugar() != null && dto.getBloodSugar() > 7.0) {
			if (warn.length() > 0) warn.append("; ");
			warn.append("血糖偏高");
		}
		if (dto.getHeartRate() != null && (dto.getHeartRate() < 60 || dto.getHeartRate() > 100)) {
			if (warn.length() > 0) warn.append("; ");
			warn.append("心率异常");
		}

		HealthVitalSigns record = new HealthVitalSigns();
		record.setPatientId(user.getId());
		record.setRecordDate(LocalDate.now());
		record.setSystolicBp(dto.getSystolicBp());
		record.setDiastolicBp(dto.getDiastolicBp());
		if (dto.getBloodSugar() != null) record.setBloodSugar(BigDecimal.valueOf(dto.getBloodSugar()));
		record.setHeartRate(dto.getHeartRate());
		record.setWarningMsg(warn.length() == 0 ? null : warn.toString());
		record.setCreateTime(LocalDateTime.now());

		boolean ok = this.save(record);
		if (!ok) throw new RuntimeException("体征保存失败");
		if (warn.length() == 0) return "体征指标正常";
		return "预警：" + warn;
	}

	@Override
	public List<HealthVitalSigns> getMyVitalSignsTrend(String username, Integer days) {
		if (days == null || days <= 0) days = 7;
		if (username == null) throw new UnauthenticatedException("未登录，请先登录");
		User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
		if (user == null || user.getId() == null) throw new UnauthenticatedException("用户不存在或未登录");
		LocalDate start = LocalDate.now().minusDays(days - 1);
		QueryWrapper<HealthVitalSigns> qw = new QueryWrapper<>();
		qw.eq("patient_id", user.getId()).ge("record_date", start).orderByAsc("record_date");
		return this.baseMapper.selectList(qw);
	}
}
