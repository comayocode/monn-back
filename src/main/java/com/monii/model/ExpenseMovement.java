package com.monii.model;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

// Egresos
@Entity
@DiscriminatorValue("EXPENSE")
public class ExpenseMovement extends Movement {
    @Override
    public void validate() {

    }
}