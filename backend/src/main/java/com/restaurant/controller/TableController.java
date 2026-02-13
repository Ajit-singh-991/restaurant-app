package com.restaurant.controller;

import com.restaurant.entity.RestaurantTable;
import com.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/available")
    public ResponseEntity<List<RestaurantTable>> getAvailableTables() {
        return ResponseEntity.ok(tableService.getAvailableTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTable> getTable(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantTable> createTable(@RequestBody RestaurantTable table) {
        return ResponseEntity.ok(tableService.createTable(table));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RestaurantTable> updateStatus(@PathVariable Long id,
                                                         @RequestParam RestaurantTable.TableStatus status) {
        return ResponseEntity.ok(tableService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
