package com.restaurant.service;

import com.restaurant.dto.MenuItemDto;
import com.restaurant.entity.Category;
import com.restaurant.entity.MenuItem;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.KitchenStationRepository;
import com.restaurant.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private KitchenStationRepository kitchenStationRepository;

    @InjectMocks
    private MenuService menuService;

    private Category testCategory;
    private MenuItem testItem;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Main Course")
                .description("Main dishes")
                .displayOrder(1)
                .active(true)
                .build();

        testItem = MenuItem.builder()
                .id(1L)
                .name("Butter Chicken")
                .description("Creamy tomato-based chicken curry")
                .price(new BigDecimal("350.00"))
                .category(testCategory)
                .available(true)
                .vegetarian(false)
                .vegan(false)
                .glutenFree(true)
                .preparationTimeMinutes(25)
                .build();
    }

    @Test
    void getAllCategories_returnsActiveCategories() {
        when(categoryRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(testCategory));

        List<Category> result = menuService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Main Course", result.get(0).getName());
    }

    @Test
    void getAvailableItems_returnsAvailableItems() {
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of(testItem));

        List<MenuItem> result = menuService.getAvailableItems();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void getItemsByCategory_returnsItemsForCategory() {
        when(menuItemRepository.findByCategoryIdAndAvailableTrue(1L))
                .thenReturn(List.of(testItem));

        List<MenuItem> result = menuService.getItemsByCategory(1L);

        assertEquals(1, result.size());
        assertEquals("Butter Chicken", result.get(0).getName());
    }

    @Test
    void getItemById_withValidId_returnsItem() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        MenuItem result = menuService.getItemById(1L);

        assertEquals("Butter Chicken", result.getName());
        assertEquals(new BigDecimal("350.00"), result.getPrice());
    }

    @Test
    void getItemById_withInvalidId_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> menuService.getItemById(999L));
    }

    @Test
    void searchItems_returnsMatchingItems() {
        when(menuItemRepository.findByNameContainingIgnoreCase("butter"))
                .thenReturn(List.of(testItem));

        List<MenuItem> result = menuService.searchItems("butter");

        assertEquals(1, result.size());
        assertEquals("Butter Chicken", result.get(0).getName());
    }

    @Test
    void createItem_withValidRequest_createsItem() {
        MenuItemDto.CreateRequest request = new MenuItemDto.CreateRequest();
        request.setName("Dal Makhani");
        request.setDescription("Creamy black lentils");
        request.setPrice(new BigDecimal("250.00"));
        request.setCategoryId(1L);
        request.setVegetarian(true);
        request.setVegan(false);
        request.setGlutenFree(true);
        request.setPreparationTimeMinutes(20);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> {
            MenuItem item = inv.getArgument(0);
            item.setId(2L);
            return item;
        });

        MenuItem result = menuService.createItem(request);

        assertNotNull(result);
        assertEquals("Dal Makhani", result.getName());
        assertEquals(new BigDecimal("250.00"), result.getPrice());
        assertTrue(result.getVegetarian());
        assertTrue(result.getGlutenFree());
        assertEquals(testCategory, result.getCategory());
    }

    @Test
    void createItem_withInvalidCategory_throwsException() {
        MenuItemDto.CreateRequest request = new MenuItemDto.CreateRequest();
        request.setName("Test");
        request.setPrice(new BigDecimal("100.00"));
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> menuService.createItem(request));
        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void createItem_withNullDietaryFlags_defaultsToFalse() {
        MenuItemDto.CreateRequest request = new MenuItemDto.CreateRequest();
        request.setName("Test Item");
        request.setPrice(new BigDecimal("100.00"));
        request.setCategoryId(1L);
        // vegetarian, vegan, glutenFree all null

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = menuService.createItem(request);

        assertFalse(result.getVegetarian());
        assertFalse(result.getVegan());
        assertFalse(result.getGlutenFree());
    }

    @Test
    void updateItem_withValidRequest_updatesFields() {
        MenuItemDto.CreateRequest request = new MenuItemDto.CreateRequest();
        request.setName("Butter Chicken Deluxe");
        request.setDescription("Premium version");
        request.setPrice(new BigDecimal("450.00"));
        request.setCategoryId(1L);
        request.setPreparationTimeMinutes(30);
        request.setVegetarian(false);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);

        MenuItem result = menuService.updateItem(1L, request);

        assertEquals("Butter Chicken Deluxe", result.getName());
        assertEquals("Premium version", result.getDescription());
        assertEquals(new BigDecimal("450.00"), result.getPrice());
        assertEquals(30, result.getPreparationTimeMinutes());
    }

    @Test
    void toggleAvailability_togglesFromTrueToFalse() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);

        assertTrue(testItem.getAvailable());

        MenuItem result = menuService.toggleAvailability(1L);

        assertFalse(result.getAvailable());
        verify(menuItemRepository).save(testItem);
    }

    @Test
    void toggleAvailability_togglesFromFalseToTrue() {
        testItem.setAvailable(false);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);

        MenuItem result = menuService.toggleAvailability(1L);

        assertTrue(result.getAvailable());
    }

    @Test
    void deleteItem_callsRepositoryDelete() {
        menuService.deleteItem(1L);

        verify(menuItemRepository).deleteById(1L);
    }

    @Test
    void createCategory_savesCategory() {
        Category newCategory = Category.builder().name("Desserts").build();
        when(categoryRepository.save(newCategory)).thenReturn(newCategory);

        Category result = menuService.createCategory(newCategory);

        assertEquals("Desserts", result.getName());
        verify(categoryRepository).save(newCategory);
    }

    @Test
    void deleteCategory_callsRepositoryDelete() {
        menuService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
