package com.monii.movement.dto.request;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
public class BasicMovementUpdateRequest extends MovementUpdateRequest {
    // Los movimientos básicos (INCOME, EXPENSE) no tienen campos adicionales que actualizar.
    // Solo utilizan los campos de la clase principal: ammount, description, type.
}