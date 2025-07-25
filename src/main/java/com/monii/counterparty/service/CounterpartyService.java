package com.monii.counterparty.service;

import com.monii.core.exception.BusinessException;
import com.monii.core.exception.ResourceNotFoundException;
import com.monii.counterparty.model.Counterparty;
import com.monii.counterparty.model.CounterpartyType;
import com.monii.counterparty.dto.response.CounterpartySummaryDto;
import com.monii.counterparty.dto.response.CounterpartyDebtDto;
import com.monii.movement.model.DebtMovement;
import com.monii.movement.model.LoanMovement;
import com.monii.movement.model.Movement;
import com.monii.movement.model.RecurrentMovement;
import com.monii.counterparty.repository.CounterpartyRepository;
import com.monii.movement.repository.MovementRepository;
import com.monii.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;
    private final MovementRepository movementRepository;

    public CounterpartyService(CounterpartyRepository counterpartyRepository,
                               MovementRepository movementRepository) {
        this.counterpartyRepository = counterpartyRepository;
        this.movementRepository = movementRepository;
    }

    // Crea un nuevo contacto (counterparty)
    public Counterparty createCounterparty(Counterparty counterparty, User owner) {
        // Validar que el nombre no esté vacío
        if (counterparty.getName() == null || counterparty.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre del contacto no puede estar vacío", 400);
        }

        // Asignar el propietario
        counterparty.setOwner(owner);

        return counterpartyRepository.save(counterparty);
    }

    // Obtiene todos los contactos de un usuario
    public List<Counterparty> getAllCounterparties(User owner) {
        return counterpartyRepository.findByOwnerId(owner.getId());
    }

    // Obtiene contactos por tipo (PERSON, COMPANY, PLATFORM)
    public List<Counterparty> getCounterpartiesByType(User owner, CounterpartyType type) {
        return counterpartyRepository.findByOwnerIdAndType(owner.getId(), type);
    }

    // Obtiene un contacto específico por ID
    public Counterparty getCounterpartyById(Long id, User owner) {
        return counterpartyRepository.findById(id)
                .filter(counterparty -> counterparty.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));
    }

    // Actualiza un contacto existente
    public Counterparty updateCounterparty(Long id, Counterparty updatedCounterparty, User owner) {
        Counterparty existingCounterparty = getCounterpartyById(id, owner);

        // Actualizar propiedades
        existingCounterparty.setName(updatedCounterparty.getName());
        existingCounterparty.setType(updatedCounterparty.getType());
        existingCounterparty.setTaxId(updatedCounterparty.getTaxId());
        existingCounterparty.setEmail(updatedCounterparty.getEmail());
        existingCounterparty.setPhone(updatedCounterparty.getPhone());

        return counterpartyRepository.save(existingCounterparty);
    }

    // Elimina un contacto
    public void deleteCounterparty(Long id, User owner) {
        Counterparty counterparty = getCounterpartyById(id, owner);

        // Verificar si tiene movimientos asociados
        List<Movement> movements = getCounterpartyMovements(id, owner);
        if (!movements.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar el contacto porque tiene movimientos asociados",
                    400
            );
        }

        counterpartyRepository.delete(counterparty);
    }

    // Obtiene un resumen de los contactos con montos pendientes
    public List<CounterpartySummaryDto> getCounterpartySummary(User owner) {
        List<Counterparty> counterparties = getAllCounterparties(owner);

        return counterparties.stream().map(counterparty -> {
            CounterpartySummaryDto dto = new CounterpartySummaryDto(counterparty);

            // Calcular montos pendientes
            BigDecimal pendingLoans = calculatePendingLoans(counterparty, owner);
            BigDecimal pendingDebts = calculatePendingDebts(counterparty, owner);

            // Establecer montos pendientes
            dto.setPendingLoansAmount(pendingLoans);
            dto.setPendingDebtsAmount(pendingDebts);

            // Establecer el monto total como la diferencia entre los montos pendientes
            dto.setTotalAmount(pendingLoans.subtract(pendingDebts));

            return dto;
        }).collect(Collectors.toList());
    }


    // Calcula el monto pendiente de préstamos para un contacto
    private BigDecimal calculatePendingLoans(Counterparty counterparty, User owner) {
        List<LoanMovement> loans = movementRepository.findLoansByUserAndCounterparty(owner, counterparty);

        return loans.stream()
                .map(loan -> loan.getRemainingAmount() != null ? loan.getRemainingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calcula el monto pendiente de deudas para un contacto
    private BigDecimal calculatePendingDebts(Counterparty counterparty, User owner) {
        List<DebtMovement> debts = movementRepository.findDebtsByUserAndCounterparty(owner, counterparty);

        return debts.stream()
                .map(debt -> debt.getRemainingAmount() != null ? debt.getRemainingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // Obtiene los movimientos asociados a un contacto
    public List<Movement> getCounterpartyMovements(Long counterpartyId, User owner) {
        // Verificar que el contacto pertenece al usuario
        Counterparty counterparty = getCounterpartyById(counterpartyId, owner);

        // Obtener movimientos por tipo que tengan este contacto
        List<Movement> result = new java.util.ArrayList<>();

        // Obtener préstamos
        List<LoanMovement> loans = movementRepository.findLoansByUserAndCounterpartyId(owner, counterpartyId);
        result.addAll(loans);

        // Obtener deudas
        List<DebtMovement> debts = movementRepository.findDebtsByUserAndCounterpartyId(owner, counterpartyId);
        result.addAll(debts);

        // Obtener pagos recurrentes
        List<RecurrentMovement> recurrents = movementRepository.findActiveRecurrentMovementsByUserAndCounterpartyId(owner, counterpartyId);
        result.addAll(recurrents);

        // Ordenar por fecha de creación (más reciente primero)
        result.sort(java.util.Comparator.comparing(Movement::getCreatedAt).reversed());

        return result;
    }

    // Busca contactos por nombre (para autocompletado)
    public List<Counterparty> searchCounterpartiesByName(User owner, String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        return counterpartyRepository.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), query);
    }

    // Obtiene contactos con deudas y sus montos
    public List<CounterpartyDebtDto> getCounterpartiesWithDebtInfo(User owner) {
        List<Object[]> results = movementRepository.findCounterpartiesWithDebtInfo(owner);
        return results.stream()
            .map(result -> {
                Counterparty counterparty = (Counterparty) result[0];
                BigDecimal totalAmount = (BigDecimal) result[1];
                BigDecimal remainingAmount = (BigDecimal) result[2];

                CounterpartyDebtDto dto = new CounterpartyDebtDto(counterparty);
                dto.setTotalDebtAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
                dto.setRemainingDebtAmount(remainingAmount != null ? remainingAmount : BigDecimal.ZERO);

                return dto;
            })
            .collect(Collectors.toList());
    }
}
