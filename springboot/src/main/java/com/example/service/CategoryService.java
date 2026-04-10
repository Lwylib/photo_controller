package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Category;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.CategoryMapper;
import com.example.mapper.PictureMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 相册信息业务处理
 **/
@Service
public class CategoryService {
    @Resource
    private PictureService pictureService;

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private UserService userService;

    /**
     * 新增
     */
    public void add(Category category) {
        category.setTime(DateUtil.now());
        category.setStatusRadio("正常");
        categoryMapper.insert(category);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        pictureService.deleteByCategoryId(id);
        categoryMapper.deleteById(id);
    }

    public void deleteByUserId(Integer id) {
        pictureService.deleteByUserId(id);
        categoryMapper.deleteByUserId(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Category category) {
        categoryMapper.updateById(category);
    }

    /**
     * 根据ID查询
     */
    public Category selectById(Integer id) {
        Category category = categoryMapper.selectById(id);
        if ("违规".equals(category.getStatusRadio())) {
            throw new CustomException("500", "该相册已违规，暂时无法查看内容");
        }
        User user = userService.selectById(category.getUserId());
        if (ObjectUtil.isNotEmpty(user)) {
            category.setUserName(user.getName());
            category.setUserAvatar(user.getAvatar());
        }
        return category;
    }

    /**
     * 查询所有
     */
    public List<Category> selectAll(Category category) {
        return categoryMapper.selectAll(category);
    }

    /**
     * 分页查询
     */
    public PageInfo<Category> selectPage(Category category, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            category.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Category> list = this.selectAll(category);
        for (Category dbCategory : list) {
            User user = userService.selectById(dbCategory.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                dbCategory.setUserName(user.getName());
                dbCategory.setUserAvatar(user.getAvatar());
            }
        }
        return PageInfo.of(list);
    }

    /**
     * 相册广场分页查询
     */
    public PageInfo<Category> selectAlbumPage(Category category, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> list = this.selectAll(category);
        for (Category dbCategory : list) {
            User user = userService.selectById(dbCategory.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                dbCategory.setUserName(user.getName());
                dbCategory.setUserAvatar(user.getAvatar());
            }
        }
        Collections.shuffle(list);
        return PageInfo.of(list);
    }


}