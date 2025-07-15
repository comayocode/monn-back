package com.monii.movement.dto.request;

import com.monii.movement.model.MovementStatus;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDebtMovementUpdateRequest extends MovementUpdateRequest {
    private Long counterpartyId;
    
    @Future
    private LocalDateTime dueDate;
    
    private MovementStatus status;
    
    private BigDecimal remainingAmount;
    
    private Boolean isPaid;
}