package com.HazaribaghLibraries.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentHistoryDTO {
    private String paymentId;
    private Double amount;
    private LocalDateTime paymentDate;
    private String libraryName;
    private String Status;

}