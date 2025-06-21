package com.monii.model;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Préstamos que solicito
@Entity
@DiscriminatorValue("LOAN")
@Getter
@Setter
public class LoanMovement extends Movement {
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
