package com.restaurant.service;

import com.restaurant.entity.RestaurantTable;
import com.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> getAvailableTables() {
        return tableRepository.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> getTablesBySection(String section) {
        return tableRepository.findBySection(section);
    }

    @Transactional
    public RestaurantTable createTable(RestaurantTable table) {
        if (tableRepository.findByTableNumber(table.getTableNumber()).isPresent()) {
            throw new IllegalArgumentException("Table number already exists");
        }
        return tableRepository.save(table);
    }

    @Transactional
    public RestaurantTable updateStatus(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = getTableById(id);
        table.setStatus(status);
        RestaurantTable saved = tableRepository.save(table);
        notificationService.notifyTableStatusChanged(id, status.name());
        return saved;
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = getTableById(id);
        tableRepository.delete(table);
    }
}
