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
import com.huawei.posfoodordersapp.localdb.MyDataBaseHelper;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Image;
import ohos.app.Context;

import java.util.List;

/**
 * News list adapter
 *
 * @since 2020-12-04
 */
public class FoodMenuProvider extends BaseItemProvider {
    private List<FoodMenuItem> foodMenuList;
    private Context context;
    int quantity;
    MyDataBaseHelper databaseHelper;

    /**
     * constructor function
     *
     * @param listBasicInfo list info
     * @param context       context
     * @since 2020-12-04
     */
    public FoodMenuProvider(List<FoodMenuItem> listBasicInfo, Context context) {
        this.foodMenuList = listBasicInfo;
        this.context = context;
        databaseHelper = new MyDataBaseHelper(context);
    }

    @Override
    public int getCount() {
        return foodMenuList == null ? 0 : foodMenuList.size();
    }

    @Override
    public Object getItem(int position) {
        return foodMenuList.get(position);
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
            temp = LayoutScatter.getInstance(context).parse(ResourceTable.Layout_item_foodmenu_layout, null, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (Text) temp.findComponentById(ResourceTable.Id_item_title);
            viewHolder.prize = (Text) temp.findComponentById(ResourceTable.Id_item_prize);
            viewHolder.plus = (Text) temp.findComponentById(ResourceTable.Id_item_plus);
            viewHolder.minus = (Text) temp.findComponentById(ResourceTable.Id_item_minus);
            viewHolder.quantity = (Text) temp.findComponentById(ResourceTable.Id_item_quantity);
            viewHolder.image = (Image) temp.findComponentById(ResourceTable.Id_image_food);

            temp.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) temp.getTag();
        }

        viewHolder.title.setText(foodMenuList.get(position).getName());
        viewHolder.prize.setText("₹" + foodMenuList.get(position).getPrize());

        Glide.with(context)
                .load(foodMenuList.get(position).getImageurl())
                .error(ResourceTable.Media_icon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(viewHolder.image);

        viewHolder.plus.setClickedListener(component1 -> {
            String itemcount = viewHolder.quantity.getText();
            quantity = Integer.parseInt(itemcount);
            quantity++;
            viewHolder.quantity.setText(String.valueOf(quantity));

            boolean isDataExist = databaseHelper.checkIsDataAlreadyExist(viewHolder.title.getText());
            if (!isDataExist) {
                int orderID = databaseHelper.fetchOrderID(viewHolder.title.getText());
                LogUtil.debug(LogUtil.TAG_LOG,"Fetching_orderId:"+orderID);
                CommonFunctions.getInstance().showToast(context, "Data not Exist");
                databaseHelper.addtoCart(viewHolder.title.getText(), foodMenuList.get(position).getPrize(), Integer.parseInt(viewHolder.quantity.getText()), foodMenuList.get(position).getImageurl(), orderID, "false", "false");
            } else {
                CommonFunctions.getInstance().showToast(context, "Data already Exist");
                databaseHelper.updateDataplus(quantity, viewHolder.title.getText());
            }
        });

        viewHolder.minus.setClickedListener(component1 -> {
            int quant = Integer.parseInt(viewHolder.quantity.getText());
            if (quant == 0) {
                CommonFunctions.getInstance().showToast(context, "Cant decrease quantity < 0");
            } else if (quant >= 1) {
                boolean isDataExist = databaseHelper.checkIsDataAlreadyExist(viewHolder.title.getText());
                quant -= 1;
                viewHolder.quantity.setText(String.valueOf(quant));

                if (isDataExist) {
                    if (quant == 0) {
                        CommonFunctions.getInstance().showToast(context, "Data already Exist - Deleted");
                        databaseHelper.deleteData(viewHolder.title.getText());
                    } else {
                        CommonFunctions.getInstance().showToast(context, "Data already Exist - Updated");
                        databaseHelper.updateDataMinus(quant, viewHolder.title.getText());
                    }
                }
            }
        });

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
        Text plus;
        Text minus;
        Text quantity;
        Image image;
    }
}
