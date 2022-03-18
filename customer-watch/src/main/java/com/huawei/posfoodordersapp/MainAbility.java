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

package com.huawei.posfoodordersapp;

import com.huawei.posfoodordersapp.slice.CustomerDetailsAbilitySlice;
import com.huawei.posfoodordersapp.slice.FoodMenuAbilitySlice;
import com.huawei.posfoodordersapp.slice.ConfirmOrderAbilitySlice;
import com.huawei.posfoodordersapp.slice.OrderProcessAbilitySlice;
import com.huawei.posfoodordersapp.slice.FoodPaymentDetailsAbilitySlice;
import com.huawei.posfoodordersapp.slice.PaymentResultAbilitySlice;

import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.Constants;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;

import static com.huawei.posfoodordersapp.utils.Constants.TIMER_MILLISECOND;

public class MainAbility extends Ability {
    public static final int REQUEST_CODE = 1;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(CustomerDetailsAbilitySlice.class.getName());

        initActionRoute();
    }

    private void initActionRoute() {
        addActionRoute(Constants.CALL_CUSTOMER_DETAILS_ACTION, CustomerDetailsAbilitySlice.class.getName());
        addActionRoute(Constants.CALL_FOODMENUITEM_ACTION, FoodMenuAbilitySlice.class.getName());
        addActionRoute(Constants.CALL_FOODORDERYSLICE_ACTION, ConfirmOrderAbilitySlice.class.getName());
        addActionRoute(Constants.CALL_ORDERCONFIRMATON_WITHTIMER_ACTION, OrderProcessAbilitySlice.class.getName());
        addActionRoute(Constants.CALL_FOOD_PAYMENT_DETAILS_ACTION, FoodPaymentDetailsAbilitySlice.class.getName());
        addActionRoute(Constants.CALL_PAYMENT_SLICE, PaymentResultAbilitySlice.class.getName());
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
                LogUtil.debug(LogUtil.TAG_LOG, "Permission granted");
            } else {
                CommonFunctions.getInstance().showToast(this,"Permission is required to proceed");
                getMainTaskDispatcher().delayDispatch(this::terminateAbility, TIMER_MILLISECOND);
            }
        }
    }
}
