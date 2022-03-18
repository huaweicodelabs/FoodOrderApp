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

package com.huawei.posfoodordersapp.localdb;
import com.huawei.posfoodordersapp.model.FoodMenuItem;
import com.huawei.posfoodordersapp.utils.CommonFunctions;
import com.huawei.posfoodordersapp.utils.LogUtil;
import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.ValuesBucket;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.AbsRdbPredicates;
import ohos.data.resultset.ResultSet;

import java.util.ArrayList;
/**
 * The type My data base helper.
 */
public class MyDataBaseHelper {
    private Context mcontext;
    private RdbStore mStore;
    /**
     * The Config.
     */
    StoreConfig config = StoreConfig.newDefaultConfig("RdbStoreTest.db");
    /**
     * Instantiates a new My data base helper.
     *
     * @param context the context
     */
    public MyDataBaseHelper(Context context) {
        this.mcontext = context;
        DatabaseHelper helper = new DatabaseHelper(context);
        mStore = helper.getRdbStore(config, 1, callback, null);
    }
    /**
     * The Callback.
     */
    final RdbOpenCallback callback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore store) {
            store.executeSql("CREATE TABLE IF NOT EXISTS cart (id INTEGER PRIMARY KEY AUTOINCREMENT, foodname TEXT NOT NULL , prize INTEGER NOT NULL, quantity INTEGER NOT NULL, imageURL TEXT NOT NULL, orderID TEXT NOT NULL, isOrderConfirmed TEXT NOT NULL, isOrderDelievered TEXT NOT NULL)");
        }

        @Override
        public void onUpgrade(RdbStore store, int oldVersion, int newVersion) {
        }
    };
    /**
     * Addto cart.
     *
     * @param foodname          the foodname
     * @param prize             the prize
     * @param quantity          the quantity
     * @param imageURL          the image url
     * @param orderID           the order id
     * @param isOrderConfirmed  the is order confirmed
     * @param isOrderDelievered the is order delievered
     */
    public void addtoCart(String foodname, int prize, int quantity, String imageURL, int orderID, String isOrderConfirmed, String isOrderDelievered) {
        ValuesBucket values = new ValuesBucket();
        values.putString("foodname", foodname);
        values.putInteger("prize", prize);
        values.putInteger("quantity", quantity);
        values.putString("imageURL", imageURL);
        values.putInteger("orderID", orderID);
        values.putString("isOrderConfirmed", isOrderConfirmed);
        values.putString("isOrderDelievered", isOrderDelievered);
        long id = mStore.insert("cart", values);
        viewCartItems();
    }
    /**
     * Update data.
     *
     * @param quantity the quantity
     * @param foodname the foodname
     */
    public void updateData(String quantity, String foodname) {
        try {
            ValuesBucket values = new ValuesBucket();
            values.putString("quantity", quantity);

            AbsRdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", foodname);
            int index = mStore.update(values, rdbPredicates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Delete table.
     */
    public void deleteTable() {
        try {
            RdbPredicates rdbPredicates = new RdbPredicates("cart");
            int index = mStore.delete(rdbPredicates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Delete data.
     *
     * @param name the name
     */
    public void deleteData(String name) {
        try {
            RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", name);
            int index = mStore.delete(rdbPredicates);
            checkIsDataAlreadyExist(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * View cart items is order confirmed array list.
     *
     * @param isOrderConfirmed  the is order confirmed
     * @param isOrderDelievered the is order delievered
     * @return the array list
     */
    public ArrayList<FoodMenuItem> viewCartItemsIsOrderConfirmed(String isOrderConfirmed, String isOrderDelievered) {
        ArrayList<FoodMenuItem> cartList = new ArrayList<>();
        try {
            String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
            RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("isOrderConfirmed",isOrderConfirmed).equalTo("isOrderDelievered",isOrderDelievered).orderByAsc("id");
            ResultSet resultSet = mStore.query(rdbPredicates, columns);
            if (resultSet == null || resultSet.getRowCount() <= 0) {
            }
            String data = "";
            while (resultSet.goToNextRow()) {
                String foodname = resultSet.getString(resultSet.getColumnIndexForName("foodname"));
                int prize = resultSet.getInt(resultSet.getColumnIndexForName("prize"));
                int quantity = resultSet.getInt(resultSet.getColumnIndexForName("quantity"));
                String imageURL = resultSet.getString(resultSet.getColumnIndexForName("imageURL"));
                int orderId = resultSet.getInt(resultSet.getColumnIndexForName("orderID"));
                String isOrderConfirm = resultSet.getString(resultSet.getColumnIndexForName("isOrderConfirmed"));
                String isOrderDelievere = resultSet.getString(resultSet.getColumnIndexForName("isOrderDelievered"));

                FoodMenuItem cartModel = new FoodMenuItem();
                cartModel.setName(foodname);
                cartModel.setPrize(prize);
                cartModel.setQuantity(quantity);
                cartModel.setImageurl(imageURL);
                cartModel.setOrderId(orderId);
                cartModel.setIsOrderConfirmed(isOrderConfirm);
                cartModel.setIsOrderDelievered(isOrderDelievere);
                cartList.add(cartModel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cartList;
    }
    /**
     * View cart items array list.
     *
     * @return the array list
     */
    public ArrayList<FoodMenuItem> viewCartItems() {
        ArrayList<FoodMenuItem> cartList = new ArrayList<>();
        try {
            String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
            RdbPredicates rdbPredicates = new RdbPredicates("cart").orderByAsc("id");
            ResultSet resultSet = mStore.query(rdbPredicates, columns);
            if (resultSet == null || resultSet.getRowCount() <= 0) {
            }
            String data = "";
            while (resultSet.goToNextRow()) {
                String foodname = resultSet.getString(resultSet.getColumnIndexForName("foodname"));
                int prize = resultSet.getInt(resultSet.getColumnIndexForName("prize"));
                int quantity = resultSet.getInt(resultSet.getColumnIndexForName("quantity"));
                String imageURL = resultSet.getString(resultSet.getColumnIndexForName("imageURL"));
                int orderId = resultSet.getInt(resultSet.getColumnIndexForName("orderID"));
                String isOrderConfirmed = resultSet.getString(resultSet.getColumnIndexForName("isOrderConfirmed"));
                String isOrderDelievered = resultSet.getString(resultSet.getColumnIndexForName("isOrderDelievered"));
                FoodMenuItem cartModel = new FoodMenuItem();
                cartModel.setName(foodname);
                cartModel.setPrize(prize);
                cartModel.setQuantity(quantity);
                cartModel.setImageurl(imageURL);
                cartModel.setOrderId(orderId);
                cartModel.setIsOrderConfirmed(isOrderConfirmed);
                cartModel.setIsOrderDelievered(isOrderDelievered);
                cartList.add(cartModel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cartList;
    }
    /**
     * Check is data already exist boolean.
     *
     * @param foodname the foodname
     * @return the boolean
     */
    public boolean checkIsDataAlreadyExist(String foodname) {
        String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
        RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", foodname).equalTo("orderID", 0);
        ResultSet resultSet = mStore.query(rdbPredicates, columns);
        if (resultSet.getRowCount() <= 0) {
            resultSet.close();
            return false;
        }
        resultSet.close();
        return true;
    }
    /**
     * Update data order id.
     *
     * @param orderID           the order id
     * @param isOrderConfirmed  the is order confirmed
     * @param isOrderDelievered the is order delievered
     */
    public void updateDataOrderID(int orderID, String isOrderConfirmed, String isOrderDelievered) {
        try {
            ValuesBucket values = new ValuesBucket();
            values.putInteger("orderID", orderID);
            values.putString("isOrderConfirmed", isOrderConfirmed);
            values.putString("isOrderDelievered", isOrderDelievered);
            String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
            RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("orderID", 0);
            ResultSet resultSet = mStore.query(rdbPredicates, columns);
            for (int i = 0; i < resultSet.getRowCount(); i++) {
                LogUtil.debug(LogUtil.TAG_LOG, "Count:" + resultSet.getRowCount());
                int index = mStore.update(values, rdbPredicates);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Update dataplus.
     *
     * @param quantity the quantity
     * @param foodname the foodname
     */
    public void updateDataplus(int quantity,String foodname){
        try {
            ValuesBucket values = new ValuesBucket();
            values.putInteger("quantity", quantity);

            AbsRdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname",foodname);
            int index = mStore.update(values, rdbPredicates);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Update data minus.
     *
     * @param quantity the quantity
     * @param foodname the foodname
     */
    public void updateDataMinus(int quantity,String foodname){
        try {
            ValuesBucket values = new ValuesBucket();
            values.putInteger("quantity", quantity);

            AbsRdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname",foodname);
            int index = mStore.update(values, rdbPredicates);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Fetch order id int.
     *
     * @param foodname the foodname
     * @return the int
     */
    public int fetchOrderID(String foodname) {
        String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
        RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", foodname);
        ResultSet resultSet = mStore.query(rdbPredicates, columns);
        int orderID = 0;
        while (resultSet.goToNextRow()) {
            orderID = resultSet.getInt(resultSet.getColumnIndexForName("orderID"));
        }
        resultSet.close();
        return orderID;
    }
    /**
     * Fetch quaunity int.
     *
     * @param foodname the foodname
     * @return the int
     */
    public int fetchQuaunity(String foodname) {
        String[] columns = new String[]{"id", "foodname", "prize", "quantity", "imageURL", "orderID", "isOrderConfirmed", "isOrderDelievered"};
        RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", foodname);
        ResultSet resultSet = mStore.query(rdbPredicates, columns);
        int quantity = 0;
        while (resultSet.goToNextRow()) {
            quantity = resultSet.getInt(resultSet.getColumnIndexForName("quantity"));
        }
        resultSet.close();
        return quantity;
    }
    /**
     * Delete data list.
     *
     * @param foodMenuItemArrayList the food menu item array list
     */
    public void deleteDataList(ArrayList<FoodMenuItem> foodMenuItemArrayList) {
        for (FoodMenuItem foodMenuItem:foodMenuItemArrayList){
            if(foodMenuItem.getOrderId()==0) {
                try {
                    RdbPredicates rdbPredicates = new RdbPredicates("cart").equalTo("foodname", foodMenuItem.getName());
                    int index = mStore.delete(rdbPredicates);
                    CommonFunctions.getInstance().showToast(mcontext,"Order cancelled");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
