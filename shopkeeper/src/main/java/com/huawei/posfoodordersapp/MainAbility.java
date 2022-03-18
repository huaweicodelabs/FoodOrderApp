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

import com.huawei.posfoodordersapp.model.GrantPermissionModel;
import com.huawei.posfoodordersapp.slice.SplashAbilitySlice;
import com.huawei.posfoodordersapp.slice.DashboardAbilitySlice;
import com.huawei.posfoodordersapp.slice.ViewOrderDetailsAbilitySlice;
import com.huawei.posfoodordersapp.slice.PaymentResultAbilitySlice;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import org.greenrobot.eventbus.EventBus;

public class MainAbility extends Ability {
    public static final int REQUEST_CODE = 1;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(SplashAbilitySlice.class.getName());
        initActionRoute();
    }

    private void initActionRoute() {
        addActionRoute(Constants.DASHBOARD_SLICE, DashboardAbilitySlice.class.getName());
        addActionRoute(Constants.VIEW_ORDER_SLICE, ViewOrderDetailsAbilitySlice.class.getName());
        addActionRoute(Constants.PAYMENT_RESULT_SLICE, PaymentResultAbilitySlice.class.getName());
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            for (int i=0;i<permissions.length;i++) {
                EventBus.getDefault().post(new GrantPermissionModel(permissions[i],grantResults[i] == IBundleManager.PERMISSION_GRANTED));
            }
        }
    }
}
