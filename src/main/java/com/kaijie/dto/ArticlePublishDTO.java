package com.kaijie.dto;

import lombok.Data;

@Data
public class ArticlePublishDTO {
    private String title;
    private String category;
    private String coverImage;//封面图片url
    private String content;
}

