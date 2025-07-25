package com.monii.counterparty.dto.response;

import com.monii.counterparty.model.Counterparty;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CounterpartyDebtDto {
    private Long id;
    private String name;
    private BigDecimal totalDebtAmount;
    private BigDecimal remainingDebtAmount;

    // Constructor that accepts Counterparty
    public CounterpartyDebtDto(Counterparty counterparty) {
        this.id = counterparty.getId();
        this.name = counterparty.getName();
        this.totalDebtAmount = BigDecimal.ZERO;
        this.remainingDebtAmount = BigDecimal.ZERO;
    }
}