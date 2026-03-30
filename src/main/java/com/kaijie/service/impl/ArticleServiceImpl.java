package com.kaijie.service.impl;

import com.kaijie.dto.ArticlePublishDTO;
import com.kaijie.entity.Article;
import com.kaijie.entity.User;
import com.kaijie.mapper.ArticleMapper;
import com.kaijie.mapper.UserMapper;
import com.kaijie.service.IArticleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 健康科普资讯文章表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
@Transactional
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String publishArticle(String username, ArticlePublishDTO dto) {
        if (username == null || dto == null) {
            throw new RuntimeException("参数错误");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || user.getId() == null) {
            throw new RuntimeException("用户不存在");
        }
        Integer roleType = null;
        if (user.getRoleType() != null) roleType = (int) user.getRoleType();
        if (roleType == null || !(roleType == 0 || roleType == 1)) {
            throw new RuntimeException("无权发布文章");
        }

        Article article = new Article();
        article.setAuthorId(user.getId());
        article.setTitle(dto.getTitle());
        article.setCategory(dto.getCategory());
        article.setCoverImage(dto.getCoverImage());
        article.setContent(dto.getContent());
        article.setPublishStatus((byte)1);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCreateTime(LocalDateTime.now());

        boolean ok = this.save(article);
        if (!ok) throw new RuntimeException("文章发布失败");
        return "文章发布成功";
    }

    @Override
    public List<Article> getArticleList(String category) {
        QueryWrapper<Article> q = new QueryWrapper<>();
        q.eq("publish_status", 1);
        // is_deleted handled by TableLogic, but include explicit check for safety
        q.eq("is_deleted", 0);
        if (category != null && !category.trim().isEmpty()) {
            q.eq("category", category.trim());
        }
        q.orderByDesc("create_time");
        return this.baseMapper.selectList(q);
    }

    @Override
    public Article getArticleDetail(Long id) {
        if (id == null) throw new RuntimeException("参数错误");
        Article article = this.baseMapper.selectById(id);
        if (article == null) throw new RuntimeException("文章不存在");

        Integer vc = article.getViewCount();
        if (vc == null) vc = 0;
        article.setViewCount(vc + 1);
        article.setUpdateTime(LocalDateTime.now());
        this.updateById(article);

        return article;
    }
}
