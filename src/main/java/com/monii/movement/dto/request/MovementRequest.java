package com.monii.movement.dto.request;

import com.monii.movement.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicMovementRequest.class, name = "INCOME"),
        @JsonSubTypes.Type(value = BasicMovementRequest.class, name = "EXPENSE"),
        @JsonSubTypes.Type(value = LoanDebtMovementRequest.class, name = "LOAN"),
        @JsonSubTypes.Type(value = LoanDebtMovementRequest.class, name = "DEBT"),
        @JsonSubTypes.Type(value = RecurrentMovementRequest.class, name = "RECURRENT")
})
@Getter
@Setter
@NoArgsConstructor
public abstract class MovementRequest {
    @NotNull
    private MovementType type;

    @NotNull @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;
}