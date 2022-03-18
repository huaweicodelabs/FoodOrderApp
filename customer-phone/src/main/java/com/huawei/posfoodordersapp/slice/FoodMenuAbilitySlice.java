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
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.provider.FoodMenuProvider;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.ListContainer;
import ohos.agp.components.Text;

import java.util.ArrayList;
import java.util.List;

import static com.huawei.posfoodordersapp.utils.Constants.CALL_FOODORDERYSLICE_ACTION;
import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;

public class FoodMenuAbilitySlice extends AbilitySlice {
    private String shopKeeperDeviceId = "";
    private ListContainer listContainer;
    private Button mBtnOrderNow;
    private MyDataBaseHelper dataBaseHelper;
    private FoodMenuProvider sampleItemProvider;
    private Text mImgCancel;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_food_menu);
        getData();
        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    shopKeeperDeviceId = deviceId;
                    LogUtil.debug(TAG_LOG, "Received deviceId from Intent: " + shopKeeperDeviceId);
                }
            }
        }
        dataBaseHelper = new MyDataBaseHelper(this);
        initView();
        initListContainer();
        initClickListener();
    }

    private void initView() {
        listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container);
        mBtnOrderNow = (Button) findComponentById(ResourceTable.Id_btn_Order_now);
        mImgCancel = (Text) findComponentById(ResourceTable.Id_cancel);
    }

    /* initListContainer used to initiate list container */
    private void initListContainer() {
        List<FoodMenuItem> list = getData();
        sampleItemProvider = new FoodMenuProvider(list, this);
        listContainer.setItemProvider(sampleItemProvider);
        sampleItemProvider.notifyDataChanged();
    }

    /* initClickListener used to initiate click listenere */
    private void initClickListener() {
        mBtnOrderNow.setClickedListener(component -> {
            ArrayList<FoodMenuItem> cartList = dataBaseHelper.viewCartItems();
            if (cartList.size() >= 1) {
                navigateFoodorderScreen();
            } else {
                CommonFunctions.getInstance().showToast(FoodMenuAbilitySlice.this, "Please select any food");
            }
        });
        mImgCancel.setClickedListener(component -> {
            dataBaseHelper.deleteTable();
            present(new CustomerDetailsAbilitySlice(), new Intent());
        });

    }

    private void navigateFoodorderScreen() {
        Intent intent1 = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withAction(CALL_FOODORDERYSLICE_ACTION)
                .build();
        intent1.setOperation(operation);
        intent1.setParam("DeviceId", shopKeeperDeviceId);
        startAbility(intent1);
    }
/*
* Loaded with food item images User can replace imgeURL */
    private List<FoodMenuItem> getData() {

        ArrayList<FoodMenuItem> arrayList = new ArrayList<>();

        FoodMenuItem foodMenutem1 = new FoodMenuItem();
        foodMenutem1.setName("Panner Tikka");
        foodMenutem1.setPrize(200);
        foodMenutem1.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem2 = new FoodMenuItem();
        foodMenutem2.setName("Veg Biriyani");
        foodMenutem2.setPrize(150);
        foodMenutem2.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem3 = new FoodMenuItem();
        foodMenutem3.setName("Gobi Manchurian");
        foodMenutem3.setPrize(170);
        foodMenutem3.setImageurl("YOUR_IMAGE_URL");


        FoodMenuItem foodMenutem4 = new FoodMenuItem();
        foodMenutem4.setName("Chilli Chicken");
        foodMenutem4.setPrize(290);
        foodMenutem4.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem5 = new FoodMenuItem();
        foodMenutem5.setName("Mushroom Masala");
        foodMenutem5.setPrize(265);
        foodMenutem5.setImageurl("YOUR_IMAGE_URL");


        FoodMenuItem foodMenutem6 = new FoodMenuItem();
        foodMenutem6.setName("Baby Corn");
        foodMenutem6.setPrize(249);
        foodMenutem6.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem7 = new FoodMenuItem();
        foodMenutem7.setName("Carrot 65");
        foodMenutem7.setPrize(215);
        foodMenutem7.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem8 = new FoodMenuItem();
        foodMenutem8.setName("Mutton Chops");
        foodMenutem8.setPrize(365);
        foodMenutem8.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem9 = new FoodMenuItem();
        foodMenutem9.setName("Cauliflower Chilli");
        foodMenutem9.setPrize(240);
        foodMenutem9.setImageurl("YOUR_IMAGE_URL");

        FoodMenuItem foodMenutem10 = new FoodMenuItem();
        foodMenutem10.setName("Paneer Fry");
        foodMenutem10.setPrize(265);
        foodMenutem10.setImageurl("YOUR_IMAGE_URL");

        arrayList.add(foodMenutem1);
        arrayList.add(foodMenutem2);
        arrayList.add(foodMenutem3);
        arrayList.add(foodMenutem4);
        arrayList.add(foodMenutem5);
        arrayList.add(foodMenutem6);
        arrayList.add(foodMenutem7);
        arrayList.add(foodMenutem8);
        arrayList.add(foodMenutem9);
        arrayList.add(foodMenutem10);

        return arrayList;
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
