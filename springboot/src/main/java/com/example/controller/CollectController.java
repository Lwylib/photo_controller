package com.example.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.example.common.Result;
import com.example.entity.Collect;
import com.example.exception.CustomException;
import com.example.service.CollectService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 收藏信息前端操作接口
 **/
@RestController
@RequestMapping("/collect")
@Slf4j
public class CollectController {

    @Resource
    private CollectService collectService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Collect collect) {
        log.info("add() - 新增收藏, 用户ID: {}, 相册ID: {}", collect.getUserId(), collect.getCategoryId());
        List<Collect> collects = collectService.selectAll(collect);
        if (CollectionUtil.isNotEmpty(collects)) {
            log.warn("add() - 重复收藏, 用户ID: {}, 相册ID: {}", collect.getUserId(), collect.getCategoryId());
            throw new CustomException("500", "您已经收藏过该相册，请勿重复收藏");
        }
        collectService.add(collect);
        log.info("add() - 新增收藏成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("deleteById() - 删除收藏, ID: {}", id);
        collectService.deleteById(id);
        log.info("deleteById() - 删除收藏成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        log.info("deleteBatch() - 批量删除收藏, ID列表: {}", ids);
        collectService.deleteBatch(ids);
        log.info("deleteBatch() - 批量删除收藏成功");
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Collect collect) {
        log.info("updateById() - 更新收藏, ID: {}, 用户ID: {}", collect.getId(), collect.getUserId());
        collectService.updateById(collect);
        log.info("updateById() - 更新收藏成功");
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("selectById() - 查询收藏, ID: {}", id);
        Collect collect = collectService.selectById(id);
        log.info("selectById() - 查询收藏成功");
        return Result.success(collect);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Collect collect) {
        log.info("selectAll() - 查询所有收藏");
        List<Collect> list = collectService.selectAll(collect);
        log.info("selectAll() - 查询到 {} 条收藏记录", list.size());
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Collect collect,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("selectPage() - 分页查询收藏, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
        log.info("selectPage() - 分页查询成功, 总记录数: {}", page.getTotal());
        return Result.success(page);
    }

}