package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.dto.PaymentHistoryDTO;
import com.HazaribaghLibraries.dto.PaymentVerificationDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.PaymentHistoryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.aspectj.asm.IModelFilter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BookingRepository bookingRepository;
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    public BookingService(PaymentHistoryRepository paymentHistoryRepository, BookingRepository bookingRepository,
                          LibraryRepository libraryRepository,
                          UserRepository userRepository, ModelMapper modelMapper) {
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.bookingRepository = bookingRepository;
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }
    // Initialize Razorpay Client when the application starts
    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    // ========================================================================
    // STEP 1: CREATE ORDER (Calculates Fees & Talks to Razorpay)
    // ========================================================================
    @Transactional
    public Booking createOrder(String userEmail, Long libraryId) throws RazorpayException {

        // A. Validate User & Library
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library not found"));

        // B. Calculate Base Price (Old vs New Customer)
        boolean isOldCustomer = bookingRepository.existsByUser(user);
        Double basePrice = isOldCustomer ? library.getOriginalPrice() : library.getOfferPrice(); // e.g., 400.0

        // C. ADD SURCHARGE (Your requirement: User pays extra so you get full amount)
        // Adding approx 3% to cover gateway charges
        Double surcharge = basePrice * 0.03;
        Double totalAmount = basePrice + surcharge;
        // Example: Base 400 + 12 = 412. User pays 412.

        // Razorpay expects amount in PAISE (Multiply by 100)
        // 412.0 becomes 41200 paise
        int amountInPaise = (int) (Math.round(totalAmount) * 100);

        // D. Call Razorpay API
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String orderId = razorpayOrder.get("id");

        // E. Save Temporary Booking in DB (Status: PENDING)
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setLibrary(library);
        booking.setBookingDate(LocalDateTime.now());
        booking.setAmountPaid(totalAmount); // Saving 412.0
        booking.setRazorpayOrderId(orderId);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setSeatNumber("TBD"); // To Be Decided after payment

        return bookingRepository.save(booking);
    }


    // ========================================================================
    // STEP 2: VERIFY PAYMENT (Called after user pays on Frontend)
    // ========================================================================
    @Transactional
    public Booking verifyPayment(PaymentVerificationDTO verificationDTO) {

        try {
            // A. Verify Signature (Security Check)
            // It generates the signature locally and matches it with what Razorpay sent
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", verificationDTO.getRazorpayOrderId());
            options.put("razorpay_payment_id", verificationDTO.getRazorpayPaymentId());
            options.put("razorpay_signature", verificationDTO.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new RuntimeException("Payment Verification Failed: Signature Mismatch");
            }

            // B. Find the Pending Booking
            Booking booking = bookingRepository.findByRazorpayOrderId(verificationDTO.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Booking Order not found"));

            // C. Update Booking Status to CONFIRMED
            booking.setPaymentId(verificationDTO.getRazorpayPaymentId());
            booking.setStatus(Booking.BookingStatus.CONFIRMED);

            // Set Validity (e.g., 28 Days from now)
            booking.setValidUntil(LocalDateTime.now().plusDays(28));
            booking.setSeatNumber("Auto-" + (System.currentTimeMillis() % 1000)); // Assign seat logic here

            // D. Generate Receipt (Payment History)
            PaymentHistory receipt = new PaymentHistory();
            receipt.setUser(booking.getUser());
            receipt.setLibrary(booking.getLibrary());
            receipt.setAmount(booking.getAmountPaid());
            receipt.setPaymentId(booking.getPaymentId());
            receipt.setPaymentDate(LocalDateTime.now());
            receipt.setStatus("SUCCESS");
            paymentHistoryRepository.save(receipt);

            return bookingRepository.save(booking);

        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay Verification Error: " + e.getMessage());
        }
    }


    // 2. THE DASHBOARD LOGIC  for the  user
    public List<DashboardBookingDTO> getUserDashboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUser(user);

        return bookings.stream().map(booking -> {
            DashboardBookingDTO dto = new DashboardBookingDTO();
            dto.setBookingId(booking.getId());
            dto.setStatus(booking.getStatus().toString());
            dto.setAmountPaid(booking.getAmountPaid());
            dto.setSeatNumber(booking.getSeatNumber());

            // Library Details
            dto.setLibraryName(booking.getLibrary().getName());
            dto.setLibraryAddress(booking.getLibrary().getAddress());
            dto.setLibraryOwnerContact(booking.getLibrary().getContactNumber());

            // Dates
            dto.setBookingDate(booking.getBookingDate());
            dto.setValidUntil(booking.getValidUntil());

                    // Fix for null validUntil if PENDING
                    if(booking.getValidUntil() != null) {
                        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), booking.getValidUntil());
                        dto.setDaysRemaining(daysLeft > 0 ? daysLeft : 0); // calculate the remaining days
                    } else {
                        dto.setDaysRemaining(0);
                    }
            return dto;
        }

        ).collect(Collectors.toList());
    }

    // 3. GET HISTORY METHOD
    public List<PaymentHistoryDTO> getPaymentHistory(String userEmail, Long libraryId) {

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        Library library = libraryRepository.findById(libraryId).orElseThrow(() -> new RuntimeException("Library not found"));

        // 1. Fetch all Entities
        List<PaymentHistory> historyList = paymentHistoryRepository
                .findByUserAndLibraryOrderByPaymentDateDesc(user, library);

        // 2. Convert to DTOs by model mapper
        return historyList.stream().map(history ->   {
                    PaymentHistoryDTO dto = modelMapper.map(history, PaymentHistoryDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }


}