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

import com.andexert.library.RippleView;
import com.huawei.posfoodordersapp.MainAbility;
import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.controller.DistributedNotificationPlugin;
import com.huawei.posfoodordersapp.controller.SendRequestRemote;
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.service.DeviceService;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.Constants;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.animation.AnimatorProperty;
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
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Image;
import ohos.agp.components.Component;
import ohos.agp.components.DependentLayout;
import ohos.agp.components.TextField;
import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;

public class CustomerDetailsAbilitySlice extends AbilitySlice implements DistributedNotificationPlugin.DistributedNotificationEventListener {
    private Button mBtnRequestMenu;
    private Button mBtnContinue;
    private DependentLayout mDepWelcome;
    private DependentLayout mDiUserDetails;
    private Image mLloadingBtn;
    private Text mTxtTitle;
    private DirectionalLayout loadingImgContent;
    private TextField mTextUserName;
    private TextField mTextNoOfPeople;
    private RippleView mRippleContinue;
    private RippleView mRippleRequestmenu;
    private String shopKeeperDeviceId = "";
    private String customerDeviceId = "";
    private String customerDeviceType = "";
    private DistributedNotificationPlugin distributedNotificationPlugin;
    private DeviceInfo shopKeeperDeviceInfo;
    private SendRequestRemote sendDataToPhoneRemoteProxy;
    private MyDataBaseHelper dataBaseHelper;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_customer_details);
        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);
        dataBaseHelper = new MyDataBaseHelper(this);
        initView();
        initData();
        initClickListener();
        animatorPropertyHandle();
        mTextUserName.setText("Vikraman");
        mTextNoOfPeople.setText("2");
    }

    private void requirePermissions(String... permissions) {
        for (String permission : permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[]{permission}, MainAbility.REQUEST_CODE);
            }
        }
    }

    /* initview used to initiate component */
    private void initView() {
        mBtnRequestMenu = (Button) findComponentById(ResourceTable.Id_btn_requestmenu);
        mBtnContinue = (Button) findComponentById(ResourceTable.Id_btn_continue);
        mDepWelcome = (DependentLayout) findComponentById(ResourceTable.Id_dep_welcome);
        mDiUserDetails = (DependentLayout) findComponentById(ResourceTable.Id_di_userdetails);
        loadingImgContent = (DirectionalLayout) findComponentById(ResourceTable.Id_loading_img_content);
        mLloadingBtn = (Image) findComponentById(ResourceTable.Id_imgLoading);
        mTxtTitle = (Text) findComponentById(ResourceTable.Id_txt_title);
        mTextUserName = (TextField) findComponentById(ResourceTable.Id_text_username);
        mTextNoOfPeople = (TextField) findComponentById(ResourceTable.Id_text_no_of_people);
        mRippleContinue = (RippleView) findComponentById(ResourceTable.Id_ripple_continue);
        mRippleRequestmenu = (RippleView) findComponentById(ResourceTable.Id_ripple_requestmenu);
        mTxtTitle.setText("Requesting menu card. Please wait a moment...");
    }

    /* initData initiate distributed class */
    private void initData() {
        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
        distributedNotificationPlugin.setEventListener(this);
        distributedNotificationPlugin.subscribeEvent();
        DeviceManager.registerDeviceStateCallback(iDeviceStateCallback);
    }

    /* initClickListener used to click listener */
    private void initClickListener() {
        mBtnRequestMenu.setClickedListener(component -> {
            if (!mTextUserName.getText().isEmpty() && !mTextNoOfPeople.getText().isEmpty()) {
                showLoadingProgress();
                fetchOnlinePhoneDevice();
            } else {
                CommonFunctions.getInstance().showToast(CustomerDetailsAbilitySlice.this, "Please enter the user details");
            }
        });

        mBtnContinue.setClickedListener(component -> {
            dataBaseHelper.deleteTable();
            mDepWelcome.setVisibility(Component.HIDE);
            mDiUserDetails.setVisibility(Component.VISIBLE);
        });
    }

    /* animatorPropertyHandle used to animate progrss bar */
    private void animatorPropertyHandle() {
        AnimatorProperty mAnimatorProperty = mLloadingBtn.createAnimatorProperty();
        mAnimatorProperty.rotate(360).setDuration(2000).setLoopedCount(1000);
        mLloadingBtn.setBindStateChangedListener(new Component.BindStateChangedListener() {
            @Override
            public void onComponentBoundToWindow(Component component) {
                if (mAnimatorProperty != null) {
                    mAnimatorProperty.start();
                }
            }
            @Override
            public void onComponentUnboundFromWindow(Component component) {
            }
        });
    }

    private void handlingLoadingProgress() {
        if (loadingImgContent.getVisibility() == Component.VISIBLE) {
            loadingImgContent.setVisibility(Component.INVISIBLE);
        }
        else {
            loadingImgContent.setVisibility(Component.VISIBLE);
        }
    }

    private void showLoadingProgress() {
        mBtnRequestMenu.setEnabled(true);
        mRippleRequestmenu.setEnabled(true);
        mBtnRequestMenu.setClickable(true);
        mRippleRequestmenu.setClickable(true);
        loadingImgContent.setVisibility(Component.VISIBLE);
    }

    private void dismissLoadingProgress() {
        mBtnRequestMenu.setEnabled(false);
        mRippleRequestmenu.setEnabled(false);
        mBtnRequestMenu.setClickable(false);
        mRippleRequestmenu.setClickable(false);
        loadingImgContent.setVisibility(Component.INVISIBLE);
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
        LogUtil.debug(LogUtil.TAG_LOG, "onEventReceiveUserdetails");
        LogUtil.info(LogUtil.TAG_LOG, result);

        switch (result) {
            case Constants.MENU_ACCEPTED:
                dismissLoadingProgress();
                break;
            case Constants.MENU_CANCELED:
                dismissLoadingProgress();
                CommonFunctions.getInstance().showToast(CustomerDetailsAbilitySlice.this, "Sorry for the inconvenience, your request for menu card is cancelled");
                break;
            case Constants.ORDER_CANCELED:
                dismissLoadingProgress();
                CommonFunctions.getInstance().showToast(CustomerDetailsAbilitySlice.this, "Sorry for the inconvenience, your order is cancelled");
                break;
            case Constants.FINISH:
                System.exit(0);
                break;
            default:
                break;
        }
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

    /* fetchOnlinePhoneDevice is usage for fetch availble device in same WIFI and same account */
    private void fetchOnlinePhoneDevice() {
        List<DeviceInfo> deviceInfoList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
        if (deviceInfoList.isEmpty()) {
            dismissLoadingProgress();
            CommonFunctions.getInstance().showToast(this, "No device found");
            LogUtil.debug(LogUtil.TAG_LOG, "No device found");
        } else {
            deviceInfoList.forEach(deviceInformation -> {
                LogUtil.info(TAG_LOG, "Found devices - Name " + deviceInformation.getDeviceName());
                if (deviceInformation.getDeviceType() == DeviceInfo.DeviceType.SMART_PHONE) {
                    shopKeeperDeviceInfo = deviceInformation;
                    LogUtil.info(TAG_LOG, "Found device - Type " + deviceInformation.getDeviceType());
                    LogUtil.info(TAG_LOG, "Found device - State " + deviceInformation.getDeviceState());
                    LogUtil.info(TAG_LOG, "Found device - Id remote " + deviceInformation.getDeviceId());
                    LogUtil.info(TAG_LOG, "Found device - Id local " + shopKeeperDeviceId);
                    connectToRemoteService();
                }
            });
        }
    }

    /* connectToRemoteService connecting remote device package classes */
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
            List<AbilityInfo> abilityInfos = getBundleManager().queryAbilityByIntent(intent,
                    IBundleManager.GET_BUNDLE_DEFAULT, 0);
            if (abilityInfos != null && !abilityInfos.isEmpty()) {
                connectAbility(intent, iAbilityConnection);
            } else {
                dismissLoadingProgress();
                CommonFunctions.getInstance().showToast(this, "Cannot connect service on phone");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* iAbilityConnection to establish connecting remote device */
    private IAbilityConnection iAbilityConnection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            LogUtil.info(TAG_LOG, "ability connect done!");

            customerDeviceId = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(CustomerDetailsAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getId();
            customerDeviceType = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(CustomerDetailsAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getType();
            LogUtil.debug(TAG_LOG, "Customer_deviceId: " + customerDeviceId);
            LogUtil.debug(TAG_LOG, "Customer_deviceType: " + customerDeviceType);

            sendDataToPhoneRemoteProxy = new SendRequestRemote(iRemoteObject, customerDeviceId);

            sendRequestToShopKeeper(Constants.REQUEST_MENU, Constants.PENDING, customerDeviceId);
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            LogUtil.info(TAG_LOG, "ability disconnect done!");
            disconnectAbility(iAbilityConnection);
            dismissLoadingProgress();
        }
    };

    /* iDeviceStateCallback to check device is online or offline */
    private IDeviceStateCallback iDeviceStateCallback = new IDeviceStateCallback() {
        @Override
        public void onDeviceOffline(String s, int i) {
            CommonFunctions.getInstance().showToast(CustomerDetailsAbilitySlice.this, "Device offline");
        }

        @Override
        public void onDeviceOnline(String s, int i) {
            CommonFunctions.getInstance().showToast(CustomerDetailsAbilitySlice.this, "Device online");
            fetchOnlinePhoneDevice();
        }
    };

    /* sendRequestToShopKeeper used  to send data to remote class */
    private void sendRequestToShopKeeper(String action, String status, String customerDeviceId) {
        if (sendDataToPhoneRemoteProxy != null) {
            sendDataToPhoneRemoteProxy.remoteControl(action, status, customerDeviceId, formUserDetails());
        }
        CommonFunctions.getInstance().showToast(getApplicationContext(), "Requesting menu card");
    }

    private OrderDetailModel formUserDetails() {
        OrderDetailModel orderDetailModel = new OrderDetailModel();
        orderDetailModel.setOrderID(0);
        orderDetailModel.setDeviceID(customerDeviceId);
        orderDetailModel.setDeviceType("Smart Phone");
        orderDetailModel.setUsername(mTextUserName.getText());
        orderDetailModel.setTotalNoPeople(Integer.parseInt(mTextNoOfPeople.getText()));
        orderDetailModel.setTableNumber(1);
        orderDetailModel.setStatus(Constants.PENDING);
        orderDetailModel.setCommand(Constants.REQUEST_MENU);
        orderDetailModel.setMenuAccepted(false);
        orderDetailModel.setOrderPlaced(false);
        orderDetailModel.setOrderAccepted(false);
        orderDetailModel.setOrderDelivered(false);
        orderDetailModel.setPaid(false);

        return orderDetailModel;
    }


    @Override
    protected void onStop() {
        super.onStop();
        DeviceManager.unregisterDeviceStateCallback(iDeviceStateCallback);
        distributedNotificationPlugin.unsubscribeEvent();
    }
}
