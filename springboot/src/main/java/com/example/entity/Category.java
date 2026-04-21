package com.example.entity;


/**
 * 相册信息
*/
public class Category {
    /** 主键ID */
    private Integer id;
    /** 相册封面 */
    private String img;
    /** 相册名称 */
    private String name;
    private String description;
    /** 用户IDId */
    private Integer userId;
    /** 状态%正常,违规 */
    private String statusRadio;
    /** 创建时间 */
    private String time;
    /** 相册权限%公开,私有 */
    private String roleRadio;
    private String userName;
    private String userAvatar;
    private Integer hotPoint;

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

    public String getStatusRadio() {
        return statusRadio;
    }

    public void setStatusRadio(String statusRadio) {
        this.statusRadio = statusRadio;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }


    public Integer getHotPoint() {
        return hotPoint;
    }

    public void setHotPoint(Integer hotPoint) {
        this.hotPoint = hotPoint;
    }
}