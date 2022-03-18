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

package com.huawei.posfoodordersapp.controller;

import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.MessageOption;
import ohos.rpc.RemoteException;


public class SendRequestRemote implements IRemoteBroker {
    private final IRemoteObject remoteObject;
    private String currentDeviceId;
    private static final int REMOTE_COMMAND = 0;

    public SendRequestRemote(IRemoteObject iRemoteObject, String deviceID) {
        this.remoteObject = iRemoteObject;
        this.currentDeviceId = deviceID;
    }

    @Override
    public IRemoteObject asObject() {
        return remoteObject;
    }
    /* remoteControl to used to send data to remote device
     * */
    public void remoteControl(String action, String status, String customerDeviceID, OrderDetailModel ordermodel) {
        MessageParcel data = MessageParcel.obtain();
        MessageParcel reply = MessageParcel.obtain();
        MessageOption option = new MessageOption(MessageOption.TF_SYNC);
        data.writeString(action);
        data.writeString(status);
        data.writeString(customerDeviceID);
        data.writeValue(ordermodel);
        LogUtil.debug(LogUtil.TAG_LOG, "remote_order_id " + ordermodel.getOrderID());
        try {
            remoteObject.sendRequest(REMOTE_COMMAND, data, reply, option);
        } catch (RemoteException e) {
            LogUtil.error(LogUtil.TAG_LOG, "remote action error " + e.getMessage());
        } finally {
            data.reclaim();
            reply.reclaim();
        }
    }
}
