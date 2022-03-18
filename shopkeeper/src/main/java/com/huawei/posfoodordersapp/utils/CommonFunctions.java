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
import ohos.app.Context;
import java.security.SecureRandom;
public class CommonFunctions {
    private static volatile CommonFunctions sInstance = null;
    public static CommonFunctions getInstance() {
        if (sInstance == null) {
            synchronized (CommonFunctions.class) {
                if (sInstance == null) {
                    sInstance = new CommonFunctions();
                }
            }
        }
        return sInstance;
    }
    public void showToast(Context context, String msg) {
    }
    public int generateOrderNumber() {
        // create instance of Random class
        SecureRandom rand = new SecureRandom();
        // Generate random integers in range 0 to 999
        int randInt = rand.nextInt(1000);
        LogUtil.debug(LogUtil.TAG_LOG, "Random_number : " + randInt);
        return randInt;
    }
}
