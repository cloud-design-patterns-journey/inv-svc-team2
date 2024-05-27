package com.ibm.inventory_management.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ibm.inventory_management.models.StockItem;

public class StockItemReadRepository {

    private static StockItemReadRepository instance;
    public static StockItemReadRepository get() {
        if (instance == null)
            instance = new StockItemReadRepository();
        return instance;
    }

    public List<StockItem> stockItems = new ArrayList<>(StockItemWriteRepository.getStockItemsWrite().stockItems);
    private final Object lock = new Object();

    public List<StockItem> getStockItems() {
        synchronizeStores();
        synchronized (lock) {
            return new ArrayList<>(this.stockItems);
        }
    }

    private boolean isSync() {
        List<StockItem> stockItemsWrite = StockItemWriteRepository.getStockItemsWrite().stockItems;
        synchronized (StockItemWriteRepository.getStockItemsWrite().getLock()) {
            if(stockItemsWrite.size() != stockItems.size()) return false;
            for(int i = 0; i < stockItemsWrite.size(); i++) {
                StockItem item = stockItemsWrite.get(i);
                if(item.hashCode() != stockItems.get(i).hashCode())
                    return false;
            }
            return true;
        }
    }

    private void synchronizeStores() {
        if(!isSync()) {
            final List<StockItem> stockItemsWrite = StockItemWriteRepository.getStockItemsWrite().stockItems;
            synchronized (lock) {
                synchronized (StockItemWriteRepository.getStockItemsWrite().getLock()) {
                    this.stockItems = new ArrayList<>(stockItemsWrite);
                }
            }
        }

    }
}
