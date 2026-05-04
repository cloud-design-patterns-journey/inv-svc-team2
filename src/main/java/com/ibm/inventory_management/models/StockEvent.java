package com.ibm.inventory_management.models;

import java.time.Instant;

public class StockEvent {
    private String id;
    private StockEventType eventType;
    private StockEventPriority priority = StockEventPriority.NORMAL;
    private String itemId;
    private String itemName;
    private int previousStock;
    private int newStock;
    private Instant timestamp;
    private String performedBy;

    public StockEvent() {
        super();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public StockEvent withId(String id) { this.id = id; return this; }

    public StockEventType getEventType() { return eventType; }
    public void setEventType(StockEventType eventType) { this.eventType = eventType; }
    public StockEvent withEventType(StockEventType eventType) { this.eventType = eventType; return this; }

    public StockEventPriority getPriority() { return priority; }
    public void setPriority(StockEventPriority priority) { this.priority = priority; }
    public StockEvent withPriority(StockEventPriority priority) { this.priority = priority; return this; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public StockEvent withItemId(String itemId) { this.itemId = itemId; return this; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public StockEvent withItemName(String itemName) { this.itemName = itemName; return this; }

    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }
    public StockEvent withPreviousStock(int previousStock) { this.previousStock = previousStock; return this; }

    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }
    public StockEvent withNewStock(int newStock) { this.newStock = newStock; return this; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public StockEvent withTimestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public StockEvent withPerformedBy(String performedBy) { this.performedBy = performedBy; return this; }
}
