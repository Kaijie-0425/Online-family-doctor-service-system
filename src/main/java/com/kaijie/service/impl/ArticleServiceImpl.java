package com.kaijie.service.impl;

import com.kaijie.entity.Article;
import com.kaijie.mapper.ArticleMapper;
import com.kaijie.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 健康科普资讯文章表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

}
