package com.rabbiter.em.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_file")
public class MyFile {
    @TableId(type = IdType.AUTO)
    private Integer id;  // 使用包装类Integer而非primitive类型int
    private String name;
    private String type;
    private Long size;  // 使用包装类Long
    private String url;
    @TableField("is_delete")
    private Boolean isDelete;  // 使用包装类Boolean
    private Boolean enable;  // 使用包装类Boolean
    private String md5;

    public MyFile() {
    }

    public MyFile(Integer id, String name, String type, Long size, String url, Boolean isDelete, Boolean enable, String md5) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.url = url;
        this.isDelete = isDelete;
        this.enable = enable;
        this.md5 = md5;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean delete) {
        isDelete = delete;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "MyFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", isDelete=" + isDelete +
                ", enable=" + enable +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
