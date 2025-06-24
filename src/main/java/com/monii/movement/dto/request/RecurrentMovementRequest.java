package com.monii.movement.dto.request;

import com.monii.movement.model.RecurrenceFrequency;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurrentMovementRequest extends MovementRequest {
    @NotNull
    private RecurrenceFrequency frequency;

    @NotNull @FutureOrPresent
    private LocalDateTime startDate;

    @Future
    private LocalDateTime endDate;

    private Long counterpartyId;
}
