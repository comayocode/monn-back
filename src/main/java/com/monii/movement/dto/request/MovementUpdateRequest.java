package com.monii.movement.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.monii.movement.model.MovementStatus;
import com.monii.movement.model.MovementType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicMovementUpdateRequest.class, name = "INCOME"),
        @JsonSubTypes.Type(value = BasicMovementUpdateRequest.class, name = "EXPENSE"),
        @JsonSubTypes.Type(value = LoanDebtMovementUpdateRequest.class, name = "LOAN"),
        @JsonSubTypes.Type(value = LoanDebtMovementUpdateRequest.class, name = "DEBT"),
        @JsonSubTypes.Type(value = RecurrentMovementUpdateRequest.class, name = "RECURRENT")
})
@Getter
@Setter
@NoArgsConstructor
public abstract class MovementUpdateRequest {
    private MovementType type;

    @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;
}