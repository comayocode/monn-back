package com.monii.controller;

import com.monii.dto.*;
import com.monii.model.*;
import com.monii.service.MovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {
    private final MovementService movementService;
    // Creación de movimientos
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createMovement(
            @Valid @RequestBody MovementRequest request,
            @AuthenticationPrincipal User user) {

        Movement createdMovement = movementService.createMovement(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponseDto(createdMovement));
    }

    // Endpoint específico para préstamos (opcional)
    @PostMapping("/loans")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoanMovementResponse> createLoan(
            @Valid @RequestBody LoanDebtMovementRequest request,
            @AuthenticationPrincipal User user) {

        Movement movement = movementService.createMovement(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoanMovementResponse((LoanMovement) movement));
    }

    // Endpoint específico para recurrentes (opcional)
    @PostMapping("/recurrent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecurrentMovementResponse> createRecurrent(
            @Valid @RequestBody RecurrentMovementRequest request,
            @AuthenticationPrincipal User user) {

        RecurrentMovement movement = (RecurrentMovement) movementService.createMovement(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RecurrentMovementResponse(movement));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<?>>> getAllMovements(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) MovementStatus status,
            @RequestParam(required = false) Long categoryId) {

        // Obtener movimientos con filtros
        List<Movement> movements;

        if (counterpartyId != null) {
            // Si se especifica counterpartyId, filtrar por contacto
            movements = movementService.getMovementsByCounterparty(user, counterpartyId);
        } else if (type != null) {
            // Si se especifica tipo, filtrar por tipo
            movements = movementService.getMovementsByType(user, type);
        } else if (startDate != null || endDate != null) {
            // Si se especifican fechas, filtrar por rango de fechas
            movements = movementService.getMovementsByDateRange(user, startDate, endDate);
        } else if (status != null) {
            // Si se especifica estado, filtrar por estado
            movements = movementService.getMovementsByStatus(user, status);
        } else if (categoryId != null) {
            // Si se especifica categoría, filtrar por categoría
            movements = movementService.getMovementsByCategory(user, categoryId);
        } else {
            // Si no hay filtros, obtener todos los movimientos
            movements = movementService.getAllMovements(user);
        }

        // Convertir cada movimiento a su DTO específico
        List<?> movementResponses = movements.stream()
                .map(this::createResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.ok("Movimientos obtenidos exitosamente", movementResponses)
        );
    }

    private Object createResponseDto(Movement movement) {
        return switch(movement.getType()) {
            case LOAN -> new LoanMovementResponse((LoanMovement) movement);
            case DEBT -> new DebtMovementResponse((DebtMovement) movement);
            case RECURRENT -> new RecurrentMovementResponse((RecurrentMovement) movement);
            default -> new MovementResponse(movement);
        };
    }
}