package com.rabbiter.em.entity.dto;

import java.math.BigDecimal;

public class GoodDTO {
    private Long id;
    private String name;
    private String imgs;
    private BigDecimal price;
    private Integer status;
    private Integer totalStore;
    private Boolean soldOut;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotalStore() {
        return totalStore;
    }

    public void setTotalStore(Integer totalStore) {
        this.totalStore = totalStore;
    }

    public Boolean getSoldOut() {
        return soldOut;
    }

    public void setSoldOut(Boolean soldOut) {
        this.soldOut = soldOut;
    }

    @Override
    public String toString() {
        return "GoodDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imgs='" + imgs + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", totalStore=" + totalStore +
                ", soldOut=" + soldOut +
                '}';
    }
}
