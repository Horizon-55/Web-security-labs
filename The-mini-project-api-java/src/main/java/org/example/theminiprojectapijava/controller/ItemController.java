package org.example.theminiprojectapijava.controller;

import org.example.theminiprojectapijava.models.Item;
import org.example.theminiprojectapijava.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 1. GET /api/items — Отримати всі об'єкти
    @GetMapping
    public List<Item> getAllItems() {
        return itemService.findAll();
    }

    // 2. GET /api/items/{id} — Отримати один об'єкт за ID
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST /api/items — Додати один новий об'єкт
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item created = itemService.create(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. POST /api/items/batch — Додати масив об'єктів за раз
    @PostMapping("/batch")
    public ResponseEntity<List<Item>> createItemsBatch(@RequestBody List<Item> items) {
        List<Item> created = itemService.createBatch(items);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 5. PUT /api/items/{id} — Оновити об'єкт за ID
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemService.update(id, item)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. DELETE /api/items/{id} — Видалити об'єкт за ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
