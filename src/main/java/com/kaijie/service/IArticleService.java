package com.kaijie.service;

import com.kaijie.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 健康科普资讯文章表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
public interface IArticleService extends IService<Article> {

    // 发布文章
    String publishArticle(String username, com.kaijie.dto.ArticlePublishDTO dto);

    // 获取文章列表（可按分类筛选）
    List<Article> getArticleList(String category);

    // 获取文章详情并增加浏览量
    Article getArticleDetail(Long id);

}
