package com.ibm.inventory_management.services;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ibm.inventory_management.models.StockItem;
import com.ibm.inventory_management.models.StockItemAuditEntry;
class StockItemServiceTest {
    private StockItemService service;
    @BeforeEach
    void setUp() {
        service = new StockItemService();
        SecurityContextHolder.clearContext();
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("Les opérations modifient la piste d'audit avec l'utilisateur courant")
    void operations_are_audited_with_current_user() {
        authenticate("organizer", "STOCK_READ", "STOCK_WRITE", "STOCK_DELETE");
        service.addStockItem("New Item", "Brand", 10.5, 3);
        service.updateStockItem("4", null, "Brand X", 12.0, 5);
        service.deleteStockItem("4");
        List<StockItemAuditEntry> auditTrail = service.getAuditTrail();
        assertThat(auditTrail).hasSize(3);
        assertThat(auditTrail.get(0).getAction()).isEqualTo("CREATE");
        assertThat(auditTrail.get(0).getActor()).isEqualTo("organizer");
        assertThat(auditTrail.get(1).getAction()).isEqualTo("UPDATE");
        assertThat(auditTrail.get(1).getVersion()).isEqualTo(2L);
        assertThat(auditTrail.get(2).getAction()).isEqualTo("DELETE");
    }
    @Test
    @DisplayName("Le service supporte des écritures concurrentes sans dupliquer les identifiants")
    void service_supports_concurrent_writes() throws Exception {
        authenticate("organizer", "STOCK_READ", "STOCK_WRITE", "STOCK_DELETE");
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Void>> tasks = java.util.stream.IntStream.range(0, 20)
                    .mapToObj(index -> (Callable<Void>) () -> {
                        service.addStockItem("Item " + index, "Brand", 10.0 + index, index);
                        return null;
                    })
                    .collect(Collectors.toList());
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
        List<StockItem> items = service.listStockItems();
        assertThat(items).hasSize(23);
        Set<String> ids = items.stream().map(StockItem::getId).collect(Collectors.toSet());
        assertThat(ids).hasSize(items.size());
    }
    private void authenticate(String username, String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "n/a", AuthorityUtils.createAuthorityList(authorities)));
    }
}
