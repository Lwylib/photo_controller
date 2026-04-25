package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Resource
    AdminService adminService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        log.info("add() - 新增管理员, 用户名: {}", admin.getUsername());
        adminService.add(admin);
        log.info("add() - 新增管理员成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        log.info("delete() - 删除管理员, ID: {}", id);
        adminService.deleteById(id);
        log.info("delete() - 删除管理员成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        log.info("delete() - 批量删除管理员, ID列表: {}", ids);
        adminService.deleteBatch(ids);
        log.info("delete() - 批量删除管理员成功");
        return Result.success();
    }

    /**
     * 新增
     */
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin) {
        log.info("update() - 更新管理员, ID: {}, 用户名: {}", admin.getId(), admin.getUsername());
        adminService.updateById(admin);
        log.info("update() - 更新管理员成功");
        return Result.success();
    }

    /**
     * 查询单个
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("selectById() - 查询管理员, ID: {}", id);
        Admin admin = adminService.selectById(id);
        log.info("selectById() - 查询管理员成功");
        return Result.success(admin);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        log.info("selectAll() - 查询所有管理员");
        List<Admin> list = adminService.selectAll(admin);
        log.info("selectAll() - 查询到 {} 个管理员", list.size());
        return Result.success(list);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectPage")
    public Result selectPage(
            Admin admin,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("selectPage() - 分页查询管理员, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Admin> pageInfo = adminService.selectPage(admin, pageNum, pageSize);
        log.info("selectPage() - 分页查询成功, 总记录数: {}", pageInfo.getTotal());
        return Result.success(pageInfo);
    }

}