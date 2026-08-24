package com.leonardo.bank_api.pix.repository;

import com.leonardo.bank_api.pix.entity.PixSchedule;
import com.leonardo.bank_api.shared.enums.PixScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PixScheduleRepository extends JpaRepository<PixSchedule, Long> {

    List<PixSchedule> findAllByStatusAndScheduledAtLessThanEqual(PixScheduleStatus status, LocalDateTime scheduledAt);

    List<PixSchedule> findAllBySourceAccountCustomerEmail(String email);

}