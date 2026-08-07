package com.leonardo.bank_api.domain;

import java.math.BigDecimal;

public class Account {
    private Long id;
    private Long customerId;
    private BigDecimal balance;

    public Account() {}

    public Account(Long id, Long customerId, BigDecimal balance) {
        this.id = id;
        this.customerId = customerId;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
