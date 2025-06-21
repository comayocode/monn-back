package com.monii.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull
    private Long movementId;

    @NotNull
    @Positive(message = "El monto del pago debe ser mayor que cero")
    private BigDecimal amount;

    private String description;
}
