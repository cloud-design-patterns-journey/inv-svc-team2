package com.ibm.inventory_management.repository;

import java.util.ArrayList;
import java.util.List;

import com.ibm.inventory_management.models.StockItem;

public class StockItemReadRepository {
    public List<StockItem> stockItems = new ArrayList<>(StockItemWriteRepository.getStockItemsWrite().stockItems);


    public List<StockItem> getStockItems() {
        return this.stockItems;
    }
}
