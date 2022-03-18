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

package com.huawei.posfoodordersapp.slice;

import com.huawei.posfoodordersapp.MainAbility;
import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.provider.FoodPaymentDetailsProvider;
import com.huawei.posfoodordersapp.utils.Constants;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import ohos.agp.components.Text;

import java.util.ArrayList;

public class FoodPaymentDetailsAbilitySlice extends AbilitySlice {
    private Button mBtnPay;
    private Button mBtnorderagain;
    private Text totalvalue;
    private String shopKeeperDeviceId = "";
    private MyDataBaseHelper dataBaseHelper;
    private ArrayList<FoodMenuItem> cartList = new ArrayList<>();
    private int prize = 0;
    private MyDataBaseHelper databaseHelper;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_payment_details);

        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    shopKeeperDeviceId = deviceId;
                    LogUtil.debug(LogUtil.TAG_LOG, "Received deviceId from Intent: " + shopKeeperDeviceId);
                }
            }
        }

        initView();
        initClickListener();
        initListContainer();
        calculateTotal();
    }

    /* initView to initialze component */
    private void initView() {
        mBtnPay = (Button) findComponentById(ResourceTable.Id_btnpay);
        mBtnorderagain = (Button) findComponentById(ResourceTable.Id_btnorderagain);
        totalvalue = (Text) findComponentById(ResourceTable.Id_totalvalue);

        dataBaseHelper = new MyDataBaseHelper(this);
    }

    /* initClickListener used to initialze click listener */
    private void initClickListener() {

        mBtnPay.setClickedListener(component -> {
            // Deleting table value
            dataBaseHelper.deleteTable();

            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withBundleName(getBundleName())
                    .withAbilityName(MainAbility.class.getName())
                    .withAction(Constants.CALL_PAYMENT_SLICE)
                    .build();
            intent.setOperation(operation);
            intent.setParam("DeviceId", shopKeeperDeviceId);
            startAbility(intent);
        });

        mBtnorderagain.setClickedListener(component -> {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withBundleName(getBundleName())
                    .withAbilityName(MainAbility.class.getName())
                    .withAction(Constants.CALL_FOODMENUITEM_ACTION)
                    .build();
            intent.setOperation(operation);
            intent.setParam("DeviceId", shopKeeperDeviceId);
            startAbility(intent);
        });
    }

    private void initListContainer() {
        ListContainer listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container_items);
        cartList = dataBaseHelper.viewCartItems();
        cartList = getCartList();
        FoodPaymentDetailsProvider sampleItemProvider = new FoodPaymentDetailsProvider(cartList, this);
        listContainer.setItemProvider(sampleItemProvider);
    }

    public ArrayList<FoodMenuItem> getCartList() {
        ArrayList<FoodMenuItem> allCartList = new ArrayList<FoodMenuItem>();
        boolean isAvailable = false;
        for (int i = 0; i < cartList.size(); i++) {
            if (allCartList.size() != 0) {
                int position = 0;
                for (FoodMenuItem foodMenuItem : allCartList) {
                    if (cartList.get(i).getName().equals(foodMenuItem.getName())) {
                        isAvailable = true;
                        allCartList.remove(position);
                        position++;
                        cartList.get(i).setQuantity(foodMenuItem.getQuantity() + 1);
                        break;
                    } else {
                        position++;
                        isAvailable = false;
                    }
                }
            }
            if (isAvailable) {
                allCartList.add(cartList.get(i));
            } else {
                allCartList.add(cartList.get(i));
            }
        }
        return allCartList;
    }

    /* calculateTotal prize */
    private void calculateTotal() {
        // calculate total prize of cart
        int tax = 55;
        int gst = 55;
        int deliverycharge = 55;
        for (int i = 0; i < cartList.size(); i++) {
            prize += cartList.get(i).getPrize() * cartList.get(i).getQuantity();
        }
        int total = prize + tax + gst + deliverycharge;
        totalvalue.setText("RS " + total);
        if (Constants.getInstance().getOrderDetailModel() != null) {
            Constants.getInstance().getOrderDetailModel().setTotalAmount(total);
        }
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
