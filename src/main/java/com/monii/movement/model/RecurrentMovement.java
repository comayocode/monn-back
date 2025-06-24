package com.monii.movement.model;
import com.monii.counterparty.model.Counterparty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@DiscriminatorValue("RECURRENT")
@Getter
@Setter
public class RecurrentMovement extends Movement {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Counterparty counterparty;

    @Override
    public void validate() {
        if (startDate == null) {
            throw new IllegalArgumentException("Fecha de inicio es requerida");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("Frecuencia es requerida");
        }
    }
}
