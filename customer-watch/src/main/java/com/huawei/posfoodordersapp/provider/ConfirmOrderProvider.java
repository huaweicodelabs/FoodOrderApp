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

package com.huawei.posfoodordersapp.provider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.components.Image;
import ohos.app.Context;
import java.util.List;

/**
 * News list adapter
 *
 * @since 2020-12-04
 */
public class ConfirmOrderProvider extends BaseItemProvider {
    private List<FoodMenuItem> cartList;
    private Context context;
    private int quantity;
    int prize;

    /**
     * constructor function
     *
     * @param listBasicInfo list info
     * @param context       context
     * @since 2020-12-04
     */
    public ConfirmOrderProvider(List<FoodMenuItem> listBasicInfo, Context context) {
        this.cartList = listBasicInfo;
        this.context = context;

    }

    @Override
    public int getCount() {
        return cartList == null ? 0 : cartList.size();
    }

    @Override
    public Object getItem(int position) {
        return cartList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component component, ComponentContainer componentContainer) {

        ViewHolder viewHolder;
        Component temp = component;
        if (temp == null) {
            temp = LayoutScatter.getInstance(context).parse(ResourceTable.Layout_item_confirm_order_layout, null, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (Text) temp.findComponentById(ResourceTable.Id_text_title);
            viewHolder.prize = (Text) temp.findComponentById(ResourceTable.Id_text_prize);
            viewHolder.quantity = (Text) temp.findComponentById(ResourceTable.Id_text_qty);
            viewHolder.image = (Image) temp.findComponentById(ResourceTable.Id_image_food);
            temp.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) temp.getTag();
        }

        viewHolder.title.setText(cartList.get(position).getName());
        viewHolder.quantity.setText("Qty: " + cartList.get(position).getQuantity());
        quantity = cartList.get(position).getQuantity();
        // calculate each item prize
        int itemPrize = cartList.get(position).getPrize();
        prize = itemPrize * quantity;
        viewHolder.prize.setText("₹" + prize);
        Glide.with(context)
                .load(cartList.get(position).getImageurl())
                .error(ResourceTable.Media_icon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(viewHolder.image);
        return temp;
    }

    /**
     * ViewHolder which has title and image
     *
     * @since 2020-12-04
     */
    private static class ViewHolder {
        Text title;
        Text prize;
        Text quantity;
        Image image;
    }

}
