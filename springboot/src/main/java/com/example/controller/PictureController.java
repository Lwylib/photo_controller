package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.entity.Category;
import com.example.entity.Picture;
import com.example.mapper.CategoryMapper;
import com.example.service.CategoryService;
import com.example.service.PictureService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 照片信息前端操作接口
 **/
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Picture picture) {
        log.info("PictureController.add() - 新增照片, 标题: {}", picture.getDescription());
        pictureService.add(picture);
        log.info("PictureController.add() - 新增照片成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("PictureController.deleteById() - 删除照片, ID: {}", id);
        pictureService.deleteById(id);
        log.info("PictureController.deleteById() - 删除照片成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        log.info("PictureController.deleteBatch() - 批量删除照片, ID列表: {}", ids);
        pictureService.deleteBatch(ids);
        log.info("PictureController.deleteBatch() - 批量删除照片成功");
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Picture picture) {
        log.info("PictureController.updateById() - 更新照片, ID: {}, 标题: {}", picture.getId(), picture.getDescription());
        pictureService.updateById(picture);
        log.info("PictureController.updateById() - 更新照片成功");
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("PictureController.selectById() - 查询照片, ID: {}", id);
        Picture picture = pictureService.selectById(id);
        log.info("PictureController.selectById() - 查询照片成功");
        return Result.success(picture);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Picture picture) {
        log.info("PictureController.selectAll() - 查询所有照片");
        List<Picture> list = pictureService.selectAll(picture);
        log.info("PictureController.selectAll() - 查询到 {} 张照片", list.size());
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Picture picture,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("PictureController.selectPage() - 分页查询照片, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Picture> page = pictureService.selectPage(picture, pageNum, pageSize);
        log.info("PictureController.selectPage() - 分页查询成功, 总记录数: {}", page.getTotal());
        return Result.success(page);
    }

    @GetMapping("/bar")
    public Result bar() {
        List<String> xList = new ArrayList<>();
        List<Long> yList = new ArrayList<>();

        List<Category> list = categoryMapper.selectByHotPoint(null);

        for(Category x : list) {
            xList.add(x.getName());
            yList.add(Long.valueOf(x.getHotPoint()));
        }

        Map<String, Object> map = new HashMap<>();
        //
        // // 查询出所有图片
        // Picture picture = new Picture();
        // picture.setStatusRadio("审核通过");
        // List<Picture> pictures = pictureService.selectAll(picture);
        //
        // Map<String, Long> collect = pictures.stream()
        //         .filter(x -> ObjectUtil.isNotEmpty(x.getCategoryName()))
        //         .collect(Collectors.groupingBy(Picture::getCategoryName, ))
        //         .entrySet().stream()
        //         .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        //         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        // for (String categoryName : collect.keySet()) {
        //     xList.add(categoryName);
        //     yList.add(collect.get(categoryName));
        // }

        map.put("xList", xList);
        map.put("yList", yList);
        return Result.success(map);
    }

}