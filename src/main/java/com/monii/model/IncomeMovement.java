package com.monii.model;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

// Ingresos
@Entity
@DiscriminatorValue("INCOME")
public class IncomeMovement extends Movement {
    @Override
    public void validate() {

    }
}
