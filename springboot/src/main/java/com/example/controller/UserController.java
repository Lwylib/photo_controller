package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        log.info("add() - 新增用户, 用户名: {}", user.getUsername());
        userService.add(user);
        log.info("add() - 新增用户成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        log.info("delete() - 删除用户, ID: {}", id);
        userService.deleteById(id);
        log.info("delete() - 删除用户成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        log.info("delete() - 批量删除用户, ID列表: {}", ids);
        userService.deleteBatch(ids);
        log.info("delete() - 批量删除用户成功");
        return Result.success();
    }

    /**
     * 新增
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        log.info("update() - 更新用户, ID: {}, 用户名: {}", user.getId(), user.getUsername());
        userService.updateById(user);
        log.info("update() - 更新用户成功");
        return Result.success();
    }

    /**
     * 查询单个
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("selectById() - 查询用户, ID: {}", id);
        User user = userService.selectById(id);
        log.info("selectById() - 查询用户成功");
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        log.info("selectAll() - 查询所有用户");
        List<User> list = userService.selectAll(user);
        log.info("selectAll() - 查询到 {} 个用户", list.size());
        return Result.success(list);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectPage")
    public Result selectPage(
            User user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("selectPage() - 分页查询用户, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<User> pageInfo = userService.selectPage(user, pageNum, pageSize);
        log.info("selectPage() - 分页查询成功, 总记录数: {}", pageInfo.getTotal());
        return Result.success(pageInfo);
    }

}