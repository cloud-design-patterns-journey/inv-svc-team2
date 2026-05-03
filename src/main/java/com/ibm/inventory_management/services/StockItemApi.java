package com.ibm.inventory_management.services;

import java.util.List;

import com.ibm.inventory_management.models.StockItem;

public interface StockItemApi {
    List<StockItem> listStockItems();

    void updateStockItem(String id, String name, String manufacturer, double price, int stock);

    void addStockItem(String name, String manufacturer, double price, int stock);

    void deleteStockItem(String id);
}