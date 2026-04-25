package com.example.controller;

import com.example.service.ExportService;
import jakarta.annotation.Resource;
// import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 数据导出控制器
 */
@RestController
@RequestMapping("/export")
@Slf4j
public class ExportController {

    @Resource
    private ExportService exportService;

    /**
     * 导出相册图片
     * @param categoryId 相册ID
     * @return ZIP文件下载响应
     */
    @GetMapping("/album/{categoryId}")
    public ResponseEntity<org.springframework.core.io.Resource> exportAlbumImages(@PathVariable Integer categoryId) {
        log.info("exportAlbumImages() - 导出相册图片, 相册ID: {}", categoryId);
        ResponseEntity<org.springframework.core.io.Resource> response = exportService.exportAlbumImages(categoryId);
        log.info("exportAlbumImages() - 导出相册图片成功, 相册ID: {}", categoryId);
        return response;
    }
}