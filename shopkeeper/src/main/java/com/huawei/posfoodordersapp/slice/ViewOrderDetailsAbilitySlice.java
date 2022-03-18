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
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.provider.FoodOrderedProvider;
import com.huawei.posfoodordersapp.service.DeviceService;
import com.huawei.posfoodordersapp.utils.LogUtil;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.IBundleManager;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.distributedschedule.interwork.DeviceManager;
import ohos.distributedschedule.interwork.IDeviceStateCallback;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.security.SystemPermission;

import java.util.ArrayList;
import java.util.List;

import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;

public class ViewOrderDetailsAbilitySlice extends AbilitySlice implements DistributedNotificationPlugin.DistributedNotificationEventListener {
    private Button mBtnAcceptOrder;
    private Button mBtnReadyToDeliver;
    private ListContainer listContainer;
    private FoodOrderedProvider sampleItemProvider;

    private int currentPosition = 0;
    private String shopKeeperDeviceId= "";
    private String currentCustomerAction = "";
    private SendRequestRemote sendDataToWatchRemoteProxy;
    private OrderDetailModel orderDetailModel;

    private DistributedNotificationPlugin distributedNotificationPlugin;
    private ArrayList<FoodMenuItem> cartList = new ArrayList<>();
    private ArrayList<OrderDetailModel> orderList = new ArrayList<>();
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_reveivedorder_slice);

        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);

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
        initData();
        initClickListener();
        initListContainer();
        handleUI();
    }

    private void requirePermissions(String... permissions) {
        for (String permission: permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[] {permission}, MainAbility.REQUEST_CODE);
            }
        }
    }
    
    private void initView() {
        mBtnAcceptOrder=(Button) findComponentById(ResourceTable.Id_btn_accept_order);
        mBtnReadyToDeliver=(Button) findComponentById(ResourceTable.Id_btn_ready_delivery);
        listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container_cart);
    }
    
    private void initData() {
        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
        distributedNotificationPlugin.setEventListener(this);
        distributedNotificationPlugin.subscribeEvent();

        DeviceManager.registerDeviceStateCallback(iDeviceStateCallback);
    }

    private void initClickListener() {
        mBtnAcceptOrder.setClickedListener(component -> {
            if (!currentCustomerAction.isEmpty() && orderDetailModel != null) {
                mBtnAcceptOrder.setEnabled(false);
                startService();
            }
        });
        mBtnReadyToDeliver.setClickedListener(component -> {
            if (!currentCustomerAction.isEmpty() && orderDetailModel != null) {
                mBtnReadyToDeliver.setEnabled(false);
                startService();
            }
        });
    }
    
    private void initListContainer() {
        CommonFunctions.getInstance().showToast(ViewOrderDetailsAbilitySlice.this, String.valueOf(orderDetailModel.getListOfOrderItems().size()));
        getOrderDetails();
        sampleItemProvider = new FoodOrderedProvider(cartList, this);
        listContainer.setItemProvider(sampleItemProvider);
    }

    public void getOrderDetails() {
        if (orderDetailModel.getStatus().toString().contains(Constants.PAID)) {
            cartList = getCartList();
        } else if (orderDetailModel.getStatus().toString().contains(Constants.WAITING)
                && orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderPlaced() && !orderDetailModel.isOrderAccepted()) {
            cartList = getCurrentOrderList();
        } else if (orderDetailModel.getStatus().toString().contains(Constants.WAITING)
                && orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderAccepted() && !orderDetailModel.isOrderDelivered()) {
            cartList = getCurrentOrderNotDelieveredList();
        } else {
            cartList = getCartList();
        }
    }

    public ArrayList<FoodMenuItem> getCurrentOrderList(){
        ArrayList<FoodMenuItem> allCartList = new ArrayList<FoodMenuItem>();
        for (int i = 0; i < orderDetailModel.getListOfOrderItems().size(); i++) {
            if (orderDetailModel.getListOfOrderItems().get(i).getIsOrderConfirmed().contains("false")
            && orderDetailModel.getListOfOrderItems().get(i).getIsOrderDelievered().contains("false")) {
                allCartList.add(orderDetailModel.getListOfOrderItems().get(i));
            }
        }
        return allCartList;
    }

    public ArrayList<FoodMenuItem> getCurrentOrderNotDelieveredList(){
        ArrayList<FoodMenuItem> allCartList = new ArrayList<FoodMenuItem>();
        for (int i = 0; i < orderDetailModel.getListOfOrderItems().size(); i++) {
            if (orderDetailModel.getListOfOrderItems().get(i).getIsOrderConfirmed().contains("true")
                    && orderDetailModel.getListOfOrderItems().get(i).getIsOrderDelievered().contains("false")) {
                allCartList.add(orderDetailModel.getListOfOrderItems().get(i));
            }
        }
        return allCartList;
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
            } else {
                allCartList.add(orderDetailModel.getListOfOrderItems().get(i));
            }
        }
        return allCartList;
    }

    private void handleUI() {
        mBtnReadyToDeliver.setEnabled(true);
        mBtnAcceptOrder.setEnabled(true);

        if (!orderDetailModel.isMenuAccepted()) {
            mBtnReadyToDeliver.setVisibility(Component.HIDE);
            mBtnAcceptOrder.setVisibility(Component.VISIBLE);
            mBtnAcceptOrder.setText("Accept");
            currentCustomerAction = Constants.ACCEPTMENU;

            int orderID = CommonFunctions.getInstance().generateOrderNumber();
            orderDetailModel.setOrderID(orderID);
            orderDetailModel.setStatus(Constants.WAITING);
            orderDetailModel.setCommand(Constants.CUSTORDERNOTPLACED);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(false);
            orderDetailModel.setOrderAccepted(false);
            orderDetailModel.setOrderDelivered(false);
            orderDetailModel.setPaid(false);
        } else if (orderDetailModel.isMenuAccepted() && !orderDetailModel.isOrderPlaced()) {
            mBtnReadyToDeliver.setVisibility(Component.HIDE);
            mBtnAcceptOrder.setVisibility(Component.HIDE);
            currentCustomerAction = "";

            orderDetailModel.setStatus(Constants.WAITING);
            orderDetailModel.setCommand(Constants.CUSTORDERNOTPLACED);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(true);
            orderDetailModel.setOrderAccepted(false);
            orderDetailModel.setOrderDelivered(false);
            orderDetailModel.setPaid(false);
        } else if (orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderPlaced() && !orderDetailModel.isOrderAccepted()) {
            mBtnReadyToDeliver.setVisibility(Component.HIDE);
            mBtnAcceptOrder.setVisibility(Component.VISIBLE);
            mBtnAcceptOrder.setText("Accept");
            currentCustomerAction = Constants.ORDERCONFIRM;

            orderDetailModel.setStatus(Constants.WAITING);
            orderDetailModel.setCommand(Constants.ORDERCONFIRM);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(true);
            orderDetailModel.setOrderAccepted(true);
            orderDetailModel.setOrderDelivered(false);
            orderDetailModel.setPaid(false);
        } else if (orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderPlaced()
                && orderDetailModel.isOrderAccepted() && !orderDetailModel.isOrderDelivered()) {
            mBtnReadyToDeliver.setVisibility(Component.VISIBLE);
            mBtnAcceptOrder.setVisibility(Component.HIDE);
            mBtnReadyToDeliver.setText("Ready to Deliver");
            currentCustomerAction = Constants.FOODPRICE;

            orderDetailModel.setStatus(Constants.DELIEVERED);
            orderDetailModel.setCommand(Constants.FOODPRICE);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(true);
            orderDetailModel.setOrderAccepted(true);
            orderDetailModel.setOrderDelivered(true);
            orderDetailModel.setPaid(false);
        } else if (orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderPlaced() && orderDetailModel.isOrderAccepted()
                && orderDetailModel.isOrderDelivered() && !orderDetailModel.isPaid()) {
            mBtnReadyToDeliver.setVisibility(Component.HIDE);
            mBtnAcceptOrder.setVisibility(Component.VISIBLE);
            mBtnAcceptOrder.setText("Payment is pending");
            mBtnAcceptOrder.setEnabled(false);
            currentCustomerAction = "";

            orderDetailModel.setStatus(Constants.DELIEVERED);
            orderDetailModel.setCommand(Constants.FOODPRICE);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(true);
            orderDetailModel.setOrderAccepted(true);
            orderDetailModel.setOrderDelivered(true);
            orderDetailModel.setPaid(false);
        } else if (orderDetailModel.isMenuAccepted() && orderDetailModel.isOrderPlaced() && orderDetailModel.isOrderAccepted()
                & orderDetailModel.isOrderDelivered() && orderDetailModel.isPaid()) {
            mBtnReadyToDeliver.setVisibility(Component.HIDE);
            mBtnAcceptOrder.setVisibility(Component.VISIBLE);
            mBtnAcceptOrder.setText("Payment is received");
            mBtnAcceptOrder.setEnabled(false);
            currentCustomerAction = "";

            orderDetailModel.setStatus(Constants.DELIEVERED);
            orderDetailModel.setCommand(Constants.FOODPRICE);
            orderDetailModel.setMenuAccepted(true);
            orderDetailModel.setOrderPlaced(true);
            orderDetailModel.setOrderAccepted(true);
            orderDetailModel.setOrderDelivered(true);
            orderDetailModel.setPaid(true);
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
        connectToRemoteService();
    }
    private void connectToRemoteService() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId(orderDetailModel.getDeviceID())
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
                CommonFunctions.getInstance().showToast(this,"Cannot connect service on phone");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private IAbilityConnection iAbilityConnection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            LogUtil.info(TAG_LOG, "ability connect done!");
            shopKeeperDeviceId = KvManagerFactory.getInstance()
                    .createKvManager(new KvManagerConfig(ViewOrderDetailsAbilitySlice.this))
                    .getLocalDeviceInfo()
                    .getId();
            LogUtil.info(TAG_LOG, "Shop keeper device ID: "+shopKeeperDeviceId);

            sendDataToWatchRemoteProxy = new SendRequestRemote(iRemoteObject, shopKeeperDeviceId);

            sendResponseRemote();
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            LogUtil.info(TAG_LOG, "ability disconnect done!");
            disconnectAbility(iAbilityConnection);
        }
    };
    
    private IDeviceStateCallback iDeviceStateCallback = new IDeviceStateCallback() {
        @Override
        public void onDeviceOffline(String s, int i) {
            CommonFunctions.getInstance().showToast(ViewOrderDetailsAbilitySlice.this,"Device offline");
        }

        @Override
        public void onDeviceOnline(String s, int i) {
            if (!currentCustomerAction.isEmpty() && orderDetailModel != null) {
                startService();
            }
        }
    };

    private void sendResponseRemote() {
        if (currentCustomerAction != null && !currentCustomerAction.isEmpty()
                && shopKeeperDeviceId != null && !shopKeeperDeviceId.isEmpty()) {
            if (sendDataToWatchRemoteProxy != null) {
                sendDataToWatchRemoteProxy.remoteControl(currentCustomerAction, shopKeeperDeviceId, orderDetailModel);
            }
        }
        disconnectAbility(iAbilityConnection);
        handleUI();
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private void updatingOrderList(int orderID) {
        if (Constants.getInstance().getOrderDetailsList() != null
                && Constants.getInstance().getOrderDetailsList().size() >= 1) {
            for (int i=0;i<Constants.getInstance().getOrderDetailsList().size();i++) {
                int compOrderID = Constants.getInstance().getOrderDetailsList().get(i).getOrderID();
                if (orderID == compOrderID) {
                    OrderDetailModel tempOrderDetailModel = Constants.getInstance().getOrderDetailsList().get(i);
                    orderDetailModel = tempOrderDetailModel;
                    Constants.getInstance().getOrderDetailsList().set(currentPosition, orderDetailModel);
                    break;
                }
            }
        }
    }

    @Override
    public void onEventSubscribe(String result) {

    }

    @Override
    public void onEventPublish(String result) {

    }

    @Override
    public void onEventUnSubscribe(String result) {

    }

    @Override
    public void onEventReceive(String result, int orderId) {
        LogUtil.debug(TAG_LOG, "onEventReceiveConectingMenu " + result);
        switch (result) {
            case Constants.REQUEST_MENU:
                updatingOrderList(orderId);
                handleUI();
                CommonFunctions.getInstance().showToast(this, "User requesting menu");
                break;
            case Constants.ACCEPTMENU:
                updatingOrderList(orderId);
                handleUI();
                CommonFunctions.getInstance().showToast(this, "New order received");
                break;
            case Constants.PAYMENTRECEIVED:
                updatingOrderList(orderId);
                handleUI();
                CommonFunctions.getInstance().showToast(this, "Payment received");
                break;
            case Constants.PENDING:
                CommonFunctions.getInstance().showToast(this, "User requesting menu");
                break;
            case Constants.WAITING:
                CommonFunctions.getInstance().showToast(this, "New order received");
                break;
            case Constants.DELIEVERED:
                CommonFunctions.getInstance().showToast(this, "Payment received");
                break;
            default:
                break;
        }
    }
}
