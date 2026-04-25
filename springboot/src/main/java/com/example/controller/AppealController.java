package com.example.controller;

import com.example.common.Result;
import com.example.entity.Appeal;
import com.example.service.AppealService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/appeal")
@Slf4j
public class AppealController {

    @Resource
    private AppealService appealService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Appeal appeal) {
        log.info("add() - 新增申诉, 申诉人: {}", appeal.getUserName());
        appealService.add(appeal);
        log.info("add() - 新增申诉成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("deleteById() - 删除申诉, ID: {}", id);
        appealService.deleteById(id);
        log.info("deleteById() - 删除申诉成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        log.info("deleteBatch() - 批量删除申诉, ID列表: {}", ids);
        appealService.deleteBatch(ids);
        log.info("deleteBatch() - 批量删除申诉成功");
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Appeal appeal) {
        log.info("updateById() - 更新申诉, ID: {}, 申诉人: {}", appeal.getId(), appeal.getUserName());
        appealService.updateById(appeal);
        log.info("updateById() - 更新申诉成功");
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("selectById() - 查询申诉, ID: {}", id);
        Appeal appeal = appealService.selectById(id);
        log.info("selectById() - 查询申诉成功");
        return Result.success(appeal);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Appeal appeal) {
        log.info("selectAll() - 查询所有申诉");
        List<Appeal> list = appealService.selectAll(appeal);
        log.info("selectAll() - 查询到 {} 条申诉", list.size());
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Appeal appeal,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("selectPage() - 分页查询申诉, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Appeal> page = appealService.selectPage(appeal, pageNum, pageSize);
        log.info("selectPage() - 分页查询成功, 总记录数: {}", page.getTotal());
        return Result.success(page);
    }

}