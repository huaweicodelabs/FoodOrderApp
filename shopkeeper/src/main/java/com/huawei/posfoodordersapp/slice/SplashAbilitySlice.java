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
import com.huawei.posfoodordersapp.model.GrantPermissionModel;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import ohos.security.SystemPermission;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import static com.huawei.posfoodordersapp.utils.Constants.TIMER_MILLISECOND;

/**
 * SplashAbilitySlice ability slice part of Main ability.
 * Showing splash screen.
 */
public class SplashAbilitySlice extends AbilitySlice {

    private LottieAnimationView lv_animationView_Splash;
    private LottieAnimationViewData data_animationView_Splash;


    /**
     * Abilityslice lifecycle method onStart.
     * @param intent as Intent
     */
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_splash);

        EventBus.getDefault().register(this);
        requirePermissions(SystemPermission.DISTRIBUTED_DATASYNC);

        lv_animationView_Splash = (LottieAnimationView)findComponentById(ResourceTable.Id_animationView_Splash);
        data_animationView_Splash = new LottieAnimationViewData();
        data_animationView_Splash.setUrl(Constants.LOTTIANIM_SPLASH);
        data_animationView_Splash.autoPlay = true;
        data_animationView_Splash.setRepeatCount(1000); // specify repetition count
        lv_animationView_Splash.setAnimationData(data_animationView_Splash);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrantedPermissionModel(GrantPermissionModel grantPermissionModel) {
        if (grantPermissionModel.permission.equals(SystemPermission.DISTRIBUTED_DATASYNC)
                && grantPermissionModel.isGranted) {
            navigateToOnlineDeviceScreen();
        } else {
            System.exit(0);
        }
    }

    private void requirePermissions(String... permissions) {
        for (String permission: permissions) {
            if (verifyCallingOrSelfPermission(permission) != IBundleManager.PERMISSION_GRANTED) {
                requestPermissionsFromUser(new String[] {permission}, MainAbility.REQUEST_CODE);
            } else {
                navigateToOnlineDeviceScreen();
            }
        }
    }

    /**
     * Navigate to app online device screen.
     */
    private void navigateToOnlineDeviceScreen() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                present(new DashboardAbilitySlice(), new Intent());
                terminate();
            }
        }, TIMER_MILLISECOND);
    }

    /**
     * Abilityslice lifecycle method onActive.
     */
    @Override
    public void onActive() {
        super.onActive();
    }

    /**
     * Abilityslice lifecycle method onForeground.
     * @param intent as Intent
     */
    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
