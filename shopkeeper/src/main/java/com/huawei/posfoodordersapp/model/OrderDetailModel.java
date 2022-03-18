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
import java.util.List;

public class OrderDetailModel implements Serializable {
    private int orderID;
    private String deviceID;
    private String deviceType;
    private String username;
    private int totalNoPeople;
    private int tableNumber;
    private List<FoodMenuItem> listOfOrderItems;
    private String status;
    private String command;
    private boolean isMenuAccepted = false;
    private boolean isOrderPlaced = false;
    private boolean isOrderAccepted = false;
    private boolean isOrderDelivered = false;
    private boolean isPaid = false;
    private int totalAmount;

    public OrderDetailModel() {
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getTotalNoPeople() {
        return totalNoPeople;
    }

    public void setTotalNoPeople(int totalNoPeople) {
        this.totalNoPeople = totalNoPeople;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public List<FoodMenuItem> getListOfOrderItems() {
        return listOfOrderItems;
    }

    public void setListOfOrderItems(List<FoodMenuItem> listOfOrderItems) {
        this.listOfOrderItems = listOfOrderItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMenuAccepted() {
        return isMenuAccepted;
    }

    public void setMenuAccepted(boolean menuAccepted) {
        isMenuAccepted = menuAccepted;
    }

    public boolean isOrderAccepted() {
        return isOrderAccepted;
    }

    public void setOrderAccepted(boolean orderAccepted) {
        isOrderAccepted = orderAccepted;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isOrderDelivered() {
        return isOrderDelivered;
    }

    public void setOrderDelivered(boolean orderDelivered) {
        isOrderDelivered = orderDelivered;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isOrderPlaced() {
        return isOrderPlaced;
    }

    public void setOrderPlaced(boolean orderPlaced) {
        isOrderPlaced = orderPlaced;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}
