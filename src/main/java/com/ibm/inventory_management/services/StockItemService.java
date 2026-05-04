package com.ibm.inventory_management.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ibm.inventory_management.models.StockEvent;
import com.ibm.inventory_management.models.StockEventPriority;
import com.ibm.inventory_management.models.StockEventType;
import com.ibm.inventory_management.models.StockItem;
import com.ibm.inventory_management.models.StockItemAuditEntry;
@Service
public class StockItemService implements StockItemApi {
    private static final Logger log = LoggerFactory.getLogger(StockItemService.class);
    private final Map<String, StockItem> stockItems = new ConcurrentHashMap<>();
    private final List<StockItemAuditEntry> auditTrail = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Autowired(required = false)
    private EventStore eventStore;

    public StockItemService() {
        registerSeedItem("Item 1", 100, 10.5, "Sony");
        registerSeedItem("Item 2", 150, 100.5, "Insignia");
        registerSeedItem("Item 3", 10, 1000.0, "Panasonic");
    }
    @Override
    @Cacheable("stockItems")
    public List<StockItem> listStockItems() {
        return stockItems.values().stream()
                .sorted(Comparator.comparingLong(stockItem -> parseId(stockItem.getId())))
                .map(this::copyOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void addStockItem(String name, String manufacturer, double price, int stock) {
        String id = String.valueOf(idSequence.incrementAndGet());
        Instant now = Instant.now();
        StockItem item = new StockItem(id)
                .withName(name)
                .withStock(stock)
                .withPrice(price)
                .withManufacturer(manufacturer)
                .withVersion(1L)
                .withCreatedAt(now)
                .withUpdatedAt(now)
                .withLastModifiedBy(currentActor());
        stockItems.put(id, item);

        recordAudit("CREATE", item, "Created stock item");

        // Event Sourcing pattern (L2): append immutable creation event
        if (eventStore != null) {
            eventStore.append(new StockEvent()
                    .withId(UUID.randomUUID().toString())
                    .withEventType(StockEventType.STOCK_CREATED)
                    .withItemId(id)
                    .withItemName(name)
                    .withPreviousStock(0)
                    .withNewStock(stock)
                    .withTimestamp(now)
                    .withPerformedBy(currentActor()));
        }
    }
    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void updateStockItem(String id, String name, String manufacturer, Double price, Integer stock) {
        StockItem itemToUpdate = stockItems.get(id);
        if (itemToUpdate == null) {
            recordAudit("UPDATE_FAILED", id, null, 0L, "Item not found");
            return;
        }

        int previousStock = itemToUpdate.getStock();

        synchronized (itemToUpdate) {
            if (name != null) {
                itemToUpdate.setName(name);
            }
            if (manufacturer != null) {
                itemToUpdate.setManufacturer(manufacturer);
            }
            if (price != null) {
                itemToUpdate.setPrice(price);
            }
            if (stock != null) {
                itemToUpdate.setStock(stock);
            }
            itemToUpdate.setVersion(itemToUpdate.getVersion() + 1);
            Instant now = Instant.now();
            itemToUpdate.setUpdatedAt(now);
            itemToUpdate.setLastModifiedBy(currentActor());
            recordAudit("UPDATE", itemToUpdate, "Updated stock item");

            // Event Sourcing pattern (L2): append immutable update event with before/after stock
            if (eventStore != null) {
                eventStore.append(new StockEvent()
                        .withId(UUID.randomUUID().toString())
                        .withEventType(StockEventType.STOCK_UPDATED)
                        .withItemId(id)
                        .withItemName(itemToUpdate.getName())
                        .withPreviousStock(previousStock)
                        .withNewStock(stock != null ? stock : previousStock)
                        .withTimestamp(now)
                        .withPerformedBy(currentActor()));
            }
        }
    }
    @Override
    @CacheEvict(value = "stockItems", allEntries = true)
    public void deleteStockItem(String id) {
        StockItem removedItem = stockItems.remove(id);
        if (removedItem == null) {
            recordAudit("DELETE_FAILED", id, null, 0L, "Item not found");
            return;
        }

        recordAudit("DELETE", removedItem, "Deleted stock item");

        // Event Sourcing pattern (L2): append immutable deletion event
        if (eventStore != null) {
            Instant now = Instant.now();
            eventStore.append(new StockEvent()
                    .withId(UUID.randomUUID().toString())
                    .withEventType(StockEventType.STOCK_DELETED)
                    .withPriority(resolvePriority(removedItem.getName(), removedItem.getManufacturer()))
                    .withItemId(id)
                    .withItemName(removedItem.getName())
                    .withPreviousStock(removedItem.getStock())
                    .withNewStock(0)
                    .withTimestamp(now)
                    .withPerformedBy(currentActor()));
        }
    }
    @Override
    public List<StockItemAuditEntry> getAuditTrail() {
        return List.copyOf(auditTrail);
    }
    private void registerSeedItem(String name, int stock, double price, String manufacturer) {
        String id = String.valueOf(idSequence.incrementAndGet());
        Instant now = Instant.now();
        stockItems.put(id, new StockItem(id)
                .withName(name)
                .withStock(stock)
                .withPrice(price)
                .withManufacturer(manufacturer)
                .withVersion(1L)
                .withCreatedAt(now)
                .withUpdatedAt(now)
                .withLastModifiedBy("system"));
    }
    private StockItem copyOf(StockItem source) {
        return new StockItem(source.getId())
                .withName(source.getName())
                .withStock(source.getStock())
                .withPrice(source.getPrice())
                .withManufacturer(source.getManufacturer())
                .withVersion(source.getVersion())
                .withCreatedAt(source.getCreatedAt())
                .withUpdatedAt(source.getUpdatedAt())
                .withLastModifiedBy(source.getLastModifiedBy());
    }
    private void recordAudit(String action, StockItem item, String message) {
        recordAudit(action, item.getId(), item.getName(), item.getVersion(), message);
    }
    private void recordAudit(String action, String itemId, String itemName, long version, String message) {
        StockItemAuditEntry entry = new StockItemAuditEntry(
                Instant.now(),
                action,
                currentActor(),
                currentAuthorities(),
                itemId,
                itemName,
                version,
                message);
        auditTrail.add(entry);
        log.info("audit action={} actor={} itemId={} version={} message={}", action, entry.getActor(), itemId,
                version, message);
    }
    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "anonymous";
        }
        return authentication.getName();
    }
    private List<String> currentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .sorted()
                .toList();
    }
    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return Long.MAX_VALUE;
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
