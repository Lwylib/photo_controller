package com.example.entity;


/**
 * 照片信息
*/
public class Picture {
    /** 主键ID */
    private Integer id;
    /** 照片图片 */
    private String img;
    /** 照片名称 */
    private String name;
    /** 照片描述 */
    private String description;
    /** 用户IDId */
    private Integer userId;
    /** 相册IDId */
    private Integer categoryId;
    /** 上传时间 */
    private String time;
    /** 照片权限%公开,私有 */
    private String roleRadio;
    /** 照片状态%待审核,审核通过,审核拒绝 */
    private String statusRadio;
    private String userName;
    private String categoryName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoleRadio() {
        return roleRadio;
    }

    public void setRoleRadio(String roleRadio) {
        this.roleRadio = roleRadio;
    }

    public String getStatusRadio() {
        return statusRadio;
    }

    public void setStatusRadio(String statusRadio) {
        this.statusRadio = statusRadio;
    }

}