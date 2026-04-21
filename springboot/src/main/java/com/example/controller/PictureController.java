package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.entity.Category;
import com.example.entity.Picture;
import com.example.mapper.CategoryMapper;
import com.example.service.CategoryService;
import com.example.service.PictureService;
import com.github.pagehelper.PageInfo;
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
        pictureService.add(picture);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        pictureService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        pictureService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Picture picture) {
        pictureService.updateById(picture);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Picture picture = pictureService.selectById(id);
        return Result.success(picture);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Picture picture) {
        List<Picture> list = pictureService.selectAll(picture);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Picture picture,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Picture> page = pictureService.selectPage(picture, pageNum, pageSize);
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