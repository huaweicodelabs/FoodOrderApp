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

package com.huawei.posfoodordersapp.utils;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.model.OrderDetailModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Constants.
 */
public class Constants {
    /**
     * The constant API_BASE_URL.
     */
    public static final String API_BASE_URL = "YourUrl";
    /**
     * The constant lottieanim_splash.
     */
    public static final String LOTTIANIM_SPLASH="YourAnimationUrl";
    /**
     * The constant DEVICE_ID_KEY.
     */
    public static final String DEVICE_ID_KEY = "deviceId";
    /**
     * The constant START.
     */
    public static final String START = "start";
    /**
     * The constant CALL.
     */
    public static final String CALL = "call";
    /**
     * The constant FINISH.
     */
    public static final String FINISH = "finish";
    /**
     * The constant REQUEST_MENU.
     */
    public static final String REQUEST_MENU = "requesting_menu";
    /**
     * The constant MENU_ACCEPTED.
     */
    public static final String MENU_ACCEPTED = "menu_accepted";
    /**
     * The constant MENU_CANCELED.
     */
    public static final String MENU_CANCELED = "menu_canceled";
    /**
     * The constant ORDER_ACCEPTED.
     */
    public static final String ORDER_ACCEPTED = "order_accepted";
    /**
     * The constant ORDER_CANCELED.
     */
    public static final String ORDER_CANCELED = "order_canceled";
    /**
     * The constant ORDER_PREPARING.
     */
    public static final String ORDER_PREPARING = "order_preparing";
    /**
     * The constant ORDER_READY.
     */
    public static final String ORDER_READY = "order_ready";
    /**
     * The constant ORDER_DELAY.
     */
    public static final String ORDER_DELAY = "order_delay";
    /**
     * The constant PAID.
     */
    public static final String PAID = "paid";
    /**
     * The constant NOT_PAID.
     */
    public static final String NOT_PAID = "not_paid";
    /**
     * The constant PENDING.
     */
    public static final String PENDING = "pending";
    /**
     * The constant WAITING.
     */
    public static final String WAITING = "waiting";
    /**
     * The constant DELIEVERED.
     */
    public static final String DELIEVERED = "delievered";
    /**
     * The constant ACCEPTMENU.
     */
    public static final String ACCEPTMENU = "acceptMenu";
    /**
     * The constant CUSTORDERNOTPLACED.
     */
    public static final String CUSTORDERNOTPLACED = "custordernotplaced";
    /**
     * The constant ORDERCONFIRM.
     */
    public static final String ORDERCONFIRM = "orderconfirm";
    /**
     * The constant FOODPRICE.
     */
    public static final String FOODPRICE = "foodprice";
    /**
     * The constant TIMER_MILLISECOND.
     */
    public static final int TIMER_MILLISECOND = 3000;
    /**
     * The constant TOAST_DURATION_MILLISECOND.
     */
    public static final int TOAST_DURATION_MILLISECOND = 2000;
    /**
     * The constant PACKAGE_NAME.
     */
    public static final String PACKAGE_NAME = "com.huawei.posfoodordersapp";
    /**
     * The constant MAIN_ABILITY.
     */
    public static final String MAIN_ABILITY = "com.huawei.posfoodordersapp.MainAbility";
    /**
     * The constant SERVICE_ABILITY_NAME.
     */
    public static final String SERVICE_ABILITY_NAME = "com.huawei.posfoodordersapp.service.DeviceService";
    /**
     * The constant DASHBOARD_SLICE.
     */
    public static final String DASHBOARD_SLICE = "action.home.dashboard";
    /**
     * The constant VIEW_ORDER_SLICE.
     */
    public static final String VIEW_ORDER_SLICE = "action.vieworderdetails.slice";
    /**
     * The constant PAYMENT_RESULT_SLICE.
     */
    public static final String PAYMENT_RESULT_SLICE =  "action.paymentresult.slice";
    private static volatile Constants sInstance = null;
    /**
     * The constant ACKNOWLEDGE.
     */
    public static final String ACKNOWLEDGE = "acknowledge";
    /**
     * The constant CONFIRMORDER.
     */
    public static final String CONFIRMORDER = "confirmorder";
    /**
     * The constant PAYMENTRECEIVED.
     */
    public static final String PAYMENTRECEIVED = "paymentreceived";
    /**
     * The constant PAYMENT_FAILED.
     */
    public static final String PAYMENT_FAILED = "payment_failed";
    /**
     * The constant list.
     */
    public static List<FoodMenuItem> list=new ArrayList<>();
    /**
     * The constant ACK_RESPONSE_SERVICEABILITY.
     */
    public static final String ACK_RESPONSE_SERVICEABILITY = "com.huawei.posfoodordersapp.service.DeviceService";
    private ArrayList<OrderDetailModel> orderDetailsList = new ArrayList<>();
    /**
     * The Current selected tab.
     */
    public String currentSelectedTab = Constants.PENDING;
    /**
     * Initialize Instance.
     *
     * @return the instance
     */
    public static Constants getInstance() {
        if (sInstance == null) {
            synchronized (Constants.class) {
                if (sInstance == null) {
                    sInstance = new Constants();
                }
            }
        }
        return sInstance;
    }
    /**
     * Gets order details list.
     *
     * @return the order details list
     */
    public ArrayList<OrderDetailModel> getOrderDetailsList() {
        return orderDetailsList;
    }
    /**
     * Sets order details list.
     *
     * @param orderDetailsList the order details list
     */
    public void setOrderDetailsList(ArrayList<OrderDetailModel> orderDetailsList) {
        this.orderDetailsList = orderDetailsList;
    }
}
