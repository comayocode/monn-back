package com.monii.counterparty.repository;

import com.monii.counterparty.model.Counterparty;
import com.monii.counterparty.model.CounterpartyType;
import com.monii.movement.model.DebtMovement;
import com.monii.movement.model.LoanMovement;
import com.monii.movement.model.Movement;
import com.monii.movement.model.RecurrentMovement;
import com.monii.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    @Query(value = "SELECT * FROM movements WHERE counterparty_id = :counterpartyId", nativeQuery = true)
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

    // Encuentra contactos ordenados por monto de deuda (de mayor a menor)
    @Query("SELECT m.counterparty, SUM(m.amount) as totalDebt " +
            "FROM DebtMovement m " +
            "WHERE m.user = :user " +
            "GROUP BY m.counterparty " +
            "ORDER BY totalDebt DESC")
    List<Object[]> findCounterpartiesWithDebtOrderedDesc(@Param("user") User user);

}
