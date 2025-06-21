package com.monii.dto;

import com.monii.model.CounterpartyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterpartyRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    private CounterpartyType type;

    private String taxId;

    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    private String phone;
}
