package com.leonardo.bank_api.adapters.inbound;

import com.leonardo.bank_api.application.CreateAccountUseCase;
import com.leonardo.bank_api.domain.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountControllerAdapter {

    private final CreateAccountUseCase createAccountUseCase;

    public AccountControllerAdapter(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestParam Long customerId) {
        Account created = createAccountUseCase.execute(customerId);
        return ResponseEntity.ok(created);
    }
}
