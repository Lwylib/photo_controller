package com.example.controller;

import com.example.common.Result;
import com.example.entity.Appeal;
import com.example.service.AppealService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/appeal")
public class AppealController {

    @Resource
    private AppealService appealService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Appeal appeal) {
        appealService.add(appeal);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        appealService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        appealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Appeal appeal) {
        appealService.updateById(appeal);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Appeal appeal = appealService.selectById(id);
        return Result.success(appeal);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Appeal appeal) {
        List<Appeal> list = appealService.selectAll(appeal);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Appeal appeal,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Appeal> page = appealService.selectPage(appeal, pageNum, pageSize);
        return Result.success(page);
    }

}