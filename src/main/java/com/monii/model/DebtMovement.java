package com.monii.model;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.DiscriminatorValue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Deudas que tienen conmigo
@Entity
@DiscriminatorValue("DEBT")
@Getter
@Setter
public class DebtMovement extends Movement {
    @ManyToOne(fetch = FetchType.LAZY)
    private Counterparty counterparty;

    private BigDecimal RemainingAmount;
    private MovementStatus status;
    private LocalDateTime dueDate;
    private Boolean isPaid = false;

    @Override
    public void validate() {

    }
}
