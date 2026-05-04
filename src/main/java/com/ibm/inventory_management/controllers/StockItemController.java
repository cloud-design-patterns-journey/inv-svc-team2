package com.ibm.inventory_management.controllers;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ibm.inventory_management.models.StockItem;
import com.ibm.inventory_management.models.StockItemAuditEntry;
import com.ibm.inventory_management.services.StockItemApi;
@RestController
public class StockItemController {
    private final StockItemApi service;
    public StockItemController(StockItemApi service) {
        this.service = service;
    }
    @GetMapping(path = "/stock-items", produces = "application/json")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public List<StockItem> listStockItems() {
        return this.service.listStockItems();
    }
    @GetMapping(path = "/stock-items/audit", produces = "application/json")
    @PreAuthorize("hasAuthority('STOCK_AUDIT')")
    public List<StockItemAuditEntry> listAuditTrail() {
        return this.service.getAuditTrail();
    }
    @PostMapping(path = "/stock-item")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public void addStockItem(@RequestParam String name, @RequestParam String manufacturer, @RequestParam float price,
            @RequestParam int stock) {
        this.service.addStockItem(name, manufacturer, price, stock);
    }
    @PutMapping(path = "/stock-item/{id}")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public void updateStockItem(@PathVariable("id") String id, @RequestParam(required = false) String name,
            @RequestParam(required = false) String manufacturer, @RequestParam(required = false) Double price,
            @RequestParam(required = false) Integer stock) {
        this.service.updateStockItem(id, name, manufacturer, price, stock);
    }
    @DeleteMapping(path = "/stock-item/{id}")
    @PreAuthorize("hasAuthority('STOCK_DELETE')")
    public void deleteStockItem(@PathVariable("id") String id) {
        this.service.deleteStockItem(id);
    }
}
