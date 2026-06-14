package com.teya.tinyledger.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void supportsLedgerOperationsThroughApi() throws Exception {
        UUID accountId = openAccount("100.00");

        postAmount("/api/v1/accounts/%s/deposits".formatted(accountId), "25.50")
                .andExpect(status().isOk());
        postAmount("/api/v1/accounts/%s/withdrawals".formatted(accountId), "40.25")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/accounts/%s/balance".formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(85.25));

        mockMvc.perform(get("/api/v1/accounts/%s/transactions".formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].type").value("OPENING_BALANCE"))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[2].type").value("WITHDRAWAL"));
    }

    @Test
    void keepsAccountBalancesIndependent() throws Exception {
        UUID firstAccountId = openAccount("100.00");
        UUID secondAccountId = openAccount("50.00");

        postAmount("/api/v1/accounts/%s/deposits".formatted(firstAccountId), "25.00")
                .andExpect(status().isOk());
        postAmount("/api/v1/accounts/%s/withdrawals".formatted(secondAccountId), "10.00")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/accounts/%s/balance".formatted(firstAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(125.00));

        mockMvc.perform(get("/api/v1/accounts/%s/balance".formatted(secondAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(40.00));
    }

    @Test
    void returnsErrorWhenAccountHasInsufficientFunds() throws Exception {
        UUID accountId = openAccount("20.00");

        postAmount("/api/v1/accounts/%s/withdrawals".formatted(accountId), "20.01")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        mockMvc.perform(get("/api/v1/accounts/%s/balance".formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20.00));

        mockMvc.perform(get("/api/v1/accounts/%s/transactions".formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void returnsErrorForUnknownAccount() throws Exception {
        UUID accountId = UUID.randomUUID();

        postAmount("/api/v1/accounts/%s/deposits".formatted(accountId), "10.00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not found: " + accountId));
    }

    @Test
    void returnsErrorForZeroAmount() throws Exception {
        UUID accountId = openAccount("100.00");

        postAmount("/api/v1/accounts/%s/deposits".formatted(accountId), "0.00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));
    }

    private UUID openAccount(String amount) throws Exception {
        MvcResult result = postAmount("/api/v1/accounts", amount)
                .andExpect(status().isCreated())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        String accountId = JsonPath.read(response, "$.accountId");
        return UUID.fromString(accountId);
    }

    private ResultActions postAmount(String path, String amount) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "amount": "%s"
                        }
                        """.formatted(amount)));
    }
}