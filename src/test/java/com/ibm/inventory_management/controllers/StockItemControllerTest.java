package com.ibm.inventory_management.controllers;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ibm.inventory_management.app.Application;
import com.ibm.inventory_management.models.StockItem;
import com.ibm.inventory_management.models.StockItemAuditEntry;
import com.ibm.inventory_management.services.StockItemApi;
@SpringBootTest(classes = Application.class, properties = {
        "openapi.id=test-api",
        "openapi.title=Test API",
        "openapi.description=Test API description"
})
@AutoConfigureMockMvc
@DisplayName("StockItemController")
class StockItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StockItemApi service;
    @Test
    @DisplayName("Quand aucun identifiant n'est fourni, l'accès est refusé")
    void unauthenticated_requests_are_rejected() throws Exception {
        mockMvc.perform(get("/stock-items"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test
    @DisplayName("Un fournisseur peut lire les stocks")
    void stock_reader_can_list_items() throws Exception {
        when(service.listStockItems()).thenReturn(List.of(new StockItem("1").withName("Item A")));
        mockMvc.perform(get("/stock-items").with(httpBasic("supplier", "supplier-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Item A"));
    }
    @Test
    @DisplayName("Un partenaire ne peut pas créer de stock")
    void partner_cannot_create_stock() throws Exception {
        mockMvc.perform(post("/stock-item")
                        .with(httpBasic("partner", "partner-pass"))
                        .param("name", "New Item")
                        .param("manufacturer", "Brand")
                        .param("price", "10.5")
                        .param("stock", "3"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test
    @DisplayName("Un organisateur peut créer un stock")
    void organizer_can_create_stock() throws Exception {
        mockMvc.perform(post("/stock-item")
                        .with(httpBasic("organizer", "organizer-pass"))
                        .param("name", "New Item")
                        .param("manufacturer", "Brand")
                        .param("price", "10.5")
                        .param("stock", "3"))
                .andExpect(status().isOk());
        verify(service).addStockItem("New Item", "Brand", 10.5f, 3);
    }
    @Test
    @DisplayName("Un auditeur peut consulter la piste d'audit")
    void auditor_can_read_audit_trail() throws Exception {
        when(service.getAuditTrail()).thenReturn(List.of(new StockItemAuditEntry(
                Instant.parse("2026-01-01T00:00:00Z"),
                "CREATE",
                "auditor",
                List.of("STOCK_AUDIT"),
                "1",
                "Item 1",
                1L,
                "Created stock item")));
        mockMvc.perform(get("/stock-items/audit").with(httpBasic("auditor", "auditor-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[0].actor").value("auditor"));
    }
    @Test
    @DisplayName("Un administrateur peut supprimer un stock")
    void admin_can_delete_stock() throws Exception {
        mockMvc.perform(delete("/stock-item/1").with(httpBasic("admin", "admin-pass")))
                .andExpect(status().isOk());
        verify(service).deleteStockItem("1");
    }
    @Test
    @DisplayName("Un organisateur peut mettre à jour partiellement un stock")
    void organizer_can_update_stock_partially() throws Exception {
        mockMvc.perform(put("/stock-item/1")
                        .with(httpBasic("organizer", "organizer-pass"))
                        .param("stock", "25"))
                .andExpect(status().isOk());
        verify(service).updateStockItem("1", null, null, null, 25);
    }
}
