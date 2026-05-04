package com.ibm.inventory_management.models;
import java.time.Instant;
import java.util.List;
public class StockItemAuditEntry {
    private Instant timestamp;
    private String action;
    private String actor;
    private List<String> authorities;
    private String itemId;
    private String itemName;
    private long version;
    private String message;
    public StockItemAuditEntry() {
    }
    public StockItemAuditEntry(Instant timestamp, String action, String actor, List<String> authorities, String itemId,
            String itemName, long version, String message) {
        this.timestamp = timestamp;
        this.action = action;
        this.actor = actor;
        this.authorities = authorities;
        this.itemId = itemId;
        this.itemName = itemName;
        this.version = version;
        this.message = message;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getActor() {
        return actor;
    }
    public void setActor(String actor) {
        this.actor = actor;
    }
    public List<String> getAuthorities() {
        return authorities;
    }
    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
