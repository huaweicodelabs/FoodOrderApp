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
import static com.huawei.posfoodordersapp.utils.Constants.CUSTORDERNOTPLACED;
import static com.huawei.posfoodordersapp.utils.Constants.ACCEPTMENU;
import static com.huawei.posfoodordersapp.utils.Constants.CONFIRMORDER;
import static com.huawei.posfoodordersapp.utils.Constants.FOODPRICE;
import static com.huawei.posfoodordersapp.utils.Constants.MENU_CANCELED;
import static com.huawei.posfoodordersapp.utils.Constants.ORDER_CANCELED;
import static com.huawei.posfoodordersapp.utils.Constants.CALL_CUSTOMER_DETAILS_ACTION;
import static com.huawei.posfoodordersapp.utils.Constants.CALL_FOODMENUITEM_ACTION;
import static com.huawei.posfoodordersapp.utils.Constants.CALL_ORDERCONFIRMATON_WITHTIMER_ACTION;
import static com.huawei.posfoodordersapp.utils.Constants.CALL_FOOD_PAYMENT_DETAILS_ACTION;

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
            String shopKeeperDeviceId = data.readString();
            OrderDetailModel orderDetailModel = (OrderDetailModel) data.readValue();
            Constants.getInstance().setOrderDetailModel(orderDetailModel);

            LogUtil.debug("TAG", "Request_remote_orderID: " + orderDetailModel.getOrderID());
            LogUtil.debug("TAG", "Request_remote_shopKeeperDeviceId: " + shopKeeperDeviceId);
            LogUtil.debug("TAG", "Request_command: " + command);

            distributedNotificationPlugin.publishEvent(command);

            if (Constants.START.equals(command)) {
                LogUtil.debug("TAG", "Request_command First Time");
                openAbility(shopKeeperDeviceId);
            } else if (Constants.ACCEPTMENU.equalsIgnoreCase(command) || CUSTORDERNOTPLACED.equalsIgnoreCase(command)) {
                LogUtil.debug("TAG", ACCEPTMENU);
                openFoodMenuItemAbility(shopKeeperDeviceId);
            } else if (Constants.ORDERCONFIRM.equalsIgnoreCase(command)) {
                LogUtil.debug("TAG", CONFIRMORDER);
                openOrderConfirmationWithTimerAbilitySlice(command);
            } else if (FOODPRICE.equalsIgnoreCase(command)) {
                LogUtil.debug("TAG", FOODPRICE);
                openFoodPriceDetailsAbilitySlice(shopKeeperDeviceId);
            } else if (MENU_CANCELED.equalsIgnoreCase(command)) {

            } else if (ORDER_CANCELED.equalsIgnoreCase(command)) {

            }
            return true;
        }
        return false;
    }
    /* openAbility to used to get navigate respective ability
     * */
    private void openAbility(String shopKeeperDeviceId) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(ability.getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(CALL_CUSTOMER_DETAILS_ACTION)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", shopKeeperDeviceId);
        ability.startAbility(intent);
    }

    private void openFoodMenuItemAbility(String shopKeeperDeviceId) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(ability.getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(CALL_FOODMENUITEM_ACTION)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", shopKeeperDeviceId);
        ability.startAbility(intent);
    }

    private void openOrderConfirmationWithTimerAbilitySlice(String shopKeeperDeviceId) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(ability.getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(CALL_ORDERCONFIRMATON_WITHTIMER_ACTION)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", shopKeeperDeviceId);
        ability.startAbility(intent);
    }

    private void openFoodPriceDetailsAbilitySlice(String deviceId) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(ability.getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(CALL_FOOD_PAYMENT_DETAILS_ACTION)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", deviceId);
        ability.startAbility(intent);
    }
}
