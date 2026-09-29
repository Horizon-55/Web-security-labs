package org.example.theminiprojectapijava.service;

import org.example.theminiprojectapijava.models.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemService {

    private final List<Item> items = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ItemService() {
        // Початкове наповнення об'єктами при старті сервера
        create(new Item(null, "Security Audit", "Перевірка заголовків безпеки HTTP та CORS", "Security"));
        create(new Item(null, "Auth Module", "Налаштування авторизації та ролей користувачів", "Backend"));
        create(new Item(null, "Input Validation", "Захист форм від XSS та ін'єкцій", "Frontend"));
    }

    // READ ALL — отримати всі об'єкти
    public List<Item> findAll() {
        return items;
    }

    // READ ONE — знайти об'єкт за ID
    public Optional<Item> findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    // CREATE — додати новий об'єкт
    public Item create(Item item) {
        item.setId(idCounter.getAndIncrement());
        items.add(item);
        return item;
    }

    // CREATE BATCH — додати одразу список об'єктів
    public List<Item> createBatch(List<Item> newItems) {
        List<Item> created = new ArrayList<>();
        for (Item item : newItems) {
            created.add(create(item));
        }
        return created;
    }

    // UPDATE — оновити існуючий об'єкт за ID
    public Optional<Item> update(Long id, Item updatedItem) {
        return findById(id).map(existingItem -> {
            existingItem.setName(updatedItem.getName());
            existingItem.setDescription(updatedItem.getDescription());
            existingItem.setCategory(updatedItem.getCategory());
            return existingItem;
        });
    }

    // DELETE — видалити об'єкт за ID
    public boolean delete(Long id) {
        return items.removeIf(item -> item.getId().equals(id));
    }
}