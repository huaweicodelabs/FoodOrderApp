/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.posfoodordersapp.model;

import java.io.Serializable;

public class FoodMenuItem implements Serializable {
    private String name;
    private int prize;
    private int quantity;
    private int orderId = 0;
    private String isOrderConfirmed = "";
    private String isOrderDelievered = "";
    private String imageurl = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public int getPrize() {
        return prize;
    }

    public void setPrize(int prize) {
        this.prize = prize;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getIsOrderConfirmed() {
        return isOrderConfirmed;
    }

    public void setIsOrderConfirmed(String isOrderConfirmed) {
        this.isOrderConfirmed = isOrderConfirmed;
    }

    public String getIsOrderDelievered() {
        return isOrderDelievered;
    }

    public void setIsOrderDelievered(String isOrderDelievered) {
        this.isOrderDelievered = isOrderDelievered;
    }
}