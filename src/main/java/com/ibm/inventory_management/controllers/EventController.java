package com.ibm.inventory_management.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.ibm.inventory_management.models.StockEvent;
import com.ibm.inventory_management.services.EventStore;

@RestController
@RequestMapping("/stock-events")
public class EventController {

    private final EventStore eventStore;

    public EventController(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @GetMapping(produces = "application/json")
    public List<StockEvent> getAllEvents() {
        return eventStore.getAll();
    }

    @GetMapping(path = "/{itemId}", produces = "application/json")
    public List<StockEvent> getEventsByItem(@PathVariable("itemId") String itemId) {
        return eventStore.getByItemId(itemId);
    }
}
