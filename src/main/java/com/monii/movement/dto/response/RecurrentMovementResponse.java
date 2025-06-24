package com.monii.movement.dto.response;

import com.monii.counterparty.dto.response.CounterpartySummaryDto;
import com.monii.movement.model.RecurrentMovement;
import lombok.Getter;
import lombok.Setter;
import com.monii.movement.model.RecurrenceFrequency;

import java.time.LocalDateTime;

@Getter
@Setter
public class RecurrentMovementResponse extends MovementResponse {
    private final RecurrenceFrequency frequency;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Boolean isActive;
    private final CounterpartySummaryDto counterparty;

    public RecurrentMovementResponse(RecurrentMovement movement) {
        super(movement);
        this.frequency = movement.getFrequency();
        this.startDate = movement.getStartDate();
        this.endDate = movement.getEndDate();
        this.isActive = movement.getIsActive();
        this.counterparty = movement.getCounterparty() != null
                ? new CounterpartySummaryDto(movement.getCounterparty())
                : null;
    }
}