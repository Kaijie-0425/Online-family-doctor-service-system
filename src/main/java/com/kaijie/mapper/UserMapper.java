package com.kaijie.mapper;

import com.kaijie.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 系统核心用户表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
