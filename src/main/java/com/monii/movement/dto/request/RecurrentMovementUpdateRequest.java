package com.monii.movement.dto.request;

import com.monii.movement.model.RecurrenceFrequency;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecurrentMovementUpdateRequest extends MovementUpdateRequest {
    private Long counterpartyId;
    
    private RecurrenceFrequency frequency;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Boolean isActive;
}