package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.BaseContext;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Picture;
import com.example.mapper.PictureMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 照片信息业务处理
 **/
@Service
public class PictureService {

    @Resource
    private PictureMapper pictureMapper;

    /**
     * 新增
     */
    public void add(Picture picture) {
        picture.setTime(DateUtil.now());
        picture.setStatusRadio("审核通过");
        pictureMapper.insert(picture);
    }

    public void deleteByCategoryId(Integer categoryId) {
        pictureMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        pictureMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            pictureMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Picture picture) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            picture.setStatusRadio("审核通过");
        }
        pictureMapper.updateById(picture);
    }

    /**
     * 根据ID查询
     */
    public Picture selectById(Integer id) {
        return pictureMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Picture> selectAll(Picture picture) {
        List<Picture> pictures = pictureMapper.selectAll(picture);
        if (ObjectUtil.isNull(pictures)) return null;
        for(Picture x : pictures) {
            if(RoleEnum.USER.name().equals(TokenUtils.getCurrentUser().getRole()) && "私有".equals(x.getRoleRadio()) && !x.getUserId().equals(BaseContext.getCurrentId())) {
                pictures.remove(x);
            }
        }
        return pictures;
        // return pictureMapper.selectAll(picture);
    }

    /**
     * 分页查询
     */
    public PageInfo<Picture> selectPage(Picture picture, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Picture> list = this.selectAll(picture);
        return PageInfo.of(list);
    }

    public void deleteByUserId(Integer id) {
        pictureMapper.deleteByUserId(id);
    }
}