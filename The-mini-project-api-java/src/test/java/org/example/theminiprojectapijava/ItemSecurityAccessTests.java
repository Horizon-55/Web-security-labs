package org.example.theminiprojectapijava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Лабораторна робота №7 (Лекція №8: Безпекове тестування в Spring Security).
 * 10 модульних/інтеграційних JUnit 5 тестів контролю доступу через MockMvc.
 * Кожен тест перевіряє конкретне правило ролі (0.5 бали за тест = 5.0 балів).
 */
@SpringBootTest
public class ItemSecurityAccessTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 1 / 0.5 б: Читання всіх об'єктів доступне для звичайного користувача
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1. [USER] GET /api/items -> 200 OK")
    @WithMockUser(username = "user", roles = {"USER"})
    void user_canReadAllItems_status200() throws Exception {
        mockMvc.perform(get("/api/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 2 / 0.5 б: Читання одного об'єкта доступне для менеджера
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2. [MANAGER] GET /api/items/1 -> 200 OK")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void manager_canReadItemById_status200() throws Exception {
        mockMvc.perform(get("/api/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 3 / 0.5 б: Читання одного об'єкта доступне для адміністратора
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("3. [ADMIN] GET /api/items/2 -> 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_canReadItemById_status200() throws Exception {
        mockMvc.perform(get("/api/items/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 4 / 0.5 б: Створення одиничного об'єкта дозволено менеджеру
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4. [MANAGER] POST /api/items -> 201 Created")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void manager_canCreateSingleItem_status201() throws Exception {
        String itemJson = """
                {
                    "name": "JUnit Security Item",
                    "description": "Created during Lab 7 testing",
                    "category": "Testing"
                }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 5 / 0.5 б: Створення одиничного об'єкта ЗАБОРОНЕНО звичайному користувачу
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("5. [USER] POST /api/items -> 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void user_cannotCreateItem_status403() throws Exception {
        String itemJson = """
                {
                    "name": "Hacker Item",
                    "description": "Should be blocked by @PreAuthorize",
                    "category": "Exploit"
                }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 6 / 0.5 б: Пакетне створення (batch) ЗАБОРОНЕНО для менеджера
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("6. [MANAGER] POST /api/items/batch -> 403 Forbidden")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void manager_cannotCreateBatch_status403() throws Exception {
        String batchJson = """
                [
                    {"name": "Batch Item 1", "description": "Desc 1", "category": "Test"},
                    {"name": "Batch Item 2", "description": "Desc 2", "category": "Test"}
                ]
                """;

        mockMvc.perform(post("/api/items/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 7 / 0.5 б: Пакетне створення (batch) дозволено адміністратору
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("7. [ADMIN] POST /api/items/batch -> 201 Created")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_canCreateBatch_status201() throws Exception {
        String batchJson = """
                [
                    {"name": "Admin Batch 1", "description": "Allowed", "category": "Admin"},
                    {"name": "Admin Batch 2", "description": "Allowed", "category": "Admin"}
                ]
                """;

        mockMvc.perform(post("/api/items/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 8 / 0.5 б: Оновлення об'єкта ЗАБОРОНЕНО для звичайного користувача
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("8. [USER] PUT /api/items/1 -> 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void user_cannotUpdateItem_status403() throws Exception {
        String updateJson = """
                {
                    "name": "Tampered Name",
                    "description": "User unauthorized modification",
                    "category": "Attack"
                }
                """;

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 9 / 0.5 б: Видалення об'єкта ЗАБОРОНЕНО для менеджера
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("9. [MANAGER] DELETE /api/items/1 -> 403 Forbidden")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void manager_cannotDeleteItem_status403() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // ТЕСТ 10 / 0.5 б: Видалення об'єкта дозволено ВИКЛЮЧНО адміністратору
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("10. [ADMIN] DELETE /api/items/3 -> 204 No Content")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_canDeleteItem_status204() throws Exception {
        mockMvc.perform(delete("/api/items/3"))
                .andExpect(status().isNoContent());
    }
}
