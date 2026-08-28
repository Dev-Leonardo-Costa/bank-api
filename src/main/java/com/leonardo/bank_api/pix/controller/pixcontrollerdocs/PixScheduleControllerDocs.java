package com.leonardo.bank_api.pix.controller.pixcontrollerdocs;


import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(
        name = "PIX Agendado",
        description = "Agendamento e processamento de transferências PIX"
)
@SecurityRequirement(name = "bearerAuth")
public interface PixScheduleControllerDocs {

    @Operation(
            summary = "Agendar transferência PIX",
            description = """
                Agenda uma transferência PIX para execução futura.

                O agendamento é criado inicialmente com status SCHEDULED.

                Quando o horário programado é atingido, o Spring Scheduler
                inicia automaticamente o processamento da transferência.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "PIX agendado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Data ou valor do agendamento inválidos"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta de origem"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta ou chave PIX não encontrada"
            )
    })
    ResponseEntity<PixScheduleResponse> createSchedule(@Valid @RequestBody CreatePixScheduleRequest request);


    @Operation(
            summary = "Listar PIX agendados",
            description = """
                Retorna os PIX agendados pertencentes ao usuário autenticado.

                Possíveis status:

                - SCHEDULED
                - PROCESSING
                - COMPLETED
                - FAILED
                - CANCELED
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Agendamentos retornados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            )
    })
    ResponseEntity<List<PixScheduleResponse>> findMySchedules();

    @Operation(
            summary = "Cancelar agendamento PIX",
            description = """
                Cancela um agendamento PIX.

                Somente o proprietário da conta de origem do agendamento
                pode cancelar o agendamento.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Agendamento cancelado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para cancelar o agendamento"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agendamento não encontrado"
            )
    })
    ResponseEntity<PixScheduleResponse> cancelSchedule(@PathVariable Long scheduleId);
}
