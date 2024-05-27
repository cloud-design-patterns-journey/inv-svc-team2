package com.ibm.inventory_management.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

import com.ibm.inventory_management.models.StockItem;
import lombok.Getter;
import lombok.Setter;

public class StockItemWriteRepository {

    private static StockItemWriteRepository stockItemWrite;
    public static StockItemWriteRepository getStockItemsWrite() {
        if (stockItemWrite == null)
            stockItemWrite = new StockItemWriteRepository();
        return stockItemWrite;
    }

    @Getter
    private final Object lock = new Object();

    @Setter
    private int id = 0;
    public List<StockItem> stockItems = new ArrayList<>(asList(
            new StockItem(++id + "")
                    .withName("Item 1")
                    .withStock(100)
                    .withPrice(10.5)
                    .withManufacturer("Sony"),
            new StockItem(++id + "")
                    .withName("Item 2")
                    .withStock(150)
                    .withPrice(100.5)
                    .withManufacturer("Insignia"),
            new StockItem(++id + "")
                    .withName("Item 3")
                    .withStock(10)
                    .withPrice(1000.0)
                    .withManufacturer("Panasonic")));

    
    public void add(StockItem stockItem) {
        synchronized (lock) {
            stockItems.add(stockItem);
        }
    }

    public void updateStockItem(String id, String name, String manufacturer, Double price, int stock) {
        synchronized (lock) {
            StockItem itemToUpdate = this.stockItems.stream().filter(stockItem -> stockItem.getId().equals(id)).findFirst()
                    .orElse(null);

            if (itemToUpdate == null) {
                System.out.println("Item not found");
                return;
            }

            itemToUpdate.setName(name != null ? name : itemToUpdate.getName());
            itemToUpdate.setManufacturer(manufacturer != null ? manufacturer : itemToUpdate.getManufacturer());
            itemToUpdate.setPrice(Double.valueOf(price) != null ? price : itemToUpdate.getPrice());
            itemToUpdate.setStock(Integer.valueOf(stock) != null ? stock : itemToUpdate.getStock());
        }
    }


    public int incrementId() {
        this.setId(++id);
        return this.id;
    }

    public void deleteStockItem(String id) {
        synchronized (lock) {
            this.stockItems = this.stockItems.stream().filter((stockItem) -> !stockItem.getId().equals(id))
                    .collect(Collectors.toList());
        }
    }
}
