package com.ibm.inventory_management.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.inventory_management.models.StockItem;
import com.ibm.inventory_management.repository.StockItemReadRepository;
import com.ibm.inventory_management.repository.StockItemWriteRepository;

@Service
public class StockItemService implements StockItemApi {

    private final StockItemReadRepository stockItemReadRepository = StockItemReadRepository.get();
    private final StockItemWriteRepository stockItemWriteRepository = StockItemWriteRepository.getStockItemsWrite();

    @Override
    public List<StockItem> listStockItems() {
        return this.stockItemReadRepository.getStockItems();
    }

    @Override
    public void addStockItem(String name, String manufacturer, double price, int stock) {
        this.stockItemWriteRepository.add(new StockItem(stockItemWriteRepository.incrementId() + "")
            .withName(name)
            .withStock(stock)
            .withPrice(price)
            .withManufacturer(manufacturer));
    }

    @Override
    public void updateStockItem(String id, String name, String manufacturer, double price, int stock) {
        this.stockItemWriteRepository.updateStockItem(id, name, manufacturer, price, stock);
    }

    @Override
    public void deleteStockItem(String id) {
        this.stockItemWriteRepository.deleteStockItem(id);
    }
}
