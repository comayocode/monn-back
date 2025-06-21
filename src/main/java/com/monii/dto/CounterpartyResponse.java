package com.monii.dto;

import com.monii.model.Counterparty;
import com.monii.model.CounterpartyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterpartyResponse {

    private Long id;
    private String name;
    private CounterpartyType type;
    private String taxId;
    private String email;
    private String phone;

    public CounterpartyResponse(Counterparty counterparty) {
        this.id = counterparty.getId();
        this.name = counterparty.getName();
        this.type = counterparty.getType();
        this.taxId = counterparty.getTaxId();
        this.email = counterparty.getEmail();
        this.phone = counterparty.getPhone();
    }
}
