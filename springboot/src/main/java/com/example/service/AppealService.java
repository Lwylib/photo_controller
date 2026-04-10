package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Appeal;
import com.example.entity.Category;
import com.example.mapper.AppealMapper;
import com.example.mapper.CategoryMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 业务处理
 **/
@Service
public class AppealService {

    @Resource
    private AppealMapper appealMapper;
    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 新增
     */
    public void add(Appeal appeal) {
        appealMapper.insert(appeal);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        appealMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            appealMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Appeal appeal) {
        appealMapper.updateById(appeal);
        Category category = categoryMapper.selectById(appeal.getCategoryId());
        if (ObjectUtil.isNotEmpty(category)) {
            if ("审核通过".equals(appeal.getStatus())) {
                category.setStatusRadio("正常");
            }
            if ("审核拒绝".equals(appeal.getStatus())) {
                category.setStatusRadio("违规");
            }
            categoryMapper.updateById(category);
        }
    }

    /**
     * 根据ID查询
     */
    public Appeal selectById(Integer id) {
        return appealMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Appeal> selectAll(Appeal appeal) {
        return appealMapper.selectAll(appeal);
    }

    /**
     * 分页查询
     */
    public PageInfo<Appeal> selectPage(Appeal appeal, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            appeal.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Appeal> list = this.selectAll(appeal);
        return PageInfo.of(list);
    }

}