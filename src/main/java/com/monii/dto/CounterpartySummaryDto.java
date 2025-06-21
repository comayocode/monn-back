package com.monii.dto;

import com.monii.model.Counterparty;
import com.monii.model.CounterpartyType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CounterpartySummaryDto {
    private Long counterpartyId;
    private String counterpartyName;
    private CounterpartyType counterpartyType;
    private BigDecimal totalAmount;
    private BigDecimal pendingLoansAmount;
    private BigDecimal pendingDebtsAmount;

    // Constructor que acepta Counterparty
    public CounterpartySummaryDto(Counterparty counterparty) {
        this.counterpartyId = counterparty.getId();
        this.counterpartyName = counterparty.getName();
        this.counterpartyType = counterparty.getType();
        this.totalAmount = BigDecimal.ZERO;
        this.pendingLoansAmount = BigDecimal.ZERO;
        this.pendingDebtsAmount = BigDecimal.ZERO;
    }
}
