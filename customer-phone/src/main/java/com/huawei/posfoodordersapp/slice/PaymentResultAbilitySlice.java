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
import com.huawei.posfoodordersapp.controller.SendRequestRemote;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.service.DeviceService;
import com.huawei.posfoodordersapp.utils.LogUtil;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.IBundleManager;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.distributedschedule.interwork.DeviceInfo;
import ohos.distributedschedule.interwork.DeviceManager;
import ohos.distributedschedule.interwork.IDeviceStateCallback;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.security.SystemPermission;

import java.util.List;

public class PaymentResultAbilitySlice extends AbilitySlice {
    private Button mBtnExit;
    private Text textAmount;
    private String shopKeeperDeviceId = "";
    private DeviceInfo shopKeeperDeviceInfo;
    private SendRequestRemote sendDataToPhoneRemoteProxy;
    private OrderDetailModel orderDetailsModel = new OrderDetailModel();
    private String customerDeviceId = "";
    private String customerDeviceType = "";
    private LottieAnimationView lv_animationView_Success;
    private LottieAnimationViewData data_animationView_Success;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_payment_result);
        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);
        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    shopKeeperDeviceId = deviceId;
                    LogUtil.debug(LogUtil.TAG_LOG, "Received deviceId from Intent: " + shopKeeperDeviceId);

                    if (shopKeeperDeviceId != null && !shopKeeperDeviceId.isEmpty()) {
                        startService();
                    }
                }
            }
        }
        initView();
        initClickListener();
    }

    private void initView() {
        mBtnExit = (Button) findComponentById(ResourceTable.Id_btnexit);
        textAmount = (Text) findComponentById(ResourceTable.Id_amount);
        DeviceManager.registerDeviceStateCallback(iDeviceStateCallback);
        if (Constants.getInstance().getOrderDetailModel() != null) {
            orderDetailsModel = Constants.getInstance().getOrderDetailModel();
            textAmount.setText("Rs " + String.valueOf(orderDetailsModel.getTotalAmount()));
        }
        lv_animationView_Success = (LottieAnimationView) findComponentById(ResourceTable.Id_animationView_Success);
        data_animationView_Success = new LottieAnimationViewData();
        data_animationView_Success.setUrl(Constants.LOTTIANIM_SUCCESS);
        data_animationView_Success.autoPlay = true;
        data_animationView_Success.setRepeatCount(1000); // specify repetition count
        lv_animationView_Success.setAnimationData(data_animationView_Success);
    }

    private void initClickListener() {
        mBtnExit.setClickedListener(component -> navigateToUserDetailsScreen());
    }

    private void requirePermissions(String... permissions) {
        for (String permission : permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[]{permission}, MainAbility.REQUEST_CODE);
            }
        }
    }

    private void navigateToUserDetailsScreen() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(Constants.PACKAGE_NAME)
                .withAbilityName(Constants.MAIN_ABILITY)
                .withAction(Constants.CALL_CUSTOMER_DETAILS_ACTION)
                .build();
        intent.setOperation(operation);
        intent.setParam("DeviceId", shopKeeperDeviceId);
        startAbility(intent, Intent.FLAG_ABILITY_NEW_MISSION | Intent.FLAG_ABILITY_CLEAR_MISSION);
    }

    private void startService() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(getBundleName())
                .withAbilityName(DeviceService.class.getName())
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);
        startAbility(intent);
        fetchOnlinePhoneDevice();
    }

    private void fetchOnlinePhoneDevice() {
        List<DeviceInfo> deviceInfoList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
        if (deviceInfoList.isEmpty()) {
            CommonFunctions.getInstance().showToast(this, "No device found");
            LogUtil.debug(LogUtil.TAG_LOG, "No device found");
        } else {
            deviceInfoList.forEach(deviceInformation -> {
                LogUtil.info(LogUtil.TAG_LOG, "Found devices - Name " + deviceInformation.getDeviceName());
                if (deviceInformation.getDeviceType() == DeviceInfo.DeviceType.SMART_PHONE) {
                    shopKeeperDeviceInfo = deviceInformation;
                    LogUtil.info(LogUtil.TAG_LOG, "Found device - Type " + deviceInformation.getDeviceType());
                    LogUtil.info(LogUtil.TAG_LOG, "Found device - State " + deviceInformation.getDeviceState());
                    LogUtil.info(LogUtil.TAG_LOG, "Found device - Id remote " + deviceInformation.getDeviceId());
                    LogUtil.info(LogUtil.TAG_LOG, "Found device - Id local " + shopKeeperDeviceId);

                    connectToRemoteService();
                }
            });
        }
    }

    private void connectToRemoteService() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId(shopKeeperDeviceInfo.getDeviceId())
                .withBundleName(Constants.PACKAGE_NAME)
                .withAbilityName(Constants.ACK_RESPONSE_SERVICEABILITY)
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);
        try {
            List<AbilityInfo> abilityInfos = getBundleManager().queryAbilityByIntent(intent, IBundleManager.GET_BUNDLE_DEFAULT, 0);
            if (abilityInfos != null && !abilityInfos.isEmpty()) {
                connectAbility(intent, iAbilityConnection);
            } else {
                CommonFunctions.getInstance().showToast(this, "Cannot connect service on watch");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private IAbilityConnection iAbilityConnection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            LogUtil.info(LogUtil.TAG_LOG, "ability connect done!");

            customerDeviceId = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(PaymentResultAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getId();
            customerDeviceType = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(PaymentResultAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getType();
            LogUtil.debug(LogUtil.TAG_LOG, "Customer_deviceId: " + customerDeviceId);
            LogUtil.debug(LogUtil.TAG_LOG, "Customer_deviceType: " + customerDeviceType);

            CommonFunctions.getInstance().showToast(getApplicationContext(), "ability connect done!");
            sendDataToPhoneRemoteProxy = new SendRequestRemote(iRemoteObject, customerDeviceId);

            sendResponseRemote(Constants.PAID);
        }
        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            LogUtil.info(LogUtil.TAG_LOG, "ability disconnect done!");
            disconnectAbility(iAbilityConnection);
        }
    };

    private IDeviceStateCallback iDeviceStateCallback = new IDeviceStateCallback() {
        @Override
        public void onDeviceOffline(String s, int i) {
            CommonFunctions.getInstance().showToast(PaymentResultAbilitySlice.this, "Device offline");
        }

        @Override
        public void onDeviceOnline(String s, int i) {
            startService();
        }
    };

    private void sendResponseRemote(String status) {
        if (shopKeeperDeviceId != null && !shopKeeperDeviceId.isEmpty()) {
            CommonFunctions.getInstance().showToast(getApplicationContext(), "Payment Received");
            orderDetailsModel.setStatus(Constants.PAID);
            orderDetailsModel.setCommand(Constants.PAYMENTRECEIVED);
            orderDetailsModel.setMenuAccepted(true);
            orderDetailsModel.setOrderPlaced(true);
            orderDetailsModel.setOrderAccepted(true);
            orderDetailsModel.setOrderDelivered(true);
            orderDetailsModel.setPaid(true);
            sendDataToPhoneRemoteProxy.remoteControl(Constants.PAYMENTRECEIVED, status, customerDeviceId, orderDetailsModel);
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
    protected void onStop() {
        super.onStop();
        DeviceManager.unregisterDeviceStateCallback(iDeviceStateCallback);
    }
}
