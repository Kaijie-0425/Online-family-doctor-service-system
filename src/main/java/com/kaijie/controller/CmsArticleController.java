package com.kaijie.controller;

import com.kaijie.dto.ArticlePublishDTO;
import com.kaijie.entity.Article;
import com.kaijie.security.SecurityUtils;
import com.kaijie.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
public class CmsArticleController {

    @Autowired
    private IArticleService articleService;

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody ArticlePublishDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }
        try {
            String msg = articleService.publishArticle(username, dto);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(required = false) String category) {
        List<Article> list = articleService.getArticleList(category);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        try {
            Article article = articleService.getArticleDetail(id);
            return ResponseEntity.ok(article);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}

