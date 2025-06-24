package com.monii.counterparty.controller;

import com.monii.core.dto.ApiResponse;
import com.monii.counterparty.dto.request.CounterpartyRequest;
import com.monii.counterparty.dto.response.CounterpartyResponse;
import com.monii.counterparty.dto.response.CounterpartySummaryDto;
import com.monii.counterparty.model.Counterparty;
import com.monii.counterparty.model.CounterpartyType;
import com.monii.movement.dto.response.DebtMovementResponse;
import com.monii.movement.dto.response.LoanMovementResponse;
import com.monii.movement.dto.response.MovementResponse;
import com.monii.movement.dto.response.RecurrentMovementResponse;
import com.monii.movement.model.DebtMovement;
import com.monii.movement.model.LoanMovement;
import com.monii.movement.model.Movement;
import com.monii.movement.model.RecurrentMovement;
import com.monii.counterparty.service.CounterpartyService;
import com.monii.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/counterparties")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyService counterpartyService;

    // Crear un nuevo contacto
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CounterpartyResponse>> createCounterparty(
            @Valid @RequestBody CounterpartyRequest request,
            @AuthenticationPrincipal User user) {

        if (request.getType() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "El tipo de contacto debe ser PERSON, COMPANY o PLATFORM"));
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "El nombre del contacto no puede estar vacío"));
        }

        Counterparty counterparty = new Counterparty();
        counterparty.setName(request.getName());
        counterparty.setType(request.getType());
        counterparty.setTaxId(request.getTaxId());
        counterparty.setEmail(request.getEmail());
        counterparty.setPhone(request.getPhone());

        Counterparty createdCounterparty = counterpartyService.createCounterparty(counterparty, user);
        CounterpartyResponse response = new CounterpartyResponse(createdCounterparty);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Contacto creado exitosamente", response));
    }

    // Obtener todos los contactos o filtrar por tipo
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CounterpartyResponse>>> getAllCounterparties(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) CounterpartyType type) {

        List<Counterparty> counterparties;
        if (type != null) {
            counterparties = counterpartyService.getCounterpartiesByType(user, type);
        } else {
            counterparties = counterpartyService.getAllCounterparties(user);
        }

        List<CounterpartyResponse> responseList = counterparties.stream()
                .map(CounterpartyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Contactos obtenidos exitosamente", responseList));
    }

    // Obtener un contacto específico por ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CounterpartyResponse>> getCounterpartyById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Counterparty counterparty = counterpartyService.getCounterpartyById(id, user);
        CounterpartyResponse response = new CounterpartyResponse(counterparty);

        return ResponseEntity.ok(ApiResponse.ok("Contacto obtenido exitosamente", response));
    }

    // Obtener resumen de contactos con montos pendientes
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CounterpartySummaryDto>>> getCounterpartySummary(
            @AuthenticationPrincipal User user) {

        List<CounterpartySummaryDto> summaryList = counterpartyService.getCounterpartySummary(user);

        return ResponseEntity.ok(ApiResponse.ok("Resumen de contactos obtenido exitosamente", summaryList));
    }

    // Obtener movimientos asociados a un contacto
    @GetMapping("/{id}/movements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Object>>> getCounterpartyMovements(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        List<Movement> movements = counterpartyService.getCounterpartyMovements(id, user);

        // Convertir cada movimiento a su DTO específico
        List<Object> movementResponses = movements.stream()
                .map(this::createMovementResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Movimientos del contacto obtenidos exitosamente", movementResponses));
    }

    // Buscar contactos por nombre (para autocompletado)
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CounterpartyResponse>>> searchCounterparties(
            @RequestParam String query,
            @AuthenticationPrincipal User user) {

        List<Counterparty> counterparties = counterpartyService.searchCounterpartiesByName(user, query);

        List<CounterpartyResponse> responseList = counterparties.stream()
                .map(CounterpartyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Búsqueda de contactos exitosa", responseList));
    }

    // Actualizar un contacto existente
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CounterpartyResponse>> updateCounterparty(
            @PathVariable Long id,
            @Valid @RequestBody CounterpartyRequest request,
            @AuthenticationPrincipal User user) {

        Counterparty counterparty = new Counterparty();
        counterparty.setName(request.getName());
        counterparty.setType(request.getType());
        counterparty.setTaxId(request.getTaxId());
        counterparty.setEmail(request.getEmail());
        counterparty.setPhone(request.getPhone());

        Counterparty updatedCounterparty = counterpartyService.updateCounterparty(id, counterparty, user);
        CounterpartyResponse response = new CounterpartyResponse(updatedCounterparty);

        return ResponseEntity.ok(ApiResponse.ok("Contacto actualizado exitosamente", response));
    }

    // Eliminar un contacto
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCounterparty(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        counterpartyService.deleteCounterparty(id, user);

        return ResponseEntity.ok(ApiResponse.ok("Contacto eliminado exitosamente"));
    }

    // Método auxiliar para convertir un Movement a su DTO específico
    private Object createMovementResponse(Movement movement) {
        return switch(movement.getType()) {
            case LOAN -> new LoanMovementResponse((LoanMovement) movement);
            case DEBT -> new DebtMovementResponse((DebtMovement) movement);
            case RECURRENT -> new RecurrentMovementResponse((RecurrentMovement) movement);
            default -> new MovementResponse(movement);
        };
    }
}
