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
import com.huawei.posfoodordersapp.model.OrderDetailModel;
/**
 * The type Constants.
 */
public class Constants {
    /**
     * The constant API_BASE_URL.
     */
    public static final String API_BASE_URL = "YourUrl";
    /**
     * The constant START.
     */
    public static final String START = "start";
    /**
     * The constant FINISH.
     */
    public static final String FINISH = "finish";
    /**
     * The constant lottieanim_cooking.
     */
    public static final String LOTTIANIM_COOKING = "YourAnimationUrl";
    /**
     * The constant lottieanim_success.
     */
    public static final String LOTTIANIM_SUCCESS = "YourAnimationUrl";
    /**
     * The constant CALL_CUSTOMER_DETAILS_ACTION.
     */
    public static final String CALL_CUSTOMER_DETAILS_ACTION = "action.customerdetails.slice";
    /**
     * The constant CALL_FOODMENUITEM_ACTION.
     */
    public static final String CALL_FOODMENUITEM_ACTION = "action.foodmenuitem.slice";
    /**
     * The constant CALL_FOODORDERYSLICE_ACTION.
     */
    public static final String CALL_FOODORDERYSLICE_ACTION = "action.foodorder.slice";
    /**
     * The constant CALL_ORDERCONFIRMATON_WITHTIMER_ACTION.
     */
    public static final String CALL_ORDERCONFIRMATON_WITHTIMER_ACTION = "action.orderconfirmationwithtier.slice";
    /**
     * The constant CALL_FOOD_PAYMENT_DETAILS_ACTION.
     */
    public static final String CALL_FOOD_PAYMENT_DETAILS_ACTION = "action.foodpaymentdetails.slice";
    /**
     * The constant CALL_PAYMENT_SLICE.
     */
    public static final String CALL_PAYMENT_SLICE = "action.payment.slice";
    /**
     * The constant TIMER_MILLISECOND.
     */
    public static final int TIMER_MILLISECOND = 3000;
    /**
     * The constant CONFIRMORDER.
     */
    public static final String CONFIRMORDER = "confirmorder";
    /**
     * The constant PAYMENTRECEIVED.
     */
    public static final String PAYMENTRECEIVED = "paymentreceived";
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
     * The constant ORDER_CANCELED.
     */
    public static final String ORDER_CANCELED = "order_canceled";
    /**
     * The constant PAID.
     */
    public static final String PAID = "paid";
    /**
     * The constant NOT_PAID.
     */
    public static final String NOT_PAID = "not_paid";
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
     * The constant PENDING.
     */
    public static final String PENDING = "pending";
    /**
     * The constant WAITING.
     */
    public static final String WAITING = "waiting";
    /**
     * The constant PACKAGE_NAME.
     */
    public static final String PACKAGE_NAME = "com.huawei.posfoodordersapp";
    /**
     * The constant MAIN_ABILITY.
     */
    public static final String MAIN_ABILITY = "com.huawei.posfoodordersapp.MainAbility";
    private static volatile Constants sInstance = null;
    /**
     * The constant foodType.
     */
    public static String foodType = "";
    /**
     * The constant ACK_RESPONSE_SERVICEABILITY.
     */
    public static final String ACK_RESPONSE_SERVICEABILITY = "com.huawei.posfoodordersapp.service.DeviceService";
    private OrderDetailModel orderDetailModel = new OrderDetailModel();
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
     * Gets order detail model.
     *
     * @return the order detail model
     */
    public OrderDetailModel getOrderDetailModel() {
        return orderDetailModel;
    }
    /**
     * Sets order detail model.
     *
     * @param orderDetailModel the order detail model
     */
    public void setOrderDetailModel(OrderDetailModel orderDetailModel) {
        this.orderDetailModel = orderDetailModel;
    }
}
