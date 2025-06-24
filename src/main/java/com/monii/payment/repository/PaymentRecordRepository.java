package com.monii.payment.repository;

import com.monii.payment.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByMovementIdOrderByPaymentDateDesc(Long movementId);
}