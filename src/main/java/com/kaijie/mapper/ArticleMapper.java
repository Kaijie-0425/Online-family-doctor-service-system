package com.kaijie.mapper;

import com.kaijie.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 健康科普资讯文章表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

}
