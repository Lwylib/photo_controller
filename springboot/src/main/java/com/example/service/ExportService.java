package com.example.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ZipUtil;
import com.example.common.BaseContext;
import com.example.common.enums.RoleEnum;
import com.example.entity.Picture;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
// import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据导出服务
 */
@Service
public class ExportService {

    @Resource
    private PictureService pictureService;

    /**
     * 导出相册图片为ZIP包
     * @param categoryId 相册ID
     * @return 包含ZIP文件的ResponseEntity
     */
    public ResponseEntity<org.springframework.core.io.Resource> exportAlbumImages(Integer categoryId) {
        try {
            // 查询相册中的所有照片
            Picture pictureParam = new Picture();
            pictureParam.setCategoryId(categoryId);
            List<Picture> allPictures = pictureService.selectAll(pictureParam);
            
            // 根据用户权限过滤可下载的照片
            // List<Picture> downloadablePictures = filterDownloadablePictures(allPictures);
            List<Picture> downloadablePictures = allPictures;

            if (downloadablePictures.isEmpty()) {
                throw new RuntimeException("没有可下载的照片");
            }
            
            // 创建临时目录
            String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "album_export_" + System.currentTimeMillis();
            FileUtil.mkdir(tempDir);
            
            // 复制照片到临时目录
            List<File> imageFiles = new ArrayList<>();
            for (Picture picture : downloadablePictures) {
                if (ObjectUtil.isNotEmpty(picture.getImg())) {
                    String fileName = picture.getImg().substring(picture.getImg().lastIndexOf('/') + 1);
                    String filePath = System.getProperty("user.dir") + "/files/" + fileName;
                    File sourceFile = new File(filePath);
                    if (sourceFile.exists()) {
                        File destFile = new File(tempDir + File.separator + fileName);
                        FileUtil.copy(sourceFile, destFile, true);
                        imageFiles.add(destFile);
                    }
                }
            }
            
            // 创建ZIP文件
            String zipFilePath = tempDir + ".zip";
            File zipFile = ZipUtil.zip(tempDir, zipFilePath);
            
            // 清理临时目录
            FileUtil.del(tempDir);
            
            // 准备下载响应
            org.springframework.core.io.Resource resource = new FileSystemResource(zipFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"album_" + categoryId + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipFile.length())
                    .body(resource);
            
        } catch (Exception e) {
            throw new RuntimeException("导出失败：" + e.getMessage(), e);
        }
    }

    // /**
    //  * 根据用户权限过滤可下载的照片
    //  * @param pictures 原始照片列表
    //  * @return 可下载的照片列表
    //  */
    // private List<Picture> filterDownloadablePictures(List<Picture> pictures) {
    //     List<Picture> result = new ArrayList<>();
    //
    //     for (Picture picture : pictures) {
    //         // 管理员可以下载所有照片
    //         if (RoleEnum.ADMIN.name().equals(TokenUtils.getCurrentUser().getRole())) {
    //             result.add(picture);
    //             continue;
    //         }
    //
    //         // 相册创建者可以下载所有照片
    //         if (picture.getUserId().equals(BaseContext.getCurrentId())) {
    //             result.add(picture);
    //             continue;
    //         }
    //
    //         // 其他用户只能下载公开且审核通过的照片
    //         if ("公开".equals(picture.getRoleRadio()) && "审核通过".equals(picture.getStatusRadio())) {
    //             result.add(picture);
    //         }
    //     }
    //
    //     return result;
    // }
}