package com.HazaribaghLibraries.dto;

import lombok.Data;

@Data
public class PaymentVerificationDTO {
    // These 3 fields come directly from Razorpay's success response
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // We also need the user's email to know whose booking to update
    private String userEmail;
}