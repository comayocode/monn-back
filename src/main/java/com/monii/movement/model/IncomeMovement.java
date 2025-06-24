package com.monii.movement.model;
import com.monii.movement.model.Movement;
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
