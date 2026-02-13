package com.restaurant.service;

import com.restaurant.entity.RestaurantTable;
import com.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TableService tableService;

    private RestaurantTable testTable;
    private RestaurantTable testTable2;

    @BeforeEach
    void setUp() {
        testTable = RestaurantTable.builder()
                .id(1L)
                .tableNumber(5)
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .section("Main Hall")
                .build();

        testTable2 = RestaurantTable.builder()
                .id(2L)
                .tableNumber(10)
                .capacity(6)
                .status(RestaurantTable.TableStatus.OCCUPIED)
                .section("Patio")
                .build();
    }

    @Test
    void getAllTables_returnsAllTables() {
        when(tableRepository.findAll()).thenReturn(List.of(testTable, testTable2));

        List<RestaurantTable> result = tableService.getAllTables();

        assertEquals(2, result.size());
        assertEquals(testTable, result.get(0));
        assertEquals(testTable2, result.get(1));
        verify(tableRepository).findAll();
    }

    @Test
    void getAllTables_returnsEmptyList() {
        when(tableRepository.findAll()).thenReturn(List.of());

        List<RestaurantTable> result = tableService.getAllTables();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableTables_returnsOnlyAvailableTables() {
        when(tableRepository.findByStatus(RestaurantTable.TableStatus.AVAILABLE))
                .thenReturn(List.of(testTable));

        List<RestaurantTable> result = tableService.getAvailableTables();

        assertEquals(1, result.size());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, result.get(0).getStatus());
        assertEquals(5, result.get(0).getTableNumber());
    }

    @Test
    void getAvailableTables_returnsEmptyWhenNoneAvailable() {
        when(tableRepository.findByStatus(RestaurantTable.TableStatus.AVAILABLE))
                .thenReturn(List.of());

        List<RestaurantTable> result = tableService.getAvailableTables();

        assertTrue(result.isEmpty());
    }

    @Test
    void getTableById_withValidId_returnsTable() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));

        RestaurantTable result = tableService.getTableById(1L);

        assertEquals(testTable, result);
        assertEquals(5, result.getTableNumber());
        assertEquals(4, result.getCapacity());
        assertEquals("Main Hall", result.getSection());
    }

    @Test
    void getTableById_withInvalidId_throwsException() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tableService.getTableById(999L));

        assertTrue(ex.getMessage().contains("Table not found"));
    }

    @Test
    void createTable_withValidTable_createsSuccessfully() {
        RestaurantTable newTable = RestaurantTable.builder()
                .tableNumber(15)
                .capacity(2)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .section("VIP")
                .build();

        when(tableRepository.findByTableNumber(15)).thenReturn(Optional.empty());
        when(tableRepository.save(newTable)).thenAnswer(inv -> {
            RestaurantTable t = inv.getArgument(0);
            t.setId(3L);
            return t;
        });

        RestaurantTable result = tableService.createTable(newTable);

        assertNotNull(result);
        assertEquals(15, result.getTableNumber());
        assertEquals(2, result.getCapacity());
        verify(tableRepository).save(newTable);
    }

    @Test
    void createTable_withDuplicateTableNumber_throwsException() {
        RestaurantTable duplicateTable = RestaurantTable.builder()
                .tableNumber(5)
                .capacity(4)
                .build();

        when(tableRepository.findByTableNumber(5)).thenReturn(Optional.of(testTable));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tableService.createTable(duplicateTable));

        assertTrue(ex.getMessage().contains("Table number already exists"));
        verify(tableRepository, never()).save(any());
    }

    @Test
    void updateStatus_updatesStatusAndNotifies() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(testTable);

        RestaurantTable result = tableService.updateStatus(1L, RestaurantTable.TableStatus.OCCUPIED);

        assertEquals(RestaurantTable.TableStatus.OCCUPIED, result.getStatus());
        verify(tableRepository).save(testTable);
        verify(notificationService).notifyTableStatusChanged(1L, "OCCUPIED");
    }

    @Test
    void updateStatus_toMaintenance_updatesAndNotifies() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(testTable);

        RestaurantTable result = tableService.updateStatus(1L, RestaurantTable.TableStatus.MAINTENANCE);

        assertEquals(RestaurantTable.TableStatus.MAINTENANCE, result.getStatus());
        verify(notificationService).notifyTableStatusChanged(1L, "MAINTENANCE");
    }

    @Test
    void updateStatus_withInvalidId_throwsException() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> tableService.updateStatus(999L, RestaurantTable.TableStatus.OCCUPIED));

        verify(tableRepository, never()).save(any());
        verify(notificationService, never()).notifyTableStatusChanged(any(), any());
    }

    @Test
    void deleteTable_withValidId_deletesTable() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));

        tableService.deleteTable(1L);

        verify(tableRepository).delete(testTable);
    }

    @Test
    void deleteTable_withInvalidId_throwsException() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tableService.deleteTable(999L));

        verify(tableRepository, never()).delete(any(RestaurantTable.class));
    }
}
