package com.ibm.inventory_management.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.inventory_management.models.StockEvent;
import com.ibm.inventory_management.models.StockEventType;

@DisplayName("EventStore")
public class EventStoreTest {
    EventStore eventStore;

    @BeforeEach
    public void setup() {
        eventStore = new EventStore();
    }

    @Nested
    @DisplayName("Given an empty event store")
    class GivenEmptyStore {

        @Test
        @DisplayName("When getAll is called then it returns an empty list")
        public void returns_empty_list() {
            assertTrue(eventStore.getAll().isEmpty());
        }

        @Test
        @DisplayName("When getByItemId is called then it returns an empty list")
        public void returns_empty_list_by_item() {
            assertTrue(eventStore.getByItemId("1").isEmpty());
        }
    }

    @Nested
    @DisplayName("Given events are appended")
    class GivenEventsAppended {

        @Test
        @DisplayName("When getAll is called then it returns all events in order")
        public void returns_all_events() {
            StockEvent e1 = newEvent("1", StockEventType.STOCK_CREATED);
            StockEvent e2 = newEvent("2", StockEventType.STOCK_UPDATED);
            eventStore.append(e1);
            eventStore.append(e2);

            List<StockEvent> result = eventStore.getAll();
            assertEquals(2, result.size());
            assertEquals(StockEventType.STOCK_CREATED, result.get(0).getEventType());
            assertEquals(StockEventType.STOCK_UPDATED, result.get(1).getEventType());
        }

        @Test
        @DisplayName("When getByItemId is called then it returns only events for that item")
        public void filters_by_item_id() {
            eventStore.append(newEvent("item-1", StockEventType.STOCK_CREATED));
            eventStore.append(newEvent("item-2", StockEventType.STOCK_UPDATED));
            eventStore.append(newEvent("item-1", StockEventType.STOCK_UPDATED));

            List<StockEvent> result = eventStore.getByItemId("item-1");
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(e -> "item-1".equals(e.getItemId())));
        }

        @Test
        @DisplayName("When appended, the original list is not mutated (immutability)")
        public void get_all_returns_defensive_copy() {
            eventStore.append(newEvent("1", StockEventType.STOCK_CREATED));
            List<StockEvent> result = eventStore.getAll();
            result.clear();

            assertEquals(1, eventStore.getAll().size());
        }
    }

    private StockEvent newEvent(String itemId, StockEventType type) {
        return new StockEvent()
                .withId(UUID.randomUUID().toString())
                .withEventType(type)
                .withItemId(itemId)
                .withItemName("Test Item")
                .withPreviousStock(10)
                .withNewStock(20)
                .withTimestamp(Instant.now())
                .withPerformedBy("test");
    }
}
