package com.restaurant.controller;

import com.restaurant.dto.BulkUploadResult;
import com.restaurant.dto.MenuItemDto;
import com.restaurant.entity.Category;
import com.restaurant.entity.MenuItem;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Category>> getAdminCategories() {
        return ResponseEntity.ok(menuService.getAllCategoriesForAdmin());
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItem>> getAvailableItems() {
        return ResponseEntity.ok(menuService.getAvailableItems());
    }

    @GetMapping("/admin/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuItem>> getAdminItems() {
        return ResponseEntity.ok(menuService.getAllItemsForAdmin());
    }

    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItem>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getItemsByCategory(categoryId));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItem> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getItemById(id));
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<MenuItem>> searchItems(@RequestParam String q) {
        return ResponseEntity.ok(menuService.searchItems(q));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> createItem(@Valid @RequestBody MenuItemDto.CreateRequest request) {
        return ResponseEntity.ok(menuService.createItem(request));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> updateItem(@PathVariable Long id,
                                                @Valid @RequestBody MenuItemDto.CreateRequest request) {
        return ResponseEntity.ok(menuService.updateItem(id, request));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.toggleAvailability(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        return ResponseEntity.ok(menuService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                    @Valid @RequestBody Category category) {
        return ResponseEntity.ok(menuService.updateCategory(id, category));
    }

    @PutMapping("/categories/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reorderCategories(@RequestBody List<Long> ids) {
        menuService.reorderCategories(ids);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/admin/items/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResult> bulkUploadItems(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(menuService.bulkCreateFromCsv(file));
    }
}
