package com.leonardo.bank_api.transaction.entity;

import com.leonardo.bank_api.shared.enums.TransactionAuditAction;
import com.leonardo.bank_api.shared.enums.TransactionAuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionAuditStatus status;

    @Column(name = "performed_by", nullable = false, length = 150)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}