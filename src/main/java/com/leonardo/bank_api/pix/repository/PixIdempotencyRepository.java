package com.leonardo.bank_api.pix.repository;

import com.leonardo.bank_api.pix.entity.PixIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PixIdempotencyRepository extends JpaRepository<PixIdempotency, Long> {

    Optional<PixIdempotency> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query(
            value = """
                INSERT INTO pix_idempotency (
                    idempotency_key,
                    request_hash,
                    created_at
                )
                VALUES (
                    :idempotencyKey,
                    :requestHash,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (idempotency_key)
                DO NOTHING
                """,
            nativeQuery = true
    )
    int reserve(
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash
    );

}