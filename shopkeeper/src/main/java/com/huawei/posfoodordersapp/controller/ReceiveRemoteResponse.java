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

package com.huawei.posfoodordersapp.controller;

import com.huawei.posfoodordersapp.MainAbility;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.utils.Constants;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;

import ohos.rpc.RemoteObject;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.MessageOption;
import static com.huawei.posfoodordersapp.utils.Constants.DASHBOARD_SLICE;

import java.util.ArrayList;



public class ReceiveRemoteResponse extends RemoteObject implements IRemoteBroker {
    private Ability ability;
    private final int REMOTE_COMMAND = 0;
    private DistributedNotificationPlugin distributedNotificationPlugin;

    public ReceiveRemoteResponse(Ability ability) {
        super("Audio Player Remote");
        this.ability = ability;
        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
    }

    @Override
    public IRemoteObject asObject() {
        return this;
    }

    /* onRemoteRequest to used to get data from remote device
     * */
    @Override
    public boolean onRemoteRequest(int code, MessageParcel data, MessageParcel reply, MessageOption option) {
        if (code == REMOTE_COMMAND) {
            String command = data.readString();
            String status = data.readString();
            String customerDeviceID = data.readString();
            LogUtil.debug(LogUtil.TAG_LOG, "onRemoteRequest in shopkeeper - begin");
            OrderDetailModel orderDetailModel = (OrderDetailModel) data.readValue();

            if (Constants.getInstance().getOrderDetailsList() != null
                    && Constants.getInstance().getOrderDetailsList().size() >= 1) {
                LogUtil.debug("TAG", "Adding order into existing orderlist");
                updateDuplicateList(orderDetailModel);
                distributedNotificationPlugin.publishEvent(command, orderDetailModel.getOrderID());
            } else {
                ArrayList<OrderDetailModel> orderList = new ArrayList<>();
                orderList.add(orderDetailModel);
                LogUtil.debug("TAG", "Adding order into new orderlist");
                Constants.getInstance().setOrderDetailsList(orderList);
                LogUtil.debug("TAG", "Request_command for menu card");
                openOrderDashboardAbilitySlice(customerDeviceID);
            }

            LogUtil.debug("TAG", "Response_command:list-> " + orderDetailModel.getUsername() + orderDetailModel.getTotalNoPeople());

        } else if (code == 1) {

        } else if (code == 2) {

        }

        return false;
    }

    private void updateDuplicateList(OrderDetailModel orderDetailModel) {
        ArrayList<OrderDetailModel> orderDetailListTemp = Constants.getInstance().getOrderDetailsList();
        int totalCount = orderDetailListTemp.size();
        boolean isExisting = false;
        ArrayList<FoodMenuItem> tempOldOrderList = new ArrayList<>();
        if (totalCount == 1) {
            if (orderDetailListTemp.get(0).getListOfOrderItems() != null) {
                for (int j = 0; j < orderDetailListTemp.get(0).getListOfOrderItems().size(); j++) {
                    if (orderDetailListTemp.get(0).getOrderID() != 0
                            && orderDetailListTemp.get(0).getOrderID() == orderDetailModel.getOrderID()) {
                        tempOldOrderList.add(orderDetailListTemp.get(0).getListOfOrderItems().get(j));
                        LogUtil.debug(LogUtil.TAG_LOG, "OLD Order food name -" + tempOldOrderList.get(j).getName() + " Qty:" +
                                tempOldOrderList.get(j).getQuantity());
                    }
                }
            }
        } else if (totalCount >= 1) {
            for (int m = 0; m < totalCount; m++) {
                if (orderDetailListTemp.get(m).getListOfOrderItems() != null) {
                    for (int j = 0; j < orderDetailListTemp.get(m).getListOfOrderItems().size(); j++) {
                        if (orderDetailListTemp.get(m).getOrderID() != 0
                                && orderDetailListTemp.get(m).getOrderID() == orderDetailModel.getOrderID()) {
                            tempOldOrderList.add(orderDetailListTemp.get(m).getListOfOrderItems().get(j));
                            LogUtil.debug(LogUtil.TAG_LOG, "OLD Order food name -" + tempOldOrderList.get(j).getName() + " Qty:" +
                                    tempOldOrderList.get(j).getQuantity());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < totalCount; i++) {
            if (orderDetailListTemp.get(i).getOrderID() != 0
                    && orderDetailListTemp.get(i).getOrderID() == orderDetailModel.getOrderID()) {
                isExisting = true;
                LogUtil.debug(LogUtil.TAG_LOG, "Order ID is existing: updating order id -" + orderDetailModel.getOrderID());
                for (int k = 0; k < orderDetailModel.getListOfOrderItems().size(); k++) {
                    if (orderDetailModel.getListOfOrderItems().get(k).getIsOrderConfirmed().contains("false") &&
                            orderDetailModel.getListOfOrderItems().get(k).getIsOrderDelievered().contains("false")) {
                        tempOldOrderList.add(orderDetailModel.getListOfOrderItems().get(k));
                        LogUtil.debug(LogUtil.TAG_LOG, "NEW Order food name -" + tempOldOrderList.get(k).getName() + " Qty:" +
                                tempOldOrderList.get(k).getQuantity());
                    }
                }
                orderDetailModel.setListOfOrderItems(tempOldOrderList);
                LogUtil.debug(LogUtil.TAG_LOG, "Count order-" + tempOldOrderList.size());
                orderDetailListTemp.set(i, orderDetailModel);
                Constants.getInstance().getOrderDetailsList().set(i, orderDetailModel);
            }
            if (i == totalCount - 1) {
                if (!isExisting) {
                    LogUtil.debug(LogUtil.TAG_LOG, "Order ID is not existing: adding order id -" + orderDetailModel.getOrderID());
                    Constants.getInstance().getOrderDetailsList().add(orderDetailModel);
                }
            }
        }
    }

    /* openAbility to used to get navigate respective ability
     * */
    private void openOrderDashboardAbilitySlice(String customerDeviceID) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(ability.getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(DASHBOARD_SLICE)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", customerDeviceID);
        ability.startAbility(intent);
    }
}