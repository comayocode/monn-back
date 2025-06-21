package com.monii.controller;

import com.monii.dto.ApiResponse;
import com.monii.dto.PaymentRequest;
import com.monii.dto.PaymentResponse;
import com.monii.model.User;
import com.monii.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // Registrar un pago
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> registerPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {

        PaymentResponse payment = paymentService.registerPayment(request, user);

        ApiResponse<PaymentResponse> response = ApiResponse.ok(
                "Pago registrado exitosamente",
                payment
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Obtener el historial de pagos de un movimiento
    @GetMapping("/movement/{movementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @PathVariable Long movementId,
            @AuthenticationPrincipal User user) {

        List<PaymentResponse> payments = paymentService.getPaymentHistory(movementId, user);

        ApiResponse<List<PaymentResponse>> response = ApiResponse.ok(
                "Historial de pagos obtenido exitosamente",
                payments
        );

        return ResponseEntity.ok(response);
    }
}
