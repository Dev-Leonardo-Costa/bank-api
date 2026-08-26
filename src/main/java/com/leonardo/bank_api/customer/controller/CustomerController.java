package com.leonardo.bank_api.customer.controller;

import com.leonardo.bank_api.customer.controller.customercontrollerdocs.CustomerControllerDocs;
import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;
import com.leonardo.bank_api.customer.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController implements CustomerControllerDocs {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse>  createCustomer(@RequestBody @Valid CreateCustomerRequest request) {
        System.out.println(request.fullName());
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable @Valid Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
}
