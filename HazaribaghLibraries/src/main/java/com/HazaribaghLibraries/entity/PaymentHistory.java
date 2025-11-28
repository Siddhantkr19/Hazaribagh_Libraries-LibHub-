package com.HazaribaghLibraries.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payment_history")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;

    private Double amount;
    private String paymentId; // UPI Transaction ID
    private LocalDateTime paymentDate;
    private String status; // SUCCESS
}