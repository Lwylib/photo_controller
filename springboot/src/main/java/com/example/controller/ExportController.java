package com.example.controller;

import com.example.service.ExportService;
import jakarta.annotation.Resource;
// import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 数据导出控制器
 */
@RestController
@RequestMapping("/export")
public class ExportController {

    @Resource
    private ExportService exportService;

    /**
     * 导出相册图片
     * @param categoryId 相册ID
     * @return ZIP文件下载响应
     */
    @GetMapping("/album/{categoryId}")
    public ResponseEntity<Resource> exportAlbumImages(@PathVariable Integer categoryId) {
        return exportService.exportAlbumImages(categoryId);
    }
}