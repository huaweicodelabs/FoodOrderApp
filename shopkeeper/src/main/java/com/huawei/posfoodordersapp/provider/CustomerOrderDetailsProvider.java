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

import com.huawei.posfoodordersapp.ResourceTable;
import com.huawei.posfoodordersapp.interfaces.Adapterlistener;
import com.huawei.posfoodordersapp.model.OrderDetailModel;
import com.huawei.posfoodordersapp.utils.LogUtil;
import com.huawei.posfoodordersapp.utils.Constants;
import ohos.app.Context;
import java.util.ArrayList;
import java.util.Locale;

import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Text;
import ohos.agp.components.DependentLayout;
import ohos.agp.components.Button;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.Component;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.TextFilter;

public class CustomerOrderDetailsProvider extends BaseItemProvider {

    private ArrayList<OrderDetailModel> orderItemList;
    public ArrayList<OrderDetailModel> orderItemListFiltered;
    private Context context;
    private Adapterlistener listener;

    public CustomerOrderDetailsProvider(Context context, ArrayList<OrderDetailModel> orderItemList, Adapterlistener listener) {
        this.context = context;
        this.orderItemList = orderItemList;
        this.orderItemListFiltered = orderItemList;
        this.listener = listener;
    }

    // Used to save the child components in ListContainer.
    public static class ViewHolder {
        Text username_text;
        Text table_no_text;
        Text status_text;
        DependentLayout layout_positive;
        DependentLayout layout_negative;
        DependentLayout layout_view;
        Button btn_view;
        Button btn_positive;
        Button btn_negative;
    }

    @Override
    public int getCount() {
        return orderItemListFiltered == null ? 0 : orderItemListFiltered.size();
    }

    @Override
    public Object getItem(int position) {
        if (orderItemListFiltered != null && position >= 0 && position < orderItemListFiltered.size()){
            return orderItemListFiltered.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component component, ComponentContainer componentContainer) {
        ViewHolder holder;
        OrderDetailModel item = orderItemListFiltered.get(position);
        Component temp = component;
        if (temp == null) {
            temp = LayoutScatter.getInstance(context).parse(ResourceTable.Layout_item_dashboard_order, null, false);
            holder = new ViewHolder();
            holder.username_text = (Text) temp.findComponentById(ResourceTable.Id_username_text);
            holder.table_no_text = (Text) temp.findComponentById(ResourceTable.Id_table_no_text);
            holder.status_text = (Text) temp.findComponentById(ResourceTable.Id_status_text);
            holder.layout_view = (DependentLayout) temp.findComponentById(ResourceTable.Id_layout_view);
            holder.layout_positive = (DependentLayout) temp.findComponentById(ResourceTable.Id_layout_positive);
            holder.layout_negative = (DependentLayout) temp.findComponentById(ResourceTable.Id_layout_negative);
            holder.btn_view = (Button) temp.findComponentById(ResourceTable.Id_btn_view);
            holder.btn_positive = (Button) temp.findComponentById(ResourceTable.Id_btn_positive);
            holder.btn_negative = (Button) temp.findComponentById(ResourceTable.Id_btn_negative);
            temp.setTag(holder);
        } else {
            holder = (ViewHolder) temp.getTag();
        }

        holder.username_text.setText(item.getUsername());
        holder.table_no_text.setText("Table "+String.valueOf(position+1));

        if (Constants.MENU_CANCELED.contains(item.getCommand())
                && !item.isMenuAccepted()) {
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_positive.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.HIDE);
            holder.status_text.setText("Menu canceled");
        } else if (Constants.ORDER_CANCELED.contains(item.getCommand())
                && item.isMenuAccepted() && item.isOrderPlaced() && !item.isOrderAccepted()) {
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_positive.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.HIDE);
            holder.status_text.setText("Order canceled");
        } else if (!item.isMenuAccepted() && !item.isOrderAccepted()) {
            holder.btn_positive.setText("Accept");
            holder.btn_negative.setText("Cancel");
            holder.layout_view.setVisibility(Component.HIDE);
            holder.status_text.setText("Waiting for menu");
        } else if (item.isMenuAccepted() && !item.isOrderPlaced()) {
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_positive.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.HIDE);
            holder.status_text.setText("Order not confirmed yet");
        } else if (item.isMenuAccepted() && item.isOrderPlaced() && !item.isOrderAccepted()) {
            holder.btn_positive.setText("Accept");
            holder.btn_negative.setText("Cancel");
            holder.layout_view.setVisibility(Component.VISIBLE);
            holder.status_text.setText("Waiting for order");
        } else if (item.isMenuAccepted() && item.isOrderPlaced() && item.isOrderAccepted() && !item.isOrderDelivered()) {
            holder.btn_positive.setText("Ready to Deliver");
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.VISIBLE);
            holder.status_text.setText("Waiting for order");
        } else if (item.isMenuAccepted() && item.isOrderPlaced() && item.isOrderAccepted() && item.isOrderDelivered() && !item.isPaid()) {
            holder.layout_positive.setVisibility(Component.HIDE);
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.VISIBLE);
            holder.status_text.setText("Payment is pending");
        } else if (item.isMenuAccepted() && item.isOrderPlaced() && item.isOrderAccepted() && item.isOrderDelivered() && item.isPaid()) {
            holder.layout_positive.setVisibility(Component.VISIBLE);
            holder.btn_positive.setText("Bill Details");
            holder.layout_negative.setVisibility(Component.HIDE);
            holder.layout_view.setVisibility(Component.VISIBLE);
            holder.status_text.setText("Payment is received");
        }

        holder.btn_view.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                listener.onItemViewClicked(item, position);
            }
        });

        holder.btn_positive.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                listener.onItemPositiveClicked(item, position);
            }
        });

        holder.btn_negative.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                listener.onItemNegativeClicked(item, position);
            }
        });

        LogUtil.error(LogUtil.TAG_LOG,"username--->"+orderItemListFiltered.get(position).getUsername());

        return temp;
    }

    public ArrayList<OrderDetailModel> getFilteredList() {
        return orderItemListFiltered;
    }

    @Override
    public TextFilter getFilter() {
        return new TextFilter() {
            @Override
            protected FilterResults executeFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    orderItemListFiltered = orderItemList;
                } else {
                    ArrayList<OrderDetailModel> filteredList = new ArrayList<>();
                    for (OrderDetailModel orderDetailModel : orderItemList) {
                        // name match condition. this might differ depending on your requirement
                        if (orderDetailModel.getStatus().toLowerCase(Locale.ROOT).contains(charString.toLowerCase(Locale.ROOT))) {
                            filteredList.add(orderDetailModel);
                        }
                    }
                    if (filteredList.size() == 0) {
                        if (charString.contains(Constants.DELIEVERED)) {
                            for (OrderDetailModel orderDetailModel : orderItemList) {
                                // name match condition. this might differ depending on your requirement
                                if (orderDetailModel.getStatus().toLowerCase(Locale.ROOT).contains(Constants.PAID.toLowerCase(Locale.ROOT))) {
                                    filteredList.add(orderDetailModel);
                                }
                            }
                        }
                    }
                    orderItemListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.results = orderItemListFiltered;
                return filterResults;
            }

            @Override
            protected void publishFilterResults(CharSequence charSequence, FilterResults filterResults) {
                orderItemListFiltered = (ArrayList<OrderDetailModel>) filterResults.results;
                notifyDataChanged();
            }
        };
    }
}
