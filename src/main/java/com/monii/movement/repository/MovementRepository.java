package com.monii.movement.repository;

import com.monii.counterparty.model.Counterparty;
import com.monii.movement.model.*;
import com.monii.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
//    List<Movement> findByUser(User user);
//    List<Movement> findByUserAndType(User user, MovementType type);
//    List<Movement> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
//
//    // Para dashboard
//    @Query("SELECT SUM(m.amount) FROM Movement m WHERE m.user = :user AND m.type = :type")
//    BigDecimal sumAmountByUserAndType(@Param("user") User user, @Param("type") MovementType type);

    // ==============================

        // Consultas básicas para todos los movimientos de un usuario
        List<Movement> findAllByUser(User user);

        // Consultas por tipo de movimiento
        List<Movement> findByUserAndType(User user, MovementType type);

        // ID y usuario
        @Query("SELECT m FROM Movement m WHERE m.id = :id AND m.user = :user")
        Optional<Movement> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

        // Sumatorias por tipo
        @Query("SELECT COALESCE(SUM(m.amount), 0) FROM Movement m WHERE m.user = :user AND m.type = :type")
        BigDecimal sumAmountByUserAndType(@Param("user") User user, @Param("type") MovementType type);

        // Balance (ingresos - egresos)
        @Query("SELECT " +
                "(SELECT COALESCE(SUM(m.amount), 0) FROM Movement m WHERE m.user = :user AND m.type = 'INCOME') - " +
                "(SELECT COALESCE(SUM(m.amount), 0) FROM Movement m WHERE m.user = :user AND m.type = 'EXPENSE')")
        BigDecimal calculateBalance(@Param("user") User user);

        // Consultas específicas para préstamos y deudas
        @Query("SELECT m FROM LoanMovement m WHERE m.user = :user")
        List<LoanMovement> findLoansByUser(@Param("user") User user);

        @Query("SELECT m FROM DebtMovement m WHERE m.user = :user")
        List<DebtMovement> findDebtsByUser(@Param("user") User user);

        @Query("SELECT m FROM RecurrentMovement m WHERE m.user = :user AND m.isActive = true")
        List<RecurrentMovement> findActiveRecurrentMovementsByUser(@Param("user") User user);

        // Sumatoria de deudas por contraparte
        @Query("SELECT m.counterparty, SUM(m.amount) FROM LoanMovement m WHERE m.user = :user GROUP BY m.counterparty")
        List<Object[]> sumLoanAmountsByCounterparty(@Param("user") User user);

        @Query("SELECT m.counterparty, SUM(m.amount) FROM DebtMovement m WHERE m.user = :user GROUP BY m.counterparty")
        List<Object[]> sumDebtAmountsByCounterparty(@Param("user") User user);

        // Consulta para obtener todos los préstamos
        @Query("SELECT m FROM LoanMovement m")
        List<LoanMovement> findAllLoanMovements();

        // Consulta para obtener todos los préstamos pendientes o vencidos
        @Query("SELECT m FROM LoanMovement m WHERE m.isPaid = false")
        List<LoanMovement> findPendingLoanMovements();

        // Consulta para obtener todas las deudas
        @Query("SELECT m FROM DebtMovement m")
        List<DebtMovement> findAllDebtMovements();

        // Consulta para obtener todas las deudas pendientes o vencidas
        @Query("SELECT m FROM DebtMovement m WHERE m.isPaid = false")
        List<DebtMovement> findPendingDebtMovements();

        // ==============================
        // Filtros avanzados
        // ==============================

        // Encuentra préstamos de un usuario con un contacto específico
        @Query("SELECT l FROM LoanMovement l WHERE l.user = :user AND l.counterparty.id = :counterpartyId")
        List<LoanMovement> findLoansByUserAndCounterpartyId(
                @Param("user") User user,
                @Param("counterpartyId") Long counterpartyId);

        // Encuentra préstamos de un usuario con un contacto específico
        @Query("SELECT l FROM LoanMovement l WHERE l.user = :user AND l.counterparty = :counterparty")
        List<LoanMovement> findLoansByUserAndCounterparty(
                @Param("user") User user,
                @Param("counterparty") Counterparty counterparty);

        // Encuentra deudas de un usuario con un contacto específico
        @Query("SELECT d FROM DebtMovement d WHERE d.user = :user AND d.counterparty.id = :counterpartyId")
        List<DebtMovement> findDebtsByUserAndCounterpartyId(
                @Param("user") User user,
                @Param("counterpartyId") Long counterpartyId);

        // Encuentra deudas de un usuario con un contacto específico
        @Query("SELECT d FROM DebtMovement d WHERE d.user = :user AND d.counterparty = :counterparty")
        List<DebtMovement> findDebtsByUserAndCounterparty(
                @Param("user") User user,
                @Param("counterparty") Counterparty counterparty);

        // Encuentra movimientos recurrentes activos de un usuario con un contacto específico
        @Query("SELECT r FROM RecurrentMovement r WHERE r.user = :user AND r.counterparty.id = :counterpartyId AND r.isActive = true")
        List<RecurrentMovement> findActiveRecurrentMovementsByUserAndCounterpartyId(
                @Param("user") User user,
                @Param("counterpartyId") Long counterpartyId);

        // Encuentra movimientos recurrentes activos de un usuario con un contacto específico
        @Query("SELECT r FROM RecurrentMovement r WHERE r.user = :user AND r.counterparty = :counterparty AND r.isActive = true")
        List<RecurrentMovement> findActiveRecurrentMovementsByUserAndCounterparty(
                @Param("user") User user,
                @Param("counterparty") Counterparty counterparty);

        // Encuentra movimientos en un rango de fechas
        List<Movement> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

        // Encuentra préstamos por estado
        @Query("SELECT l FROM LoanMovement l WHERE l.user = :user AND l.status = :status")
        List<LoanMovement> findLoansByUserAndStatus(@Param("user") User user, @Param("status") MovementStatus status);

        // Encuentra deudas por estado
        @Query("SELECT d FROM DebtMovement d WHERE d.user = :user AND d.status = :status")
        List<DebtMovement> findDebtsByUserAndStatus(@Param("user") User user, @Param("status") MovementStatus status);

        // Encuentra contactos con deudas y sus montos totales y restantes
        @Query("SELECT d.counterparty, SUM(d.amount) as totalAmount, SUM(d.RemainingAmount) as remainingAmount " +
               "FROM DebtMovement d " +
               "WHERE d.user = :user " +
               "GROUP BY d.counterparty " +
               "ORDER BY remainingAmount DESC")
        List<Object[]> findCounterpartiesWithDebtInfo(@Param("user") User user);
}
