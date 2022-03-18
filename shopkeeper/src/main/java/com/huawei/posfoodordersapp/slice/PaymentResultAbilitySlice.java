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

import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.provider.PaymentResultProvider;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ListContainer;
import ohos.agp.components.Text;

import java.util.ArrayList;

import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;

public class PaymentResultAbilitySlice extends AbilitySlice {
    private OrderDetailModel orderDetailModel;
    private int currentPosition = 0;
    private ListContainer listContainer;
    private PaymentResultProvider paymentResultProvider;
    private Text totalvalue;
    private ArrayList<FoodMenuItem> cartList = new ArrayList<>();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_payment_result);

        if (intent != null) {
            if (intent.hasParameter("OrderDetails")) {
                Object obj = intent.getParams().getParam("OrderDetails");
                if (obj instanceof OrderDetailModel) {
                    LogUtil.debug(TAG_LOG,"Received obj from Intent: "+obj);
                    orderDetailModel = (OrderDetailModel) obj;
                }
            }
            if (intent.hasParameter("Position")) {
                Object obj = intent.getParams().getParam("Position");
                if (obj instanceof Integer) {
                    int pos = (int) obj;
                    currentPosition = pos;
                    LogUtil.debug(TAG_LOG,"Received Position from Intent: "+currentPosition);
                }
            }
        }
        initView();
        initListContainer();
    }

    private void initView() {
        totalvalue = (Text)findComponentById(ResourceTable.Id_totalvalue);
        listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container_items);
        totalvalue.setText(String.valueOf(orderDetailModel.getTotalAmount()));
    }
    private void initListContainer() {
        cartList = getCartList();
        paymentResultProvider = new PaymentResultProvider(cartList, this);
        listContainer.setItemProvider(paymentResultProvider);
    }
    public ArrayList<FoodMenuItem> getCartList(){
        ArrayList<FoodMenuItem> allCartList = new ArrayList<FoodMenuItem>();
        boolean isAvailable=false;
        for (int i = 0; i < orderDetailModel.getListOfOrderItems().size(); i++) {
            if(allCartList.size()!=0) {
                int position=0;
                for (FoodMenuItem foodMenuItem : allCartList) {
                    if (orderDetailModel.getListOfOrderItems().get(i).getName().equals(foodMenuItem.getName())) {
                        isAvailable = true;
                        allCartList.remove(position);
                        position++;
                        orderDetailModel.getListOfOrderItems().get(i).setQuantity(foodMenuItem.getQuantity() + 1);
                        break;
                    }else {
                        position++;
                        isAvailable = false;
                    }
                }
            }
            if(isAvailable){
                allCartList.add(orderDetailModel.getListOfOrderItems().get(i));
            }else {
                allCartList.add(orderDetailModel.getListOfOrderItems().get(i));
            }
        }
        return allCartList;
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
