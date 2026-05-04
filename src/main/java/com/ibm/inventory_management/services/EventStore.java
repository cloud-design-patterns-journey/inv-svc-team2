package com.ibm.inventory_management.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ibm.inventory_management.models.StockEvent;

/**
 * Append-only event store — Event Sourcing pattern (L2).
 * Every stock mutation appends an immutable event; nothing is ever deleted.
 */
@Service
public class EventStore {
    private final List<StockEvent> events = new ArrayList<>();

    public void append(StockEvent event) {
        events.add(event);
    }

    public List<StockEvent> getAll() {
        return new ArrayList<>(events);
    }

    public List<StockEvent> getByItemId(String itemId) {
        return events.stream()
                .filter(e -> itemId.equals(e.getItemId()))
                .collect(Collectors.toList());
    }
}
