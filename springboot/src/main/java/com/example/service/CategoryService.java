package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Category;
import com.example.entity.Collect;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.CategoryMapper;
import com.example.mapper.CollectMapper;
import com.example.mapper.PictureMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 相册信息业务处理
 **/
@Service
public class CategoryService {
    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private CollectMapper collectMapper;
    @Resource
    private UserMapper userMapper;


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
        pictureMapper.deleteByCategoryId(id);
        categoryMapper.deleteById(id);
    }

    public void deleteByUserId(Integer id) {
        pictureMapper.deleteByUserId(id);
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
        if("私有".equals(category.getRoleRadio())) {
            collectMapper.deleteByCategoryId(category.getId());
        }
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
        User user = userMapper.selectById(category.getUserId());
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
            User user = userMapper.selectById(dbCategory.getUserId());
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
            User user = userMapper.selectById(dbCategory.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                dbCategory.setUserName(user.getName());
                dbCategory.setUserAvatar(user.getAvatar());
            }
            // 计算热度值
            // if(new Random().nextInt(0,100) < 10) {
                calculateHotPoint(dbCategory);
            // }
        }
        // Collections.shuffle(list);
        return PageInfo.of(list);
    }

    /**
     * 热度相册分页查询
     */
    public PageInfo<Category> selectHotAlbumPage(Category category, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> list = categoryMapper.selectByHotPoint(category);
        for (Category dbCategory : list) {
            User user = userMapper.selectById(dbCategory.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                dbCategory.setUserName(user.getName());
                dbCategory.setUserAvatar(user.getAvatar());
            }
            // 计算热度值
            calculateHotPoint(dbCategory);
        }
        return PageInfo.of(list);
    }

    /**
     * 计算相册热度值
     */
    private void calculateHotPoint(Category category) {
        // 获取相册的收藏数
        Collect collectParam = new Collect();
        collectParam.setCategoryId(category.getId());
        List<Collect> collects = collectMapper.selectAll(collectParam);
        int collectCount = collects.size();
        
        // 设置默认浏览量和收藏数
        if (category.getViewCount() == null) {
            category.setViewCount(0);
        }
        category.setCollectCount(collectCount);
        
        // 热度计算公式：热度 = 浏览量 * 0.3 + 收藏数 * 0.7
        double hotPoint = category.getViewCount() * 0.3 + collectCount * 0.7;
        category.setHotPoint((int) hotPoint);
        
        // 更新热度值到数据库
        categoryMapper.updateById(category);
    }

    /**
     * 增加相册浏览量
     */
    public void increaseViewCount(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category != null) {
            if (category.getViewCount() == null) {
                category.setViewCount(1);
            } else {
                category.setViewCount(category.getViewCount() + 1);
            }
            categoryMapper.updateById(category);
        }
    }

}