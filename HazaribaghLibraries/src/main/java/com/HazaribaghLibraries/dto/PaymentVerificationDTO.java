package com.HazaribaghLibraries.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentVerificationDTO {
    // These 3 fields come directly from Razorpay's success response
//    @NotBlank(message = "Razorpay Order ID missing")
    private String razorpayOrderId;

//    @NotBlank(message = "Razorpay Payment ID missing")
    private String razorpayPaymentId;

//    @NotBlank(message = "Signature missing")
    private String razorpaySignature;

    // We also need the user's email to know whose booking to update
    private String userEmail;
}