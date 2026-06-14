package com.teya.tinyledger.api.controller;

import com.teya.tinyledger.api.dto.AmountRequest;
import com.teya.tinyledger.api.dto.BalanceResponse;
import com.teya.tinyledger.api.dto.TransactionResponse;
import com.teya.tinyledger.application.LedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse openAccount(@RequestBody AmountRequest request) {
        return TransactionResponse.from(ledgerService.openAccount(request.amount()));
    }

    @PostMapping("/accounts/{accountId}/deposits")
    public TransactionResponse deposit(
            @PathVariable UUID accountId,
            @RequestBody AmountRequest request
    ) {
        return TransactionResponse.from(ledgerService.deposit(accountId, request.amount()));
    }

    @PostMapping("/accounts/{accountId}/withdrawals")
    public TransactionResponse withdraw(
            @PathVariable UUID accountId,
            @RequestBody AmountRequest request
    ) {
        return TransactionResponse.from(ledgerService.withdraw(accountId, request.amount()));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse balance(@PathVariable UUID accountId) {
        return new BalanceResponse(accountId, ledgerService.balance(accountId).amount());
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionResponse> transactions(@PathVariable UUID accountId) {
        return ledgerService.history(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

}
