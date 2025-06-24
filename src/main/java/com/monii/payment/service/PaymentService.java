package com.monii.payment.service;

import com.monii.core.exception.BusinessException;
import com.monii.payment.dto.PaymentRequest;
import com.monii.payment.dto.PaymentResponse;
import com.monii.core.exception.ResourceNotFoundException;
import com.monii.movement.model.DebtMovement;
import com.monii.movement.model.LoanMovement;
import com.monii.movement.model.Movement;
import com.monii.movement.model.MovementStatus;
import com.monii.movement.repository.MovementRepository;
import com.monii.payment.model.PaymentRecord;
import com.monii.payment.repository.PaymentRecordRepository;
import com.monii.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final MovementRepository movementRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    // Registrar un pago parcial para un préstamo o deuda
    @Transactional
    public PaymentResponse registerPayment(PaymentRequest request, User user) {
        // Buscar el movimiento
        Movement movement = movementRepository.findById(request.getMovementId())
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        // Verificar que el movimiento pertenece al usuario
        if (!movement.getUser().getId().equals(user.getId())) {
            throw new BusinessException("No tienes permiso para modificar este movimiento", HttpStatus.FORBIDDEN.value());
        }

        // Verificar que el movimiento es un préstamo o una deuda
        if (!(movement instanceof LoanMovement) && !(movement instanceof DebtMovement)) {
            throw new BusinessException("Solo se pueden registrar pagos para préstamos o deudas", HttpStatus.BAD_REQUEST.value());
        }

        // Obtener el monto restante actual
        BigDecimal remainingAmount;
        if (movement instanceof LoanMovement) {
            remainingAmount = ((LoanMovement) movement).getRemainingAmount();
        } else {
            remainingAmount = ((DebtMovement) movement).getRemainingAmount();
        }

        // Verificar que el pago no excede el monto restante
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException("El monto del pago excede el saldo pendiente", HttpStatus.BAD_REQUEST.value());
        }

        // Crear y guardar el registro de pago
        PaymentRecord payment = new PaymentRecord();
        payment.setMovement(movement);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());

        payment = paymentRecordRepository.save(payment);

        // Actualizar el monto restante
        BigDecimal newRemainingAmount = remainingAmount.subtract(request.getAmount());

        // Actualizar el estado del movimiento
        if (movement instanceof LoanMovement) {
            LoanMovement loan = (LoanMovement) movement;
            loan.setRemainingAmount(newRemainingAmount);

            // Si el monto restante es cero, marcar como pagado
            if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
                loan.setIsPaid(true);
                loan.setStatus(MovementStatus.PAID);
            } else {
                // Actualizar el estado según la fecha de vencimiento
                if (loan.getDueDate().isBefore(java.time.LocalDateTime.now())) {
                    loan.setStatus(MovementStatus.EXPIRED);
                } else {
                    loan.setStatus(MovementStatus.PENDING);
                }
            }

            movementRepository.save(loan);
        } else {
            DebtMovement debt = (DebtMovement) movement;
            debt.setRemainingAmount(newRemainingAmount);

            // Si el monto restante es cero, marcar como pagado
            if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
                debt.setIsPaid(true);
                debt.setStatus(MovementStatus.PAID);
            } else {
                // Actualizar el estado según la fecha de vencimiento
                if (debt.getDueDate().isBefore(java.time.LocalDateTime.now())) {
                    debt.setStatus(MovementStatus.EXPIRED);
                } else {
                    debt.setStatus(MovementStatus.PENDING);
                }
            }

            movementRepository.save(debt);
        }

        return new PaymentResponse(payment, newRemainingAmount);
    }

    // Obtener el historial de pagos para un movimiento
    public List<PaymentResponse> getPaymentHistory(Long movementId, User user) {
        // Verificar que el movimiento existe y pertenece al usuario
        Movement movement = movementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        if (!movement.getUser().getId().equals(user.getId())) {
            throw new BusinessException("No tienes permiso para ver este movimiento", HttpStatus.FORBIDDEN.value());
        }

        // Obtener el historial de pagos
        List<PaymentRecord> payments = paymentRecordRepository.findByMovementIdOrderByPaymentDateDesc(movementId);

        // Obtener el monto restante actual
        BigDecimal currentRemainingAmount;
        if (movement instanceof LoanMovement) {
            currentRemainingAmount = ((LoanMovement) movement).getRemainingAmount();
        } else if (movement instanceof DebtMovement) {
            currentRemainingAmount = ((DebtMovement) movement).getRemainingAmount();
        } else {
            throw new BusinessException("Solo se pueden ver pagos para préstamos o deudas", HttpStatus.BAD_REQUEST.value());
        }

        // Calcular el monto restante después de cada pago (en orden inverso)
        BigDecimal runningTotal = currentRemainingAmount;
        for (PaymentRecord paymentRecord : payments) {
            runningTotal = runningTotal.add(paymentRecord.getAmount());
        }

        // Convertir a DTOs
        BigDecimal finalRunningTotal = runningTotal;
        return payments.stream()
                .map(payment -> {
                    final BigDecimal remainingAmount = finalRunningTotal.subtract(payment.getAmount());
                    return new PaymentResponse(payment, remainingAmount);
                })
                .toList();
    }
}
