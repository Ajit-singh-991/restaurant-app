package com.restaurant.service;

import com.restaurant.dto.BulkUploadResult;
import com.restaurant.dto.MenuItemDto;
import com.restaurant.entity.Category;
import com.restaurant.entity.KitchenStation;
import com.restaurant.entity.MenuItem;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.KitchenStationRepository;
import com.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final KitchenStationRepository kitchenStationRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForAdmin() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableItems() {
        return menuItemRepository.findByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAllItemsForAdmin() {
        return menuItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    @Transactional(readOnly = true)
    public MenuItem getItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
    }

    @Transactional(readOnly = true)
    public List<MenuItem> searchItems(String query) {
        return menuItemRepository.findByNameContainingIgnoreCase(query);
    }

    @Transactional
    public MenuItem createItem(MenuItemDto.CreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        KitchenStation station = request.getStationId() != null
                ? kitchenStationRepository.findById(request.getStationId()).orElse(null)
                : null;
        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .vegetarian(request.getVegetarian() != null ? request.getVegetarian() : false)
                .vegan(request.getVegan() != null ? request.getVegan() : false)
                .glutenFree(request.getGlutenFree() != null ? request.getGlutenFree() : false)
                .allergens(request.getAllergens())
                .station(station)
                .build();

        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem updateItem(Long id, MenuItemDto.CreateRequest request) {
        MenuItem item = getItemById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(category);
        item.setImageUrl(request.getImageUrl());
        item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        if (request.getVegetarian() != null) item.setVegetarian(request.getVegetarian());
        if (request.getVegan() != null) item.setVegan(request.getVegan());
        if (request.getGlutenFree() != null) item.setGlutenFree(request.getGlutenFree());
        if (request.getAllergens() != null) item.setAllergens(request.getAllergens());
        if (request.getStationId() != null) {
            item.setStation(kitchenStationRepository.findById(request.getStationId()).orElse(null));
        } else {
            item.setStation(null);
        }

        return menuItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Transactional
    public MenuItem toggleAvailability(Long id) {
        MenuItem item = getItemById(id);
        item.setAvailable(!item.getAvailable());
        return menuItemRepository.save(item);
    }

    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updates) {
        Category category = getCategoryById(id);
        if (updates.getName() != null) category.setName(updates.getName());
        if (updates.getDescription() != null) category.setDescription(updates.getDescription());
        category.setImageUrl(updates.getImageUrl());
        if (updates.getDisplayOrder() != null) category.setDisplayOrder(updates.getDisplayOrder());
        if (updates.getActive() != null) category.setActive(updates.getActive());
        return categoryRepository.save(category);
    }

    @Transactional
    public void reorderCategories(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Category category = getCategoryById(ids.get(i));
            category.setDisplayOrder(i);
            categoryRepository.save(category);
        }
    }

    /**
     * Bulk create menu items from CSV. Header: name,description,price,categoryId,imageUrl,preparationTimeMinutes,vegetarian,vegan,glutenFree,allergens,stationId
     * Optional columns can be empty. vegetarian,vegan,glutenFree are true/false.
     */
    @Transactional
    public BulkUploadResult bulkCreateFromCsv(MultipartFile file) {
        BulkUploadResult result = new BulkUploadResult();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.toLowerCase().contains("name")) {
                result.setErrors(List.of("Invalid CSV: missing header row with 'name'"));
                return result;
            }
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                if (parts.length < 4) {
                    result.setFailed(result.getFailed() + 1);
                    result.getErrors().add("Row " + row + ": need at least name,description,price,categoryId");
                    continue;
                }
                try {
                    MenuItemDto.CreateRequest req = new MenuItemDto.CreateRequest();
                    req.setName(parts[0].trim());
                    req.setDescription(parts.length > 1 ? parts[1].trim() : null);
                    req.setPrice(new BigDecimal(parts[2].trim()));
                    req.setCategoryId(Long.parseLong(parts[3].trim()));
                    req.setImageUrl(parts.length > 4 && !parts[4].isEmpty() ? parts[4].trim() : null);
                    req.setPreparationTimeMinutes(parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5].trim()) : null);
                    req.setVegetarian(parts.length > 6 && "true".equalsIgnoreCase(parts[6].trim()));
                    req.setVegan(parts.length > 7 && "true".equalsIgnoreCase(parts[7].trim()));
                    req.setGlutenFree(parts.length > 8 && "true".equalsIgnoreCase(parts[8].trim()));
                    req.setAllergens(parts.length > 9 && !parts[9].isEmpty() ? parts[9].trim() : null);
                    req.setStationId(parts.length > 10 && !parts[10].isEmpty() ? Long.parseLong(parts[10].trim()) : null);
                    createItem(req);
                    result.setCreated(result.getCreated() + 1);
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.getErrors().add("Row " + row + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.getErrors().add("File read error: " + e.getMessage());
        }
        return result;
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if ((c == ',' && !inQuotes) || c == '\n' || c == '\r') {
                out.add(cur.toString());
                cur = new StringBuilder();
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(String[]::new);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
