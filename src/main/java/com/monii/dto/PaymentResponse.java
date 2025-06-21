package com.monii.dto;

import com.monii.model.PaymentRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long movementId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime paymentDate;
    private BigDecimal remainingAmount; // Monto restante después del pago

    public PaymentResponse(PaymentRecord payment, BigDecimal remainingAmount) {
        this.id = payment.getId();
        this.movementId = payment.getMovement().getId();
        this.amount = payment.getAmount();
        this.description = payment.getDescription();
        this.paymentDate = payment.getPaymentDate();
        this.remainingAmount = remainingAmount;
    }
}
