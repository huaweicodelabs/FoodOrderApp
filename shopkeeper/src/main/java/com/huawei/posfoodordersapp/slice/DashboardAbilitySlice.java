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
import com.huawei.posfoodordersapp.controller.DistributedNotificationPlugin;
import com.huawei.posfoodordersapp.controller.SendRequestRemote;
import com.huawei.posfoodordersapp.interfaces.Adapterlistener;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.provider.CustomerOrderDetailsProvider;
import com.huawei.posfoodordersapp.service.DeviceService;
import com.huawei.posfoodordersapp.utils.LogUtil;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.utils.Color;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.IBundleManager;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.distributedschedule.interwork.DeviceManager;
import ohos.distributedschedule.interwork.IDeviceStateCallback;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.agp.components.ListContainer;
import static com.huawei.posfoodordersapp.utils.LogUtil.TAG_LOG;
import static com.huawei.posfoodordersapp.utils.Constants.PAYMENT_RESULT_SLICE;
import static com.huawei.posfoodordersapp.utils.Constants.VIEW_ORDER_SLICE;

public class DashboardAbilitySlice extends AbilitySlice implements Adapterlistener, DistributedNotificationPlugin.DistributedNotificationEventListener {

    private DirectionalLayout layout_pending;
    private DirectionalLayout layout_waiting;
    private DirectionalLayout layout_delivered;
    private Button pending_button;
    private Button waiting_button;
    private Button delivered_button;
    private Text pending_count_text;
    private Text waiting_count_text;
    private Text delivered_count_text;
    private ListContainer dashboard_list_container;
    private CustomerOrderDetailsProvider orderDetailsListAdapter;
    private ArrayList<OrderDetailModel> orderDetailArrayList;
    private String clickedItem = Constants.PENDING;
    private String currentCustomerAction = "";
    private String currentCustomerDeviceId = "";
    private String shopKeeperDeviceId = "";
    private OrderDetailModel currentOrderDetailsModel = new OrderDetailModel();
    private DistributedNotificationPlugin distributedNotificationPlugin;
    private SendRequestRemote sendDataToWatchRemoteProxy;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_dashboard_slice);

        if (intent != null) {
            if (intent.hasParameter("DeviceId")) {
                Object obj = intent.getParams().getParam("DeviceId");
                if (obj instanceof String) {
                    String deviceId = (String) obj;
                    currentCustomerDeviceId = deviceId;
                    LogUtil.debug(TAG_LOG,"Received deviceId from Intent: "+currentCustomerDeviceId);
                }
            }
        }

        initView();
        initClickListener();
    }

    private void initView() {
        layout_pending = (DirectionalLayout) findComponentById(ResourceTable.Id_layout_pending);
        layout_waiting = (DirectionalLayout) findComponentById(ResourceTable.Id_layout_waiting);
        layout_delivered = (DirectionalLayout) findComponentById(ResourceTable.Id_layout_delivered);

        pending_button = (Button) findComponentById(ResourceTable.Id_pending_button);
        waiting_button = (Button) findComponentById(ResourceTable.Id_waiting_button);
        delivered_button = (Button) findComponentById(ResourceTable.Id_delivered_button);

        pending_count_text = (Text) findComponentById(ResourceTable.Id_pending_count_text);
        waiting_count_text = (Text) findComponentById(ResourceTable.Id_waiting_count_text);
        delivered_count_text = (Text) findComponentById(ResourceTable.Id_delivered_count_text);

        dashboard_list_container = (ListContainer) findComponentById(ResourceTable.Id_dashboard_list_container);
    }

    private void initData() {
        distributedNotificationPlugin = DistributedNotificationPlugin.getInstance();
        distributedNotificationPlugin.setEventListener(this);
        distributedNotificationPlugin.subscribeEvent();

        DeviceManager.registerDeviceStateCallback(iDeviceStateCallback);
        clickedItem = Constants.getInstance().currentSelectedTab;
        updatingOrderList(clickedItem);
        setCountValue();
    }

    @Override
    protected void onActive() {
        super.onActive();

        initData();
    }

    private void setCountValue() {
        int pendingCount = filterStatus(Constants.PENDING);
        pending_count_text.setText(String.valueOf(pendingCount));

        int waitingCount = filterStatus(Constants.WAITING);
        waiting_count_text.setText(String.valueOf(waitingCount));

        int deliveredCount = filterStatus(Constants.DELIEVERED);
        delivered_count_text.setText(String.valueOf(deliveredCount));
    }

    private int filterStatus(String status) {
        ArrayList<OrderDetailModel> orderDetailModels = new ArrayList<>();
        if (orderDetailArrayList != null && orderDetailArrayList.size() >= 1) {
            for (int i=0;i<orderDetailArrayList.size();i++) {
                if (status.contains(Constants.DELIEVERED)) {
                    if (orderDetailArrayList.get(i).getStatus().contains(Constants.PAID)
                    || orderDetailArrayList.get(i).getStatus().contains(Constants.DELIEVERED)) {
                        orderDetailModels.add(orderDetailArrayList.get(i));
                    }
                } else {
                    if (orderDetailArrayList.get(i).getStatus().toString().contains(status)) {
                        orderDetailModels.add(orderDetailArrayList.get(i));
                    }
                }
            }
        }
        return orderDetailModels.size();
    }
    private void initClickListener() {
        layout_pending.setClickedListener(component -> {
            clickedItem = Constants.PENDING;
            orderDetailsListAdapter.getFilter().filter(Constants.PENDING);
            changeTabView();
        });

        layout_waiting.setClickedListener(component -> {
            clickedItem = Constants.WAITING;
            orderDetailsListAdapter.getFilter().filter(Constants.WAITING);
            changeTabView();
        });

        layout_delivered.setClickedListener(component -> {
            clickedItem = Constants.DELIEVERED;
            orderDetailsListAdapter.getFilter().filter(Constants.DELIEVERED);
            changeTabView();
        });
    }

    private void changeTabView() {
        if (clickedItem.contains(Constants.PENDING)) {
            pending_button.setTextColor(Color.WHITE);
            pending_count_text.setTextColor(Color.WHITE);

            waiting_button.setTextColor(Color.BLACK);
            waiting_count_text.setTextColor(Color.BLACK);

            delivered_button.setTextColor(Color.BLACK);
            delivered_count_text.setTextColor(Color.BLACK);
        } else if (clickedItem.contains(Constants.WAITING)) {
            pending_button.setTextColor(Color.BLACK);
            pending_count_text.setTextColor(Color.BLACK);

            waiting_button.setTextColor(Color.WHITE);
            waiting_count_text.setTextColor(Color.WHITE);

            delivered_button.setTextColor(Color.BLACK);
            delivered_count_text.setTextColor(Color.BLACK);
        } else if (clickedItem.contains(Constants.DELIEVERED)) {
            pending_button.setTextColor(Color.BLACK);
            pending_count_text.setTextColor(Color.BLACK);

            waiting_button.setTextColor(Color.BLACK);
            waiting_count_text.setTextColor(Color.BLACK);

            delivered_button.setTextColor(Color.WHITE);
            delivered_count_text.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DeviceManager.unregisterDeviceStateCallback(iDeviceStateCallback);
    }

    @Override
    public void onItemClicked(OrderDetailModel orderItemModel, int position) {
        currentCustomerDeviceId = orderItemModel.getDeviceID();
        CommonFunctions.getInstance().showToast(this, "Item Clicked");
    }

    @Override
    public void onItemPositiveClicked(OrderDetailModel orderItemModel, int position) {
        handlePositiveClickedItem(orderItemModel, position);
    }

    @Override
    public void onItemNegativeClicked(OrderDetailModel orderItemModel, int position) {
        handleNegativeClickedItem(orderItemModel, position);
    }

    @Override
    public void onItemViewClicked(OrderDetailModel orderItemModel, int position) {
        currentCustomerDeviceId = orderItemModel.getDeviceID();
        CommonFunctions.getInstance().showToast(this, "View Clicked");

        openReceivedOrderAbilitySlice(orderItemModel, position);
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
            case Constants.ACKNOWLEDGE:
                CommonFunctions.getInstance().showToast(this, "Acknowledgement received");
                break;
            case Constants.REQUEST_MENU:
                updatingOrderList(Constants.PENDING);
                CommonFunctions.getInstance().showToast(this, "User requesting menu");
                break;
            case Constants.ACCEPTMENU:
                updatingOrderList(Constants.WAITING);
                CommonFunctions.getInstance().showToast(this, "New order received");
                break;
            case Constants.PAYMENTRECEIVED:
                updatingOrderList(Constants.DELIEVERED);
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

    private void updatingOrderList(String status) {
        orderDetailArrayList = new ArrayList<>();
        if (Constants.getInstance().getOrderDetailsList() != null
                && Constants.getInstance().getOrderDetailsList().size() >= 1) {
            orderDetailArrayList = Constants.getInstance().getOrderDetailsList();
        }
        orderDetailsListAdapter = new CustomerOrderDetailsProvider(getContext(), orderDetailArrayList, this);
        dashboard_list_container.setItemProvider(orderDetailsListAdapter);

        if (status.contains(Constants.PENDING)) {
            orderDetailsListAdapter.getFilter().filter(Constants.PENDING);
            int pendingCount = filterStatus(Constants.PENDING);
            pending_count_text.setText(String.valueOf(pendingCount));
            clickedItem = Constants.PENDING;
            changeTabView();
        } else if (status.contains(Constants.WAITING)) {
            orderDetailsListAdapter.getFilter().filter(Constants.WAITING);
            int waitingCount = filterStatus(Constants.WAITING);
            waiting_count_text.setText(String.valueOf(waitingCount));
            clickedItem = Constants.WAITING;
            changeTabView();
        } else if (status.contains(Constants.DELIEVERED)) {
            orderDetailsListAdapter.getFilter().filter(Constants.DELIEVERED);
            int deliveredCount = filterStatus(Constants.DELIEVERED);
            delivered_count_text.setText(String.valueOf(deliveredCount));
            clickedItem = Constants.DELIEVERED;
            changeTabView();
        }
        orderDetailsListAdapter.notifyDataChanged();
    }

    private void startService(String custRemoteDeviceID) {
        LogUtil.debug(TAG_LOG,"ShopkeeperService - Calling");
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(getBundleName())
                .withAbilityName(DeviceService.class.getName())
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);
        startAbility(intent);
        LogUtil.debug(TAG_LOG,"ShopkeeperService - startAbility");

        connectToRemoteService(custRemoteDeviceID);
    }

    private void connectToRemoteService(String custRemoteDeviceID) {
        LogUtil.info(TAG_LOG, "connectToRemoteService - from shopkeeper start");
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId(custRemoteDeviceID)
                .withBundleName(Constants.PACKAGE_NAME)
                .withAbilityName(Constants.ACK_RESPONSE_SERVICEABILITY)
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);
        try {
            List<AbilityInfo> abilityInfos = getBundleManager().queryAbilityByIntent(intent, IBundleManager.GET_BUNDLE_DEFAULT, 0);
            if (abilityInfos != null && !abilityInfos.isEmpty()) {
                connectAbility(intent, iAbilityConnection);
                LogUtil.info(TAG_LOG, "connectToRemoteService - from shopkeeper connectAbility");
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
                    .createKvManager(new KvManagerConfig(DashboardAbilitySlice.this))
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
        public void onDeviceOffline(String deviceId, int deviceType) {
            CommonFunctions.getInstance().showToast(DashboardAbilitySlice.this,"Device offline");
        }

        @Override
        public void onDeviceOnline(String deviceId, int deviceType) {
            startService(deviceId);
        }
    };

    private void sendResponseRemote() {
        if (currentCustomerAction != null && !currentCustomerAction.isEmpty() 
                && shopKeeperDeviceId != null && !shopKeeperDeviceId.isEmpty()) {
            if (sendDataToWatchRemoteProxy != null) {
                sendDataToWatchRemoteProxy.remoteControl(currentCustomerAction, shopKeeperDeviceId, currentOrderDetailsModel);
            }
        }
        disconnectAbility(iAbilityConnection);
    }

    private void handleNegativeClickedItem(OrderDetailModel orderItemModel, int position) {
        currentCustomerDeviceId = orderItemModel.getDeviceID();
        CommonFunctions.getInstance().showToast(this, "Negative Clicked");

        if (orderItemModel.getStatus().toString().contains(Constants.PENDING)
                && !orderItemModel.isMenuAccepted()) {
            int orderID = CommonFunctions.getInstance().generateOrderNumber();
            orderItemModel.setOrderID(orderID);
            orderItemModel.setStatus(Constants.PENDING);
            orderItemModel.setCommand(Constants.MENU_CANCELED);
            orderItemModel.setMenuAccepted(false);
            orderItemModel.setOrderPlaced(false);
            orderItemModel.setOrderAccepted(false);
            orderItemModel.setOrderDelivered(false);
            orderItemModel.setPaid(false);
            clickedItem = Constants.PENDING;

            currentCustomerAction = Constants.MENU_CANCELED;

            currentOrderDetailsModel = orderItemModel;
            orderDetailArrayList.set(position, orderItemModel);
            orderDetailsListAdapter.notifyDataChanged();
            setCountValue();
            changeTabView();
            currentCustomerDeviceId = orderItemModel.getDeviceID();

            startService(currentCustomerDeviceId);
        } else if (orderItemModel.getStatus().toString().contains(Constants.WAITING)
                && orderItemModel.isMenuAccepted() && orderItemModel.isOrderPlaced() && !orderItemModel.isOrderAccepted()) {
            orderItemModel.setStatus(Constants.WAITING);
            orderItemModel.setCommand(Constants.ORDER_CANCELED);
            orderItemModel.setMenuAccepted(true);
            orderItemModel.setOrderPlaced(true);
            orderItemModel.setOrderAccepted(false);
            orderItemModel.setOrderDelivered(false);
            orderItemModel.setPaid(false);
            clickedItem = Constants.WAITING;

            currentCustomerAction = Constants.ORDER_CANCELED;

            currentOrderDetailsModel = orderItemModel;
            orderDetailArrayList.set(position, orderItemModel);
            orderDetailsListAdapter.notifyDataChanged();
            setCountValue();
            changeTabView();
            currentCustomerDeviceId = orderItemModel.getDeviceID();

            startService(currentCustomerDeviceId);
        }
    }
    private void handlePositiveClickedItem(OrderDetailModel orderItemModel, int position) {
        if (orderItemModel.getStatus().toString().contains(Constants.PAID)
                && orderItemModel.isPaid()) {
            openPaymentReceivedAbilitySlice(orderItemModel, position);
        } else {
            if (orderItemModel.getStatus().toString().contains(Constants.PENDING)) {
                int orderID = CommonFunctions.getInstance().generateOrderNumber();
                orderItemModel.setOrderID(orderID);
                orderItemModel.setStatus(Constants.WAITING);
                orderItemModel.setCommand(Constants.CUSTORDERNOTPLACED);
                orderItemModel.setMenuAccepted(true);
                orderItemModel.setOrderPlaced(false);
                orderItemModel.setOrderAccepted(false);
                orderItemModel.setOrderDelivered(false);
                orderItemModel.setPaid(false);
                clickedItem = Constants.WAITING;
                currentCustomerAction = Constants.ACCEPTMENU;
            } else if (orderItemModel.getStatus().toString().contains(Constants.WAITING)
                    && orderItemModel.isMenuAccepted() && !orderItemModel.isOrderPlaced()) {
                orderItemModel.setStatus(Constants.WAITING);
                orderItemModel.setCommand(Constants.CUSTORDERNOTPLACED);
                orderItemModel.setMenuAccepted(true);
                orderItemModel.setOrderPlaced(true);
                orderItemModel.setOrderAccepted(false);
                orderItemModel.setOrderDelivered(false);
                orderItemModel.setPaid(false);
                clickedItem = Constants.WAITING;
                currentCustomerAction = Constants.CUSTORDERNOTPLACED;
            } else if (orderItemModel.getStatus().toString().contains(Constants.WAITING)
                    && orderItemModel.isMenuAccepted() && orderItemModel.isOrderPlaced() && !orderItemModel.isOrderAccepted()) {
                orderItemModel.setStatus(Constants.WAITING);
                orderItemModel.setCommand(Constants.ORDERCONFIRM);
                orderItemModel.setMenuAccepted(true);
                orderItemModel.setOrderPlaced(true);
                orderItemModel.setOrderAccepted(true);
                for (int i=0;i<orderItemModel.getListOfOrderItems().size();i++) {
                    FoodMenuItem foodMenuItem = orderItemModel.getListOfOrderItems().get(i);
                    if (foodMenuItem.getIsOrderConfirmed().toString().contains("false")) {
                        foodMenuItem.setIsOrderConfirmed("true");
                        foodMenuItem.setIsOrderDelievered("false");
                        orderItemModel.getListOfOrderItems().set(i,foodMenuItem);
                    }
                }
                orderItemModel.setOrderDelivered(false);
                orderItemModel.setPaid(false);
                clickedItem = Constants.WAITING;
                currentCustomerAction = Constants.ORDERCONFIRM;
            } else if (orderItemModel.getStatus().toString().contains(Constants.WAITING)
                    && orderItemModel.isMenuAccepted() && orderItemModel.isOrderAccepted() && !orderItemModel.isOrderDelivered()) {
                orderItemModel.setStatus(Constants.DELIEVERED);
                orderItemModel.setCommand(Constants.FOODPRICE);
                orderItemModel.setMenuAccepted(true);
                orderItemModel.setOrderPlaced(true);
                orderItemModel.setOrderAccepted(true);
                for (int i=0;i<orderItemModel.getListOfOrderItems().size();i++) {
                    FoodMenuItem foodMenuItem = orderItemModel.getListOfOrderItems().get(i);
                    if (foodMenuItem.getIsOrderConfirmed().toString().contains("true")) {
                        foodMenuItem.setIsOrderConfirmed("true");
                        foodMenuItem.setIsOrderDelievered("true");
                        orderItemModel.getListOfOrderItems().set(i,foodMenuItem);
                    }
                }
                orderItemModel.setOrderDelivered(true);
                orderItemModel.setPaid(false);
                clickedItem = Constants.DELIEVERED;
                currentCustomerAction = Constants.FOODPRICE;
            }

            currentOrderDetailsModel = orderItemModel;

            for (int i=0;i<orderDetailArrayList.size();i++) {
                if (orderDetailArrayList.get(i).getOrderID() != 0 &&
                        orderDetailArrayList.get(i).getOrderID() == orderItemModel.getOrderID()) {
                    orderDetailArrayList.set(i,orderItemModel);
                    break;
                }
            }
            orderDetailsListAdapter.notifyDataChanged();
            setCountValue();
            changeTabView();

            currentCustomerDeviceId = orderItemModel.getDeviceID();

            startService(currentCustomerDeviceId);
        }

        CommonFunctions.getInstance().showToast(this, "Positive Clicked");
    }

    private void openReceivedOrderAbilitySlice(OrderDetailModel orderItemModel, int position) {
        Constants.getInstance().currentSelectedTab = clickedItem;
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(Constants.PACKAGE_NAME)
                .withAbilityName(Constants.MAIN_ABILITY)
                .withAction(VIEW_ORDER_SLICE)
                .build();
        intent.setOperation(operation);
        intent.setParam("OrderDetails", orderItemModel);
        intent.setParam("Position", position);
        startAbility(intent);
    }

    private void openPaymentReceivedAbilitySlice(OrderDetailModel orderItemModel, int position) {
        Constants.getInstance().currentSelectedTab = clickedItem;
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(Constants.PACKAGE_NAME)
                .withAbilityName(Constants.MAIN_ABILITY)
                .withAction(PAYMENT_RESULT_SLICE)
                .build();
        intent.setOperation(operation);
        intent.setParam("OrderDetails",orderItemModel);
        intent.setParam("Position", position);
        startAbility(intent);
    }
}
