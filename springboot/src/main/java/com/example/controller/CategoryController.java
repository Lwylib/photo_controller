package com.example.controller;

import com.example.common.Result;
import com.example.entity.Category;
import com.example.service.CategoryService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 相册信息前端操作接口
 **/
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Category category) {
        log.info("add() - 新增相册, 名称: {}", category.getName());
        categoryService.add(category);
        log.info("add() - 新增相册成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("deleteById() - 删除相册, ID: {}", id);
        categoryService.deleteById(id);
        log.info("deleteById() - 删除相册成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        log.info("deleteBatch() - 批量删除相册, ID列表: {}", ids);
        categoryService.deleteBatch(ids);
        log.info("deleteBatch() - 批量删除相册成功");
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Category category) {
        log.info("updateById() - 更新相册, ID: {}, 名称: {}", category.getId(), category.getName());
        categoryService.updateById(category);
        log.info("updateById() - 更新相册成功");
        return Result.success();
    }

    /**
     * 更新相册状态（正常/违规）
     */
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody Category category) {
        log.info("updateStatus() - 更新相册状态, ID: {}, 状态: {}", category.getId(), category.getStatusRadio());
        Category dbCategory = categoryService.selectById(category.getId());
        dbCategory.setStatusRadio(category.getStatusRadio());
        categoryService.updateById(dbCategory);
        log.info("updateStatus() - 更新相册状态成功");
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("selectById() - 查询相册, ID: {}", id);
        Category category = categoryService.selectById(id);
        log.info("selectById() - 查询相册成功");
        return Result.success(category);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Category category) {
        log.info("selectAll() - 查询所有相册");
        List<Category> list = categoryService.selectAll(category);
        log.info("selectAll() - 查询到 {} 个相册", list.size());
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Category category,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("selectPage() - 分页查询相册, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Category> page = categoryService.selectPage(category, pageNum, pageSize);
        log.info("selectPage() - 分页查询成功, 总记录数: {}", page.getTotal());
        return Result.success(page);
    }

    /**
     * 相册广场分页查询
     */
    @GetMapping("/selectAlbumPage")
    public Result selectAlbumPage(Category category,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Category> page = categoryService.selectAlbumPage(category, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 热度相册分页查询
     */
    @GetMapping("/selectHotAlbumPage")
    public Result selectHotAlbumPage(Category category,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Category> page = categoryService.selectHotAlbumPage(category, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 增加相册浏览量
     */
    @PutMapping("/increaseViewCount/{id}")
    public Result increaseViewCount(@PathVariable Integer id) {
        categoryService.increaseViewCount(id);
        return Result.success();
    }

}