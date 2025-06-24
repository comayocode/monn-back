package com.monii.movement.dto.response;

import com.monii.movement.model.Movement;
import com.monii.movement.model.MovementType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovementResponse {
    private Long id;
    private MovementType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    public MovementResponse(Movement movement) {
        this(movement.getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getDescription(),
                movement.getCreatedAt());
    }
}