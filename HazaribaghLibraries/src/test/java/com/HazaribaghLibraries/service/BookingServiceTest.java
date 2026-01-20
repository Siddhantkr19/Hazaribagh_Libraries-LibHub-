package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
// import com.razorpay.RazorpayException; // Not strictly needed if we throw Exception, but fine to keep
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private LibraryRepository libraryRepository;
    @Mock private UserRepository userRepository;
    @Mock private RazorpayClient razorpayClient;
    @Mock private com.razorpay.OrderClient orderClient;

    @InjectMocks private BookingService bookingService;

    private User testUser;
    private Library testLibrary;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.com");

        testLibrary = new Library();
        testLibrary.setId(100L);
        testLibrary.setName("Hazaribagh Central Lib");
        testLibrary.setOfferPrice(350.0);
        testLibrary.setOriginalPrice(400.0);

        ReflectionTestUtils.setField(bookingService, "razorpayClient", razorpayClient);
        razorpayClient.orders = orderClient;
    }

    // ✅ CHANGED: throws Exception (Covers JSONException & RazorpayException)
    @Test
    void testCreateOrder_NewCustomer_CalculatesOfferPrice() throws Exception {
        // GIVEN
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        when(libraryRepository.findById(100L)).thenReturn(Optional.of(testLibrary));
        when(bookingRepository.existsByUser(testUser)).thenReturn(false);

        // ✅ This line throws JSONException, which is now caught by 'throws Exception'
        Order mockOrder = new Order(new JSONObject("{'id':'order_new_123', 'status':'created'}"));
        when(orderClient.create(any(JSONObject.class))).thenReturn(mockOrder);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createOrder("student@test.com", 100L);

        // THEN
        assertNotNull(result);
        assertEquals("order_new_123", result.getRazorpayOrderId());
        assertEquals(360.5, result.getAmountPaid(), 0.01);
    }

    // ✅ CHANGED: throws Exception
    @Test
    void testCreateOrder_OldCustomer_CalculatesOriginalPrice() throws Exception {
        // GIVEN
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        when(libraryRepository.findById(100L)).thenReturn(Optional.of(testLibrary));
        when(bookingRepository.existsByUser(testUser)).thenReturn(true);

        Order mockOrder = new Order(new JSONObject("{'id':'order_old_456', 'status':'created'}"));
        when(orderClient.create(any(JSONObject.class))).thenReturn(mockOrder);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createOrder("student@test.com", 100L);

        // THEN
        assertEquals(412.0, result.getAmountPaid(), 0.01);
    }

    @Test
    void testCreateOrder_LibraryNotFound_ThrowsException() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        when(libraryRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createOrder("student@test.com", 999L);
        });

        assertEquals("Library not found", exception.getMessage());
    }

    @Test
    void testCreateOrder_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createOrder("ghost@test.com", 100L);
        });

        assertEquals("User not found", exception.getMessage());
    }
}