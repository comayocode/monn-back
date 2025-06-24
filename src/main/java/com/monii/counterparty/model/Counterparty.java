package com.monii.counterparty.model;

import com.monii.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "counterparties")
@Getter
@Setter
public class Counterparty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private CounterpartyType type; // PERSON, COMPANY, PLATFORM

    private String taxId;
    private String email;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
}

