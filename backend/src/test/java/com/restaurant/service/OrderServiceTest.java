package com.restaurant.service;

import com.restaurant.dto.OrderDto;
import com.restaurant.entity.*;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.TableRepository;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private RestaurantTable testTable;
    private MenuItem testMenuItem;
    private User testWaiter;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testTable = RestaurantTable.builder()
                .id(1L)
                .tableNumber(5)
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        Category category = Category.builder().id(1L).name("Main Course").build();

        testMenuItem = MenuItem.builder()
                .id(1L)
                .name("Butter Chicken")
                .price(new BigDecimal("350.00"))
                .category(category)
                .available(true)
                .build();

        testWaiter = User.builder()
                .id(1L)
                .username("waiter1")
                .fullName("Test Waiter")
                .role(User.Role.WAITER)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20260211-0001")
                .table(testTable)
                .waiter(testWaiter)
                .status(Order.OrderStatus.PENDING)
                .subtotal(new BigDecimal("700.00"))
                .taxAmount(new BigDecimal("70.00"))
                .totalAmount(new BigDecimal("770.00"))
                .build();
    }

    @Test
    void createOrder_withValidRequest_createsOrderSuccessfully() {
        OrderDto.OrderItemRequest itemReq = new OrderDto.OrderItemRequest();
        itemReq.setMenuItemId(1L);
        itemReq.setQuantity(2);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTableId(1L);
        request.setItems(List.of(itemReq));
        request.setOrderType("DINE_IN");

        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.createOrder(request, testWaiter, null);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(new BigDecimal("700.00"), result.getSubtotal());
        assertEquals(new BigDecimal("70.0000"), result.getTaxAmount());
        assertEquals(RestaurantTable.TableStatus.OCCUPIED, testTable.getStatus());
        verify(tableRepository).save(testTable);
        verify(notificationService).notifyOrderCreated(result);
    }

    @Test
    void createOrder_withUnavailableMenuItem_throwsException() {
        testMenuItem.setAvailable(false);

        OrderDto.OrderItemRequest itemReq = new OrderDto.OrderItemRequest();
        itemReq.setMenuItemId(1L);
        itemReq.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTableId(1L);
        request.setItems(List.of(itemReq));

        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(request, testWaiter, null));

        assertTrue(ex.getMessage().contains("not available"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_withInvalidTable_throwsException() {
        OrderDto.OrderItemRequest itemReq = new OrderDto.OrderItemRequest();
        itemReq.setMenuItemId(1L);
        itemReq.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTableId(999L);
        request.setItems(List.of(itemReq));

        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(request, testWaiter, null));
    }

    @Test
    void createOrder_withInvalidMenuItem_throwsException() {
        OrderDto.OrderItemRequest itemReq = new OrderDto.OrderItemRequest();
        itemReq.setMenuItemId(999L);
        itemReq.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTableId(1L);
        request.setItems(List.of(itemReq));

        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(request, testWaiter, null));
    }

    @Test
    void getOrderById_withValidId_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById(1L);

        assertEquals(testOrder, result);
        assertEquals("ORD-20260211-0001", result.getOrderNumber());
    }

    @Test
    void getOrderById_withInvalidId_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void getActiveOrders_returnsActiveOrders() {
        List<Order> activeOrders = List.of(testOrder);
        when(orderRepository.findActiveOrders(any())).thenReturn(activeOrders);

        List<Order> result = orderService.getActiveOrders();

        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
    }

    @Test
    void updateOrderStatus_toReady_updatesStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.READY);

        assertEquals(Order.OrderStatus.READY, result.getStatus());
        assertNull(result.getCompletedAt());
        verify(notificationService).notifyOrderStatusChanged(result);
    }

    @Test
    void updateOrderStatus_toCompleted_setsCompletedAtAndFreesTable() {
        testOrder.setTable(testTable);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.COMPLETED);

        assertEquals(Order.OrderStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository).save(testTable);
    }

    @Test
    void updateOrderStatus_toCancelled_setsCompletedAtAndFreesTable() {
        testOrder.setTable(testTable);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

        assertEquals(Order.OrderStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
    }

    @Test
    void getOrdersByStatus_returnsFilteredOrders() {
        when(orderRepository.findByStatusIn(List.of(Order.OrderStatus.PREPARING)))
                .thenReturn(List.of(testOrder));

        List<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.PREPARING);

        assertEquals(1, result.size());
    }
}
