package com.leonardo.bank_api.pix.service;

import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;

import java.util.List;

public interface PixScheduleService {

    PixScheduleResponse createSchedule(CreatePixScheduleRequest request);

    List<PixScheduleResponse> findMySchedules();

}