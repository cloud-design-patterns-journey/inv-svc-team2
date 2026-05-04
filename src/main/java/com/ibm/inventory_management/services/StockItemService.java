package com.ibm.inventory_management.services;

import static java.util.Arrays.asList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ibm.inventory_management.models.StockEvent;
import com.ibm.inventory_management.models.StockEventPriority;
import com.ibm.inventory_management.models.StockEventType;
import com.ibm.inventory_management.models.StockItem;

@Service
public class StockItemService implements StockItemApi {
    static int id = 0;
    static List<StockItem> stockItems = new ArrayList<>(asList(
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

    @Autowired
    private EventStore eventStore;

    // Cache-Aside pattern (L4): serve reads from cache, invalidate on writes
    @Override
    @Cacheable("stockItems")
    public List<StockItem> listStockItems() {
        return this.stockItems;
    }

    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void addStockItem(String name, String manufacturer, double price, int stock) {
        String newId = ++id + "";
        this.stockItems.add(new StockItem(newId)
                .withName(name)
                .withStock(stock)
                .withPrice(price)
                .withManufacturer(manufacturer));

        // Event Sourcing pattern (L2): append immutable creation event
        eventStore.append(new StockEvent()
                .withId(UUID.randomUUID().toString())
                .withEventType(StockEventType.STOCK_CREATED)
                .withPriority(resolvePriority(name, manufacturer))
                .withItemId(newId)
                .withItemName(name)
                .withPreviousStock(0)
                .withNewStock(stock)
                .withTimestamp(Instant.now())
                .withPerformedBy("system"));
    }

    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void updateStockItem(String id, String name, String manufacturer, double price, int stock) {
        StockItem itemToUpdate = this.stockItems.stream()
                .filter(stockItem -> stockItem.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (itemToUpdate == null) {
            System.out.println("Item not found");
            return;
        }

        int previousStock = itemToUpdate.getStock();

        itemToUpdate.setName(name != null ? name : itemToUpdate.getName());
        itemToUpdate.setManufacturer(manufacturer != null ? manufacturer : itemToUpdate.getManufacturer());
        itemToUpdate.setPrice(price);
        itemToUpdate.setStock(stock);

        // Event Sourcing pattern (L2): append immutable update event with before/after stock
        eventStore.append(new StockEvent()
                .withId(UUID.randomUUID().toString())
                .withEventType(StockEventType.STOCK_UPDATED)
                .withPriority(resolvePriority(itemToUpdate.getName(), itemToUpdate.getManufacturer()))
                .withItemId(id)
                .withItemName(itemToUpdate.getName())
                .withPreviousStock(previousStock)
                .withNewStock(stock)
                .withTimestamp(Instant.now())
                .withPerformedBy("system"));
    }

    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void deleteStockItem(String id) {
        StockItem itemToDelete = this.stockItems.stream()
                .filter(stockItem -> stockItem.getId().equals(id))
                .findFirst()
                .orElse(null);

        this.stockItems = this.stockItems.stream()
                .filter(stockItem -> !stockItem.getId().equals(id))
                .collect(Collectors.toList());

        // Event Sourcing pattern (L2): append immutable deletion event
        if (itemToDelete != null) {
            eventStore.append(new StockEvent()
                    .withId(UUID.randomUUID().toString())
                    .withEventType(StockEventType.STOCK_DELETED)
                    .withPriority(resolvePriority(itemToDelete.getName(), itemToDelete.getManufacturer()))
                    .withItemId(id)
                    .withItemName(itemToDelete.getName())
                    .withPreviousStock(itemToDelete.getStock())
                    .withNewStock(0)
                    .withTimestamp(Instant.now())
                    .withPerformedBy("system"));
        }
    }

    private StockEventPriority resolvePriority(String itemName, String manufacturer) {
        String searchableText = ((itemName != null ? itemName : "") + " " + (manufacturer != null ? manufacturer : ""))
                .toLowerCase();

        if (searchableText.contains("medical")
                || searchableText.contains("urgence")
                || searchableText.contains("pharma")
                || searchableText.contains("safety")) {
            return StockEventPriority.CRITICAL;
        }

        if (searchableText.contains("resto")
                || searchableText.contains("food")
                || searchableText.contains("logistic")) {
            return StockEventPriority.HIGH;
        }

        return StockEventPriority.NORMAL;
    }
}
