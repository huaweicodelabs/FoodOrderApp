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
import com.huawei.posfoodordersapp.controller.DistributedNotificationPlugin;
import com.huawei.posfoodordersapp.controller.SendRequestRemote;
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.provider.ConfirmOrderProvider;
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
import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.Button;
import ohos.agp.components.ListContainer;
import ohos.agp.components.Text;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Image;
import ohos.agp.components.Component;
import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;

public class ConfirmOrderAbilitySlice extends AbilitySlice implements DistributedNotificationPlugin.DistributedNotificationEventListener {
    private Button mBtnConfirmOrderFood;
    private OrderDetailModel orderDetailsModel = new OrderDetailModel();
    private ArrayList<FoodMenuItem> cartList=new ArrayList<>();
    private ListContainer listContainer;
    private ConfirmOrderProvider sampleItemProvider;
    private MyDataBaseHelper dataBaseHelper;
    private DistributedNotificationPlugin distributedNotificationPlugin;
    private String customerDeviceId = "";
    private String customerDeviceType = "";
    private String shopKeeperDeviceId = "";
    private DeviceInfo shopKeeperDeviceInfo;
    private SendRequestRemote sendDataToPhoneRemoteProxy;
    private boolean isConfirmed = false;
    private Text text_totalitemprize;
    private int prize = 0;
    private DirectionalLayout loadingImgContent;
    private Image mLloadingBtn;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_abilityslice_confirm_order);

        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    shopKeeperDeviceId = deviceId;
                    LogUtil.debug(TAG_LOG,"Received deviceId from Intent: "+shopKeeperDeviceId);
                }
            }
        }
        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);
        initView();
        initData();
        initListContainer();
        initClickListerner();
        calculateTotal();
    }

    private void requirePermissions(String... permissions) {
        for (String permission: permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[] {permission}, MainAbility.REQUEST_CODE);
            }
        }
    }

    /* initView method is used to initialze the view components */
    private void initView() {
        listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container_cart);
        mBtnConfirmOrderFood=(Button) findComponentById(ResourceTable.Id_btn_confrim_orderfood);
        text_totalitemprize = (Text)findComponentById(ResourceTable.Id_text_totalitemsprize);
        loadingImgContent = (DirectionalLayout) findComponentById(ResourceTable.Id_loading_img_content);
        mLloadingBtn = (Image) findComponentById(ResourceTable.Id_imgLoading);
    }

    /* initData method is used to initialze the database class */
    private void initData() {
        dataBaseHelper = new MyDataBaseHelper(this);

        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
        distributedNotificationPlugin.setEventListener(this);
        distributedNotificationPlugin.subscribeEvent();

        DeviceManager.registerDeviceStateCallback(iDeviceStateCallback);

        if (Constants.getInstance().getOrderDetailModel() != null) {
            orderDetailsModel = Constants.getInstance().getOrderDetailModel();
        }
    }

    /* initListContainer method is used to initialze list container */
    private void initListContainer() {
        cartList = dataBaseHelper.viewCartItemsIsOrderConfirmed("false", "false");
        sampleItemProvider = new ConfirmOrderProvider(cartList, this);
        listContainer.setItemProvider(sampleItemProvider);
    }

    /* initClickListerner method is used to initialze click listners */
    private void initClickListerner() {
        mBtnConfirmOrderFood.setClickedListener(component -> {
            isConfirmed = true;
            showLoadingProgress();
            if (shopKeeperDeviceId != null && !shopKeeperDeviceId.isEmpty()
                && sendDataToPhoneRemoteProxy != null) {
                ArrayList<FoodMenuItem> tempCartList = dataBaseHelper.viewCartItems();
                sendResponseRemote(Constants.WAITING, tempCartList);
            } else {
                startService();
            }
        });
    }

    /* calculateTotal used to calculate total prize */
    private void calculateTotal() {
        // calculate total prize of cart
        for (int i = 0; i < cartList.size(); i++) {
            prize += cartList.get(i).getPrize() * cartList.get(i).getQuantity();
        }
        text_totalitemprize.setText("Total : " + prize);
    }
    private void showLoadingProgress() {
        loadingImgContent.setVisibility(Component.VISIBLE);
    }
    private void dismissLoadingProgress() {
        loadingImgContent.setVisibility(Component.INVISIBLE);
    }

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
            case Constants.MENU_CANCELED:
                CommonFunctions.getInstance().showToast(ConfirmOrderAbilitySlice.this,"Sorry for the inconvenience, your request for menu card is cancelled");
                break;
            case Constants.ORDER_CANCELED:
                CommonFunctions.getInstance().showToast(ConfirmOrderAbilitySlice.this,"Sorry for the inconvenience, your order is cancelled");
                break;
            case Constants.FINISH:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    /* startservice used to pass data to remote device */
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
            CommonFunctions.getInstance().showToast(this,"No device found");
            LogUtil.debug(LogUtil.TAG_LOG,"No device found");
        } else {
            deviceInfoList.forEach(deviceInformation -> {
                LogUtil.info(TAG_LOG, "Found devices - Name " + deviceInformation.getDeviceName());
                if (deviceInformation.getDeviceType() == DeviceInfo.DeviceType.SMART_PHONE) {
                    shopKeeperDeviceInfo = deviceInformation;
                    LogUtil.info(TAG_LOG, "Found device - Type " + deviceInformation.getDeviceType());
                    LogUtil.info(TAG_LOG, "Found device - State " + deviceInformation.getDeviceState());
                    LogUtil.info(TAG_LOG, "Found device - Id remote " + deviceInformation.getDeviceId());
                    LogUtil.info(TAG_LOG, "Found device - Id local " + shopKeeperDeviceId);
                    if (shopKeeperDeviceId == null || !shopKeeperDeviceId.contains(deviceInformation.getDeviceId())) {
                        shopKeeperDeviceId = deviceInformation.getDeviceId();
                    }
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
            List<AbilityInfo> abilityInfos = getBundleManager().queryAbilityByIntent(intent, IBundleManager.GET_BUNDLE_DEFAULT, 0);
            if (abilityInfos != null && !abilityInfos.isEmpty()) {
                connectAbility(intent, iAbilityConnection);
            } else {
                CommonFunctions.getInstance().showToast(this,"Cannot connect service on watch");
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
                    .createKvManager(new KvManagerConfig(ConfirmOrderAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getId();
            customerDeviceType = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(ConfirmOrderAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getType();
            LogUtil.debug(TAG_LOG,"Customer_deviceId: "+customerDeviceId);
            LogUtil.debug(TAG_LOG,"Customer_deviceType: "+customerDeviceType);
            CommonFunctions.getInstance().showToast(getApplicationContext(),"ability connect done!");
            sendDataToPhoneRemoteProxy = new SendRequestRemote(iRemoteObject, customerDeviceId);

            if (isConfirmed) {
                ArrayList<FoodMenuItem> tempCartList = dataBaseHelper.viewCartItemsIsOrderConfirmed
                        ("false","false");
                sendResponseRemote(Constants.WAITING, tempCartList);
            }else {
            }
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            LogUtil.info(TAG_LOG, "ability disconnect done!");
            disconnectAbility(iAbilityConnection);
        }
    };

    /* iDeviceStateCallback to check device is online or offline */
    private IDeviceStateCallback iDeviceStateCallback = new IDeviceStateCallback() {
        @Override
        public void onDeviceOffline(String s, int i) {
            CommonFunctions.getInstance().showToast(ConfirmOrderAbilitySlice.this,"Device offline");
        }

        @Override
        public void onDeviceOnline(String s, int i) {
            isConfirmed = false;
            startService();
        }
    };

    /* sendResponseRemote to send list of food items to remote device */
    private void sendResponseRemote(String status, ArrayList<FoodMenuItem> cartList) {
        if (customerDeviceId != null && !customerDeviceId.isEmpty()) {
            mBtnConfirmOrderFood.setAlpha((float) 0.5);
            mBtnConfirmOrderFood.setEnabled(false);
            orderDetailsModel.setListOfOrderItems(cartList);
            orderDetailsModel.setStatus(Constants.WAITING);
            orderDetailsModel.setCommand(Constants.ACCEPTMENU);
            orderDetailsModel.setMenuAccepted(true);
            orderDetailsModel.setOrderPlaced(true);
            // Order again
            orderDetailsModel.setOrderAccepted(false);
            orderDetailsModel.setOrderDelivered(false);
            orderDetailsModel.setPaid(false);
            sendDataToPhoneRemoteProxy.remoteControl(Constants.ACCEPTMENU, status, customerDeviceId, orderDetailsModel);
            CommonFunctions.getInstance().showToast(getApplicationContext(),"Your Food is being prepared! Thank you:)");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DeviceManager.unregisterDeviceStateCallback(iDeviceStateCallback);
        distributedNotificationPlugin.unsubscribeEvent();
    }
}
