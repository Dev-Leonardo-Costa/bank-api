package com.leonardo.bank_api.pix.entity;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pix_keys",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pix_keys_key_value",
                        columnNames = "key_value"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PixKeyType type;

    @Column(
            name = "key_value",
            nullable = false,
            unique = true,
            length = 255
    )
    private String keyValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}