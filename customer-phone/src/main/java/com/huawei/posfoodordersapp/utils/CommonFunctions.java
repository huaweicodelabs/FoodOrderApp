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
import ohos.wifi.WifiDevice;

import java.util.Random;

import static ohos.data.search.schema.PhotoItem.TAG;
/**
 * The type Common functions.
 */
public class CommonFunctions {
    /**
     * The constant sInstance.
     */
    private static volatile CommonFunctions sInstance = null;
    /**
     * Initialize Instance.
     *
     * @return the instance
     */
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
    /**
     * Show toast.
     *
     * @param context the context
     * @param msg     the msg
     */
    public void showToast(Context context, String msg) {}

}
