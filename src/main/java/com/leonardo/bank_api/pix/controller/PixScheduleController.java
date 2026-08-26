package com.leonardo.bank_api.pix.controller;

import com.leonardo.bank_api.pix.controller.pixcontrollerdocs.PixScheduleControllerDocs;
import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;
import com.leonardo.bank_api.pix.service.PixScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pix/schedules")
@RequiredArgsConstructor
public class PixScheduleController implements PixScheduleControllerDocs {

    private final PixScheduleService pixScheduleService;

    @PostMapping
    public ResponseEntity<PixScheduleResponse> createSchedule(@Valid @RequestBody CreatePixScheduleRequest request) {

        PixScheduleResponse response = pixScheduleService.createSchedule(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PixScheduleResponse>> findMySchedules() {
        return ResponseEntity.ok(pixScheduleService.findMySchedules());
    }

}