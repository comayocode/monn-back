package com.monii.model;

import com.monii.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movements")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Mantenemos tabla única para simplificar agregaciones
@DiscriminatorColumn(name = "movement_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Movement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Campo discriminador para consultas SQL nativas
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", insertable = false, updatable = false)
    private MovementType type;

    public abstract void validate();
}
