package com.leonardo.bank_api.pixschedule.unitario;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.entity.PixSchedule;
import com.leonardo.bank_api.pix.mapper.PixScheduleMapper;
import com.leonardo.bank_api.pix.repository.PixKeyRepository;
import com.leonardo.bank_api.pix.repository.PixScheduleRepository;
import com.leonardo.bank_api.pix.service.impl.PixScheduleServiceImpl;
import com.leonardo.bank_api.shared.enums.PixScheduleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixScheduleServiceImplTest {

    @Mock
    private PixScheduleRepository pixScheduleRepository;

    @Mock
    private PixKeyRepository pixKeyRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PixScheduleMapper pixScheduleMapper;

    @InjectMocks
    private PixScheduleServiceImpl pixScheduleService;

    private Customer customer;
    private Account sourceAccount;
    private Account destinationAccount;
    private PixKey destinationPixKey;

    @BeforeEach
    void setUp() {

        customer = Customer.builder()
                .id(1L)
                .fullName("Leonardo Costa")
                .email("leonardo@email.com")
                .build();

        sourceAccount = Account.builder()
                .id(5L)
                .customer(customer)
                .balance(new BigDecimal("1000.00"))
                .build();

        Customer destinationCustomer = Customer.builder()
                .id(2L)
                .fullName("Maria Silva")
                .email("maria@email.com")
                .build();

        destinationAccount = Account.builder()
                .id(10L)
                .customer(destinationCustomer)
                .balance(new BigDecimal("500.00"))
                .build();

        destinationPixKey = PixKey.builder()
                .id(1L)
                .keyValue("79849933003")
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "leonardo@email.com",
                                null
                        )
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreatePixScheduleSuccessfully() {

        LocalDateTime scheduledAt =
                LocalDateTime.now().plusDays(1);

        CreatePixScheduleRequest request =
                new CreatePixScheduleRequest(
                        5L,
                        "79849933003",
                        new BigDecimal("100.00"),
                        scheduledAt
                );

        PixSchedule schedule = PixSchedule.builder()
                .sourceAccount(sourceAccount)
                .pixKey("79849933003")
                .amount(new BigDecimal("100.00"))
                .scheduledAt(scheduledAt)
                .status(PixScheduleStatus.SCHEDULED)
                .build();

        PixSchedule savedSchedule = PixSchedule.builder()
                .id(1L)
                .sourceAccount(sourceAccount)
                .pixKey("79849933003")
                .amount(new BigDecimal("100.00"))
                .scheduledAt(scheduledAt)
                .status(PixScheduleStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        PixScheduleResponse expectedResponse =
                new PixScheduleResponse(
                        1L,
                        5L,
                        "79849933003",
                        new BigDecimal("100.00"),
                        scheduledAt,
                        PixScheduleStatus.SCHEDULED,
                        savedSchedule.getCreatedAt()
                );

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        when(pixKeyRepository.findByKeyValue("79849933003"))
                .thenReturn(Optional.of(destinationPixKey));

        when(pixScheduleMapper.toEntity(
                request,
                sourceAccount,
                destinationPixKey
        )).thenReturn(schedule);

        when(pixScheduleRepository.save(schedule))
                .thenReturn(savedSchedule);

        when(pixScheduleMapper.toResponse(savedSchedule))
                .thenReturn(expectedResponse);

        PixScheduleResponse response =
                pixScheduleService.createSchedule(request);

        assertThat(response).isNotNull();

        assertThat(response.id())
                .isEqualTo(1L);

        assertThat(response.sourceAccountId())
                .isEqualTo(5L);

        assertThat(response.pixKey())
                .isEqualTo("79849933003");

        assertThat(response.amount())
                .isEqualByComparingTo("100.00");

        assertThat(response.status())
                .isEqualTo(PixScheduleStatus.SCHEDULED);

        verify(accountRepository)
                .findById(5L);

        verify(pixKeyRepository)
                .findByKeyValue("79849933003");

        verify(pixScheduleRepository)
                .save(schedule);

        verify(pixScheduleMapper)
                .toResponse(savedSchedule);
    }

    @Test
    void shouldThrowExceptionWhenSourceAccountDoesNotExist() {

        CreatePixScheduleRequest request =
                new CreatePixScheduleRequest(
                        999L,
                        "79849933003",
                        new BigDecimal("100.00"),
                        LocalDateTime.now().plusDays(1)
                );

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixScheduleService.createSchedule(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta de origem não encontrada");

        verify(accountRepository)
                .findById(999L);

        verifyNoInteractions(
                pixKeyRepository,
                pixScheduleRepository,
                pixScheduleMapper
        );
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotBelongToAuthenticatedUser() {

        CreatePixScheduleRequest request =
                new CreatePixScheduleRequest(
                        5L,
                        "79849933003",
                        new BigDecimal("100.00"),
                        LocalDateTime.now().plusDays(1)
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "outro@email.com",
                                null
                        )
                );

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() ->
                pixScheduleService.createSchedule(request)
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(
                        "Você não possui permissão para agendar PIX nesta conta"
                );

        verify(accountRepository)
                .findById(5L);

        verifyNoInteractions(
                pixKeyRepository,
                pixScheduleRepository,
                pixScheduleMapper
        );
    }

    @Test
    void shouldThrowExceptionWhenPixKeyDoesNotExist() {

        CreatePixScheduleRequest request =
                new CreatePixScheduleRequest(
                        5L,
                        "chave-inexistente",
                        new BigDecimal("100.00"),
                        LocalDateTime.now().plusDays(1)
                );

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        when(pixKeyRepository.findByKeyValue("chave-inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixScheduleService.createSchedule(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Chave PIX de destino não encontrada"
                );

        verify(accountRepository)
                .findById(5L);

        verify(pixKeyRepository)
                .findByKeyValue("chave-inexistente");

        verify(pixScheduleRepository, never())
                .save(any());

        verifyNoInteractions(pixScheduleMapper);
    }

    @Test
    void shouldReturnAuthenticatedUserSchedules() {

        String email = "leonardo@email.com";

        PixSchedule firstSchedule = PixSchedule.builder()
                .id(1L)
                .sourceAccount(sourceAccount)
                .pixKey("79849933003")
                .amount(new BigDecimal("100.00"))
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .status(PixScheduleStatus.SCHEDULED)
                .build();

        PixSchedule secondSchedule = PixSchedule.builder()
                .id(2L)
                .sourceAccount(sourceAccount)
                .pixKey("79849933003")
                .amount(new BigDecimal("200.00"))
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .status(PixScheduleStatus.SCHEDULED)
                .build();

        PixScheduleResponse firstResponse =
                new PixScheduleResponse(
                        1L,
                        sourceAccount.getId(),
                        firstSchedule.getPixKey(),
                        firstSchedule.getAmount(),
                        firstSchedule.getScheduledAt(),
                        firstSchedule.getStatus(),
                        null
                );

        PixScheduleResponse secondResponse =
                new PixScheduleResponse(
                        2L,
                        sourceAccount.getId(),
                        secondSchedule.getPixKey(),
                        secondSchedule.getAmount(),
                        secondSchedule.getScheduledAt(),
                        secondSchedule.getStatus(),
                        null
                );

        when(pixScheduleRepository
                .findAllBySourceAccountCustomerEmail(email))
                .thenReturn(List.of(
                        firstSchedule,
                        secondSchedule
                ));

        when(pixScheduleMapper.toResponse(firstSchedule))
                .thenReturn(firstResponse);

        when(pixScheduleMapper.toResponse(secondSchedule))
                .thenReturn(secondResponse);

        List<PixScheduleResponse> result =
                pixScheduleService.findMySchedules();

        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(pixScheduleRepository)
                .findAllBySourceAccountCustomerEmail(email);

        verify(pixScheduleMapper)
                .toResponse(firstSchedule);

        verify(pixScheduleMapper)
                .toResponse(secondSchedule);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoSchedules() {

        String email = "leonardo@email.com";

        when(pixScheduleRepository
                .findAllBySourceAccountCustomerEmail(email))
                .thenReturn(List.of());

        List<PixScheduleResponse> result =
                pixScheduleService.findMySchedules();

        assertThat(result).isEmpty();

        verifyNoInteractions(pixScheduleMapper);
    }
}