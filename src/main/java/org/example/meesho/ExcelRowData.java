package org.example.meesho;

import java.time.LocalDate;
import java.util.Date;

public class ExcelRowData {
    String name;
    LocalDate date ;
    String subOrderId ;
    String skuId ;
    String quantity ;
    String size ;

    public ExcelRowData(String name, LocalDate date, String subOrderId, String skuId, String quantity, String size) {
        this.name = name;
        this.date = date;
        this.subOrderId = subOrderId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.size = size;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSubOrderId() {
        return subOrderId;
    }

    public void setSubOrderId(String subOrderId) {
        this.subOrderId = subOrderId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
