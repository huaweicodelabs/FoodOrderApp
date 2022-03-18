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

import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.aafwk.content.Intent;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.MatchingSkills;
import ohos.event.commonevent.CommonEventSupport;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.rpc.RemoteException;

public class DistributedNotificationPlugin {
    private static final String TAG = "DistributedNotificationPlugin";
    private static final String NOTIFICATION_ACTION = "com.huawei.posfoodordersapp";
    private static final String NOTIFICATION_KEY = "notification_key";
    private CommonEventSubscriber commonEventSubscriber;
    private DistributedNotificationEventListener eventListener;

    private static DistributedNotificationPlugin instance;


    public static synchronized DistributedNotificationPlugin getInstance() {
        if (instance == null) {
            synchronized (DistributedNotificationPlugin.class) {
                if (instance == null) {
                    instance = new DistributedNotificationPlugin();
                }
            }
        }
        return instance;
    }
    /* Subscribe the event to receive data
     * */
    public void subscribeEvent() {
        LogUtil.info(TAG, "CommonEvent onSubscribe begin.");
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(NOTIFICATION_ACTION);
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_SCREEN_ON);

        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscriber = new CommonEventSubscriber(commonEventSubscribeInfo) {
            @Override
            public void onReceiveEvent(CommonEventData commonEventData) {
                LogUtil.info(TAG, "CommonEventData onReceiveEvent begin");
                if (commonEventData == null) {
                    LogUtil.info(TAG, "commonEventData is null.");
                    return;
                }
                Intent intent = commonEventData.getIntent();
                if (intent == null) {
                    LogUtil.debug(TAG, "commonEventData getIntent is null.");
                    return;
                }
                String receivedAction = intent.getAction();
                LogUtil.info(TAG, "onReceiveEvent action:" + receivedAction);
                if (receivedAction.equals(NOTIFICATION_ACTION)) {
                    String notificaionContent = intent.getStringParam(NOTIFICATION_KEY);
                    if (eventListener != null) {
                        eventListener.onEventReceive(notificaionContent);
                    }
                }
            }
        };

        LogUtil.info(TAG, "CommonEventManager subscribeCommonEvent begin.");
        try {
            CommonEventManager.subscribeCommonEvent(commonEventSubscriber);
            if (eventListener != null) {
                eventListener.onEventSubscribe("CommonEvent Subscribe Success");
            }
        } catch (RemoteException e) {
            LogUtil.error(TAG, "CommonEvent Subscribe Error!");
        }
    }
    /* Publish the event to receive data
     * */
    public void publishEvent(String event) {
        LogUtil.info(TAG, "publish CommonEvent begin");
        LogUtil.info(TAG, "publish CommonEvent: NOTIFICATION_KEY - "+event);
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_ACTION);
        intent.setParam(NOTIFICATION_KEY,event);
        CommonEventData commonEventData = new CommonEventData(intent);

        try {
            CommonEventManager.publishCommonEvent(commonEventData);
            LogUtil.info(TAG, "the action of Intent is:" + NOTIFICATION_ACTION);
            if (eventListener != null) {
                eventListener.onEventPublish("CommonEvent Publish Success");
            }
        } catch (RemoteException e) {
            LogUtil.error(TAG, "CommonEvent publish Error!");
        }
    }
    /* unSubscribe the event to reject data parsing
     * */
    public void unsubscribeEvent() {
        LogUtil.info(TAG, "CommonEvent onUnsubscribe begin.");
        if (commonEventSubscriber == null) {
            LogUtil.info(TAG, "CommonEvent onUnsubscribe commonEventSubscriber is null");
            return;
        }
        try {
            LogUtil.info(TAG, "CommonEventManager unsubscribeCommonEvent begin.");
            CommonEventManager.unsubscribeCommonEvent(commonEventSubscriber);
            if (eventListener != null) {
                eventListener.onEventUnSubscribe("CommonEvent Unsubscribe Success");
            }
        } catch (RemoteException e) {
            LogUtil.error(TAG, "CommonEvent Unsubscribe Error!");
        }
    }
    /* Interface methods to access the override methods
     * */
    public interface DistributedNotificationEventListener {
        void onEventSubscribe(String result);
        void onEventPublish(String result);
        void onEventUnSubscribe(String result);
        void onEventReceive(String result);
    }

    public void setEventListener(DistributedNotificationEventListener eventListener) {
        this.eventListener = eventListener;
    }
}
