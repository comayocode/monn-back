package com.monii.movement.dto.response;

import com.monii.counterparty.dto.response.CounterpartySummaryDto;
import com.monii.movement.model.LoanMovement;
import com.monii.movement.model.MovementStatus;
import com.monii.movement.model.MovementType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class LoanMovementResponse extends MovementResponse {
    private final CounterpartySummaryDto counterparty;
    private final LocalDateTime dueDate;
    private final Boolean isPaid;
    private final MovementStatus status;
    private final BigDecimal totalPaid;
    private final BigDecimal remainingAmount;

    public LoanMovementResponse(LoanMovement movement) {
        super(
                movement.getId(),
                MovementType.LOAN,
                movement.getAmount(),
                movement.getDescription(),
                movement.getCreatedAt()
        );
        this.counterparty = movement.getCounterparty() != null
                ? new CounterpartySummaryDto(movement.getCounterparty())
                : null;
        this.dueDate = movement.getDueDate();
        this.isPaid = movement.getIsPaid();
        this.status = movement.getStatus();
        this.totalPaid = movement.getAmount().subtract(movement.getRemainingAmount());
        this.remainingAmount = movement.getRemainingAmount();
    }
}
