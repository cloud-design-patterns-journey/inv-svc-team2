package com.ibm.inventory_management.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ibm.inventory_management.models.StockEvent;
import com.ibm.inventory_management.models.StockEventPriority;

/**
 * Append-only event store — Event Sourcing pattern (L2).
 * Every stock mutation appends an immutable event; nothing is ever deleted.
 */
@Service
public class EventStore {
    private static final Comparator<QueuedStockEvent> QUEUE_ORDER = Comparator
            .comparingInt((QueuedStockEvent entry) -> priorityRank(entry.event.getPriority()))
            .reversed()
            .thenComparing(entry -> entry.event.getTimestamp(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingLong(entry -> entry.sequence);

    private final PriorityQueue<QueuedStockEvent> events = new PriorityQueue<>(QUEUE_ORDER);
    private long sequence = 0L;

    public void append(StockEvent event) {
        if (event.getPriority() == null) {
            event.setPriority(StockEventPriority.NORMAL);
        }

        events.add(new QueuedStockEvent(event, ++sequence));
    }

    public List<StockEvent> getAll() {
        PriorityQueue<QueuedStockEvent> snapshot = new PriorityQueue<>(events);
        List<StockEvent> orderedEvents = new ArrayList<>(snapshot.size());

        while (!snapshot.isEmpty()) {
            orderedEvents.add(snapshot.poll().event);
        }

        return orderedEvents;
    }

    public List<StockEvent> getByItemId(String itemId) {
        return getAll().stream()
                .filter(e -> itemId.equals(e.getItemId()))
                .collect(Collectors.toList());
    }

    private static class QueuedStockEvent {
        private final StockEvent event;
        private final long sequence;

        private QueuedStockEvent(StockEvent event, long sequence) {
            this.event = event;
            this.sequence = sequence;
        }
    }

    private static int priorityRank(StockEventPriority priority) {
        if (priority == null) {
            return 0;
        }

        switch (priority) {
            case CRITICAL:
                return 3;
            case HIGH:
                return 2;
            case NORMAL:
            default:
                return 1;
        }
    }
}
