package com.example.controller;

import com.example.common.Result;
import com.example.entity.Notice;
import com.example.service.NoticeService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 系统公告前端操作接口
 **/
@RestController
@RequestMapping("/notice")
@Slf4j
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Notice notice) {
        log.info("NoticeController.add() - 新增公告, 标题: {}", notice.getTitle());
        noticeService.add(notice);
        log.info("NoticeController.add() - 新增公告成功");
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("NoticeController.deleteById() - 删除公告, ID: {}", id);
        noticeService.deleteById(id);
        log.info("NoticeController.deleteById() - 删除公告成功");
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        log.info("NoticeController.deleteBatch() - 批量删除公告, ID列表: {}", ids);
        noticeService.deleteBatch(ids);
        log.info("NoticeController.deleteBatch() - 批量删除公告成功");
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Notice notice) {
        log.info("NoticeController.updateById() - 更新公告, ID: {}, 标题: {}", notice.getId(), notice.getTitle());
        noticeService.updateById(notice);
        log.info("NoticeController.updateById() - 更新公告成功");
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        log.info("NoticeController.selectById() - 查询公告, ID: {}", id);
        Notice notice = noticeService.selectById(id);
        log.info("NoticeController.selectById() - 查询公告成功");
        return Result.success(notice);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Notice notice) {
        log.info("NoticeController.selectAll() - 查询所有公告");
        List<Notice> list = noticeService.selectAll(notice);
        log.info("NoticeController.selectAll() - 查询到 {} 条公告", list.size());
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Notice notice,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("NoticeController.selectPage() - 分页查询公告, 页码: {}, 页大小: {}", pageNum, pageSize);
        PageInfo<Notice> page = noticeService.selectPage(notice, pageNum, pageSize);
        log.info("NoticeController.selectPage() - 分页查询成功, 总记录数: {}", page.getTotal());
        return Result.success(page);
    }

}