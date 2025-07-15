package com.monii.movement.service;

import com.monii.core.exception.BusinessException;
import com.monii.core.exception.ResourceNotFoundException;
import com.monii.counterparty.dto.response.CounterpartySummaryDto;
import com.monii.counterparty.model.Counterparty;
import com.monii.movement.dto.request.*;
import com.monii.movement.model.*;
import com.monii.counterparty.repository.CounterpartyRepository;
import com.monii.movement.repository.MovementRepository;
import com.monii.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class MovementService {

    private static final Logger logger = LoggerFactory.getLogger(MovementService.class);

    private final MovementRepository movementRepository;
    private final CounterpartyRepository counterpartyRepository;

    public MovementService(MovementRepository movementRepository,
                           CounterpartyRepository counterpartyRepository) {
        this.movementRepository = movementRepository;
        this.counterpartyRepository = counterpartyRepository;
    }

    public Movement createMovement(MovementRequest request, User user) {
        try {
            Movement movement = switch(request.getType()) {
                case INCOME, EXPENSE -> createBasicMovement((BasicMovementRequest) request, user);
                case LOAN, DEBT     -> createLoanOrDebt((LoanDebtMovementRequest) request, user);
                case RECURRENT      -> createRecurrent((RecurrentMovementRequest) request, user);
                default -> throw new IllegalArgumentException("Tipo no soportado: " + request.getType());
            };

            // Asegura la persistencia
            return movementRepository.save(movement);

        } catch (DataAccessException e) {
            throw new BusinessException("Error al guardar el movimiento", 500, e.getMessage());
        }
    }

    private Movement createBasicMovement(BasicMovementRequest request, User user) {
        Movement movement = request.getType() == MovementType.INCOME
                ? new IncomeMovement()
                : new ExpenseMovement();

        movement.setUser(user);
        movement.setAmount(request.getAmount());
        movement.setDescription(request.getDescription());
        movement.setType(request.getType());
        return movement;
    }

    private Movement createLoanOrDebt(LoanDebtMovementRequest request, User user) {
        // Crear instancia específica
        Movement movement = (request.getType() == MovementType.LOAN)
                ? new LoanMovement()
                : new DebtMovement();

        // Propiedades comunes
        movement.setUser(user);
        movement.setAmount(request.getAmount());
        movement.setDescription(request.getDescription());
        movement.setType(request.getType());

        // Propiedades específicas de Loan/Debt
        if (movement instanceof LoanMovement loanMovement) {
            loanMovement.setCounterparty(getCounterpartyOrThrow(request.getCounterpartyId()));
            loanMovement.setDueDate(request.getDueDate());
            loanMovement.setIsPaid(false);
            loanMovement.setRemainingAmount(((LoanDebtMovementRequest) request).getAmount());
            loanMovement.setStatus(calculateMovementStatus(loanMovement.getDueDate(), loanMovement.getRemainingAmount())); // Establecer estado inicial
        }
        else {
            DebtMovement debtMovement = (DebtMovement) movement;
            debtMovement.setCounterparty(getCounterpartyOrThrow(request.getCounterpartyId()));
            debtMovement.setDueDate(request.getDueDate());
            debtMovement.setIsPaid(false);
            debtMovement.setRemainingAmount(((LoanDebtMovementRequest) request).getAmount());
            debtMovement.setStatus(calculateMovementStatus(debtMovement.getDueDate(), debtMovement.getRemainingAmount())); // Establecer estado inicial
        }
        return movement;
    }

    private Movement createRecurrent(RecurrentMovementRequest request, User user) {
        RecurrentMovement movement = new RecurrentMovement();

        // Propiedades comunes de Movement
        movement.setUser(user);
        movement.setAmount(request.getAmount());
        movement.setDescription(request.getDescription());
        movement.setType(MovementType.RECURRENT);

        // Propiedades específicas de RecurrentMovement
        movement.setFrequency(request.getFrequency());
        movement.setStartDate(request.getStartDate());
        movement.setEndDate(request.getEndDate());
        movement.setIsActive(true);

        // Counterparty opcional (para préstamos/deudas recurrentes)
        if (request.getCounterpartyId() != null) {
            movement.setCounterparty(
                    counterpartyRepository.findById(request.getCounterpartyId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Counterparty", "id", request.getCounterpartyId()
                            ))
            );
        }

        movement.validate();
        return movement;
    }

    public void deleteMovement(Long id, User user) {
        Movement movement = getMovementById(id, user);
        movementRepository.delete(movement);
    }

    // Filtros
    private Counterparty getCounterpartyOrThrow(Long counterpartyId) {
        return counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contraparte no encontrada"));
    }

    public Movement getMovementById(Long id, User user) {
        return movementRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Movement", "id", id));
    }

    public List<Movement> getAllMovements(User user) {
        return movementRepository.findAllByUser(user);
    }

    public List<Movement> getMovementsByType(User user, MovementType type) {
        return movementRepository.findByUserAndType(user, type);
    }

    public BigDecimal getTotalByMovementType(User user, MovementType type) {
        return movementRepository.sumAmountByUserAndType(user, type);
    }

    public BigDecimal getBalance(User user) {
        return movementRepository.calculateBalance(user);
    }

    public List<LoanMovement> getUserLoans(User user) {
        return movementRepository.findLoansByUser(user);
    }

    public List<DebtMovement> getUserDebts(User user) {
        return movementRepository.findDebtsByUser(user);
    }

    public BigDecimal getTotalLoans(User user) {
        return getTotalByMovementType(user, MovementType.LOAN);
    }

    public BigDecimal getTotalDebts(User user) {
        return getTotalByMovementType(user, MovementType.DEBT);
    }

    public List<CounterpartySummaryDto> getLoanSummaryByCounterparty(User user) {
        List<Object[]> results = movementRepository.sumLoanAmountsByCounterparty(user);
        return mapCounterpartyAmounts(results);
    }

    public List<CounterpartySummaryDto> getDebtSummaryByCounterparty(User user) {
        List<Object[]> results = movementRepository.sumDebtAmountsByCounterparty(user);
        return mapCounterpartyAmounts(results);
    }

    public List<RecurrentMovement> getActiveRecurrentMovements(User user) {
        return movementRepository.findActiveRecurrentMovementsByUser(user);
    }

    public void deactivateRecurrentMovement(Long id, User user) {
        RecurrentMovement movement = (RecurrentMovement) getMovementById(id, user);
        movement.setIsActive(false);
        movementRepository.save(movement);
    }

    // Convertir resultados de consultas SQL en DTOs
    private List<CounterpartySummaryDto> mapCounterpartyAmounts(List<Object[]> results) {
        List<CounterpartySummaryDto> summaryList = new ArrayList<>();

        for (Object[] result : results) {
            Counterparty counterparty = (Counterparty) result[0];
            BigDecimal amount = (BigDecimal) result[1];

            CounterpartySummaryDto dto = new CounterpartySummaryDto();
            dto.setCounterpartyId(counterparty.getId());
            dto.setCounterpartyName(counterparty.getName());
            dto.setCounterpartyType(counterparty.getType());
            dto.setTotalAmount(amount);

            summaryList.add(dto);
        }

        return summaryList;
    }

    /**
     * Actualiza el estado de un movimiento según las reglas de negocio:
     * - Si saldo restante = 0 → Pagado
     * - Si fecha de vencimiento < hoy y saldo restante > 0 → Vencido
     * - Si fecha de vencimiento >= hoy y saldo restante > 0 → Pendiente
     */
    public MovementStatus calculateMovementStatus(LocalDateTime dueDate, BigDecimal remainingAmount) {
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            return MovementStatus.PAID;
        } else if (dueDate.isBefore(LocalDateTime.now()) && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            return MovementStatus.EXPIRED;
        } else {
            return MovementStatus.PENDING;
        }
    }

    // Actualizar el estado
    public void updateMovementStatus(LoanMovement movement) {
        movement.setStatus(calculateMovementStatus(movement.getDueDate(), movement.getRemainingAmount()));
    }

    public void updateMovementStatus(DebtMovement movement) {
        movement.setStatus(calculateMovementStatus(movement.getDueDate(), movement.getRemainingAmount()));
    }

    // Actualizar el estado de todos los movimientos
    @Scheduled(cron = "0 57 14 * * *") // Ejecutar a medianoche todos los días
    public void updateAllMovementStatuses() {

        logger.info("Actualizando el estado de todos los movimientos...");
        // Bbuscar movimientos que no estén pagos
        List<LoanMovement> loans = movementRepository.findPendingLoanMovements();
        List<DebtMovement> debts = movementRepository.findPendingDebtMovements();
        logger.info("Encontrados {} préstamos y {} deudas", loans.size(), debts.size());

        // Si no hay movimientos pendientes, terminar
        if (loans.isEmpty() && debts.isEmpty()) {
            logger.info("No hay movimientos pendientes para actualizar");
            return;
        }

        // Procesar préstamos
        if (!loans.isEmpty()) {
            int loansUpdated = 0;
            for (LoanMovement loan : loans) {
                // Guardar el estado anterior para comparar
                MovementStatus oldStatus = loan.getStatus();

                // Actualizar el estado
                updateMovementStatus(loan);

                // Contar cantidad de movimientos actualizados
                if (oldStatus != loan.getStatus()) {
                    loansUpdated++;
                }
            }
            // Guardar los cambios en lotes
            int batchSize = 100;
            for (int i = 0; i < loans.size(); i += batchSize) {
                List<LoanMovement> batch = loans.subList(i, Math.min(i + batchSize, loans.size()));
                movementRepository.saveAll(batch);
            }

            logger.info("Actualizados {} de {} préstamos", loansUpdated, loans.size());
        }

        // Procesar deudas
        if (!debts.isEmpty()) {
            int debtsUpdated = 0;
            for (DebtMovement debt : debts) {
                // Guardar el estado anterior para comparar
                MovementStatus oldStatus = debt.getStatus();

                // Actualizar el estado
                updateMovementStatus(debt);

                // Contar cantidad de movimientos actualizados
                if (oldStatus != debt.getStatus()) {
                    debtsUpdated++;
                }
            }

            // Guardar los cambios en lotes
            int batchSize = 100;
            for (int i = 0; i < debts.size(); i += batchSize) {
                List<DebtMovement> batch = debts.subList(i, Math.min(i + batchSize, debts.size()));
                movementRepository.saveAll(batch);
            }
            logger.info("Actualizadas {} de {} deudas", debtsUpdated, debts.size());
        }
        logger.info("Finalizada actualización de estados de movimientos");
    }

    // Obtiene los movimientos asociados a un contacto específico
    public List<Movement> getMovementsByCounterparty(User user, Long counterpartyId) {
        // Verificar que el contacto pertenece al usuario
        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
                .filter(c -> c.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));

        // Obtener movimientos por tipo que tengan este contacto
        List<Movement> result = new ArrayList<>();

        // Obtener préstamos
        List<LoanMovement> loans = movementRepository.findLoansByUserAndCounterparty(user, counterparty);
        result.addAll(loans);

        // Obtener deudas
        List<DebtMovement> debts = movementRepository.findDebtsByUserAndCounterparty(user, counterparty);
        result.addAll(debts);

        // Obtener pagos recurrentes
        List<RecurrentMovement> recurrents = movementRepository.findActiveRecurrentMovementsByUserAndCounterparty(user, counterparty);
        result.addAll(recurrents);

        // Ordenar por fecha de creación (más reciente primero)
        result.sort(Comparator.comparing(Movement::getCreatedAt).reversed());

        return result;
    }

    // Obtiene los movimientos en un rango de fechas
    public List<Movement> getMovementsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        // Si startDate es null, usar una fecha muy antigua
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.of(1900, 1, 1, 0, 0);

        // Si endDate es null, usar fecha actual
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        return movementRepository.findByUserAndCreatedAtBetween(user, effectiveStartDate, effectiveEndDate);
    }

    // Obtiene los movimientos por estado
    public List<Movement> getMovementsByStatus(User user, MovementStatus status) {
        List<Movement> result = new ArrayList<>();

        // Solo préstamos y deudas tienen estado
        if (status != null) {
            List<LoanMovement> loans = movementRepository.findLoansByUserAndStatus(user, status);
            List<DebtMovement> debts = movementRepository.findDebtsByUserAndStatus(user, status);

            result.addAll(loans);
            result.addAll(debts);
        }

        return result;
    }

    // Obtiene los movimientos por categoría
    public List<Movement> getMovementsByCategory(User user, Long categoryId) {
        // Implementar cuando se agregue la funcionalidad de categorías
        return new ArrayList<>(); // Por ahora devolver lista vacía
    }

    // Actualiza un movimiento existente
    @Transactional
    public Movement updateMovement(Long movementId, MovementUpdateRequest request, User user) {
        // Verificar que el movimiento existe y pertenece al usuario
        Movement movement = movementRepository.findByIdAndUser(movementId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        // Actualizar campos comunes
        if (request.getAmount() != null) {
            movement.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            movement.setDescription(request.getDescription());
        }

        // Actualizar campos específicos según el tipo de movimiento
        switch (movement.getType()) {
            case LOAN:
                updateLoanMovement((LoanMovement) movement, request);
                break;
            case DEBT:
                updateDebtMovement((DebtMovement) movement, request);
                break;
            case RECURRENT:
                updateRecurrentMovement((RecurrentMovement) movement, request);
                break;
            case INCOME:
            case EXPENSE:
                // No hay campos adicionales para actualizar
                break;
        }

        // Guardar y devolver el movimiento actualizado
        return movementRepository.save(movement);
    }

    private void updateLoanMovement(LoanMovement movement, MovementUpdateRequest request) {
        if (request instanceof LoanDebtMovementUpdateRequest) {
            LoanDebtMovementUpdateRequest loanRequest = (LoanDebtMovementUpdateRequest) request;

            if (loanRequest.getDueDate() != null) {
                movement.setDueDate(loanRequest.getDueDate());
            }
            if (loanRequest.getStatus() != null) {
                movement.setStatus(loanRequest.getStatus());
            }
            if (loanRequest.getRemainingAmount() != null) {
                movement.setRemainingAmount(loanRequest.getRemainingAmount());
            }
            if (loanRequest.getIsPaid() != null) {
                movement.setIsPaid(loanRequest.getIsPaid());
            }
            if (loanRequest.getCounterpartyId() != null) {
                Counterparty counterparty = counterpartyRepository.findById(loanRequest.getCounterpartyId())
                        .filter(c -> c.getOwner().getId().equals(movement.getUser().getId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));
                movement.setCounterparty(counterparty);
            }
        }
    }

    private void updateDebtMovement(DebtMovement movement, MovementUpdateRequest request) {
        if (request instanceof LoanDebtMovementUpdateRequest) {
            LoanDebtMovementUpdateRequest debtRequest = (LoanDebtMovementUpdateRequest) request;

            if (debtRequest.getDueDate() != null) {
                movement.setDueDate(debtRequest.getDueDate());
            }
            if (debtRequest.getStatus() != null) {
                movement.setStatus(debtRequest.getStatus());
            }
            if (debtRequest.getRemainingAmount() != null) {
                movement.setRemainingAmount(debtRequest.getRemainingAmount());
            }
            if (debtRequest.getIsPaid() != null) {
                movement.setIsPaid(debtRequest.getIsPaid());
            }
            if (debtRequest.getCounterpartyId() != null) {
                Counterparty counterparty = counterpartyRepository.findById(debtRequest.getCounterpartyId())
                        .filter(c -> c.getOwner().getId().equals(movement.getUser().getId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));
                movement.setCounterparty(counterparty);
            }
        }
    }

    private void updateRecurrentMovement(RecurrentMovement movement, MovementUpdateRequest request) {
        if (request instanceof RecurrentMovementUpdateRequest) {
            RecurrentMovementUpdateRequest recurrentRequest = (RecurrentMovementUpdateRequest) request;

            if (recurrentRequest.getFrequency() != null) {
                movement.setFrequency(recurrentRequest.getFrequency());
            }
            if (recurrentRequest.getStartDate() != null) {
                movement.setStartDate(recurrentRequest.getStartDate());
            }
            if (recurrentRequest.getEndDate() != null) {
                movement.setEndDate(recurrentRequest.getEndDate());
            }
            if (recurrentRequest.getIsActive() != null) {
                movement.setIsActive(recurrentRequest.getIsActive());
            }
            if (recurrentRequest.getCounterpartyId() != null) {
                Counterparty counterparty = counterpartyRepository.findById(recurrentRequest.getCounterpartyId())
                        .filter(c -> c.getOwner().getId().equals(movement.getUser().getId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));
                movement.setCounterparty(counterparty);
            }
        }
    }
}
