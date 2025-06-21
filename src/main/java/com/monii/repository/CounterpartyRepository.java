package com.monii.repository;

import com.monii.model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
//    // Encontrar personas de un usuario con sus movimientos
//    @EntityGraph(attributePaths = {"movements"})
//    List<Counterparty> findByUser(User user);
//
//    // Verificar si una persona pertenece al usuario
//    boolean existsByIdAndUser(Long id, User user);
//    // Método nuevo para buscar por ID y User
//    Optional<Counterparty> findByIdAndUser(Long id, User user);

    // =====================
        List<Counterparty> findByOwnerId(Long ownerId);

        @Query("SELECT c FROM Counterparty c WHERE c.owner.id = :ownerId AND c.type = :type")
        List<Counterparty> findByOwnerIdAndType(@Param("ownerId") Long ownerId, @Param("type") CounterpartyType type);

    // Encuentra todos los movimientos asociados a un contacto específico
    @Query("SELECT m FROM Movement m WHERE m.counterparty.id = :counterpartyId")
    List<Movement> findByCounterpartyId(@Param("counterpartyId") Long counterpartyId);

    // Encuentra préstamos de un usuario con un contacto específico
    @Query("SELECT l FROM LoanMovement l WHERE l.user = :user AND l.counterparty = :counterparty")
    List<LoanMovement> findLoansByUserAndCounterparty(
            @Param("user") User user,
            @Param("counterparty") Counterparty counterparty);

    // Encuentra deudas de un usuario con un contacto específico
    @Query("SELECT d FROM DebtMovement d WHERE d.user = :user AND d.counterparty = :counterparty")
    List<DebtMovement> findDebtsByUserAndCounterparty(
            @Param("user") User user,
            @Param("counterparty") Counterparty counterparty);

    // Encuentra movimientos recurrentes de un usuario con un contacto específico
    @Query("SELECT r FROM RecurrentMovement r WHERE r.user = :user AND r.counterparty = :counterparty AND r.isActive = true")
    List<RecurrentMovement> findActiveRecurrentMovementsByUserAndCounterparty(
            @Param("user") User user,
            @Param("counterparty") Counterparty counterparty);

    List<Counterparty> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String query);


}