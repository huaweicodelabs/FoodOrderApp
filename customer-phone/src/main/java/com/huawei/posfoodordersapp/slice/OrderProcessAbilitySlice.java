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

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.value.LottieAnimationViewData;
import com.huawei.posfoodordersapp.MainAbility;
import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.controller.DistributedNotificationPlugin;
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.utils.LogUtil;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.bundle.IBundleManager;
import ohos.security.SystemPermission;

public class OrderProcessAbilitySlice extends AbilitySlice implements DistributedNotificationPlugin.DistributedNotificationEventListener {
    private Button mBtnConfirmOrderFood;
    private LottieAnimationView lv;
    private LottieAnimationViewData data;

    private DistributedNotificationPlugin distributedNotificationPlugin;
    private String shopKeeperDeviceId= "";
    private MyDataBaseHelper myDataBaseHelper;
    
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_order_processing);

        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);

        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    shopKeeperDeviceId = deviceId;
                    LogUtil.debug(LogUtil.TAG_LOG,"Received deviceId from Intent: "+shopKeeperDeviceId);
                }
            }
        }

        initView();
        initData();
    }

    private void initView() {
        mBtnConfirmOrderFood=(Button) findComponentById(ResourceTable.Id_btn_confrim_orderfood);
        lv = (LottieAnimationView)findComponentById(ResourceTable.Id_animationView);
        data = new LottieAnimationViewData();
        data.setUrl(Constants.LOTTIANIM_COOKING);
        data.autoPlay = true;
        data.setRepeatCount(1000); // specify repetition count
        lv.setAnimationData(data);
    }

    private void initData() {
        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
        distributedNotificationPlugin.setEventListener(this);
        distributedNotificationPlugin.subscribeEvent();

        myDataBaseHelper = new MyDataBaseHelper(this);
        if (Constants.getInstance().getOrderDetailModel() != null) {
            OrderDetailModel orderDetailModel = Constants.getInstance().getOrderDetailModel();
            LogUtil.debug(LogUtil.TAG_LOG,"OrderID_from_shopkeeper: "+orderDetailModel.getOrderID());
            myDataBaseHelper.updateDataOrderID(orderDetailModel.getOrderID(), "true", "true");
        }
    }

    private void requirePermissions(String... permissions) {
        for (String permission: permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[] {permission}, MainAbility.REQUEST_CODE);
            }
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

    @Override
    public void onEventSubscribe(String result) {
        LogUtil.info(LogUtil.TAG_LOG, result);
    }

    @Override
    public void onEventPublish(String result) {
        LogUtil.info(LogUtil.TAG_LOG, result);
    }

    @Override
    public void onEventUnSubscribe(String result) {
        LogUtil.info(LogUtil.TAG_LOG, result);
    }

    @Override
    public void onEventReceive(String result) {
        LogUtil.debug(LogUtil.TAG_LOG,"onEventReceiveFoodOrderAbilityslice");
        LogUtil.info(LogUtil.TAG_LOG, result);

        switch (result) {
            case Constants.FOODPRICE:
                break;
            case Constants.FINISH:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        distributedNotificationPlugin.unsubscribeEvent();
    }
}
