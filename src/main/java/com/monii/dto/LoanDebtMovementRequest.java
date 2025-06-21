package com.monii.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDebtMovementRequest extends MovementRequest {
    @NotNull
    private Long counterpartyId;

    @NotNull @Future
    private LocalDateTime dueDate;
}
