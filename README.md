# Tiny Ledger API

A small Spring Boot REST API for opening accounts, recording deposits and withdrawals, checking balances, and reading transaction history.

The implementation is intentionally in-memory. The API is a thin transport layer over the application and domain logic.

## Requirements

- Java 21
- Maven 3.9+

## Run tests

```bash
mvn test
```

The build also generates a JaCoCo report:

```text
target/site/jacoco/index.html
```

## Run locally

```bash
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

## API examples

### Open an account

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"amount":"100.00"}'
```

The endpoint returns `201 Created`:

```json
{
  "id": "c3b8b720-6450-449e-80e1-3e02c747a813",
  "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
  "type": "OPENING_BALANCE",
  "amount": 100.00,
  "createdAt": "2026-06-08T11:59:00Z"
}
```

Opening an account records and returns an `OPENING_BALANCE` transaction. The opening balance may be `0.00`.

### Deposit money

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts/{accountId}/deposits \
  -H 'Content-Type: application/json' \
  -d '{"amount":"25.50"}'
```

The endpoint returns `200 OK`:

```json
{
  "id": "7a2a4d49-09ac-4f9f-9f6d-280a2eb4e4e9",
  "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
  "type": "DEPOSIT",
  "amount": 25.50,
  "createdAt": "2026-06-08T12:00:00Z"
}
```

### Withdraw money

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts/{accountId}/withdrawals \
  -H 'Content-Type: application/json' \
  -d '{"amount":"40.25"}'
```

The endpoint returns `200 OK`:

```json
{
  "id": "0a739add-d5a4-40ff-aa2f-80e1efdb028f",
  "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
  "type": "WITHDRAWAL",
  "amount": 40.25,
  "createdAt": "2026-06-08T12:01:00Z"
}
```

### View current balance

```bash
curl -s http://localhost:8080/api/v1/accounts/{accountId}/balance
```

```json
{
  "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
  "balance": 85.25
}
```

### View transaction history

```bash
curl -s http://localhost:8080/api/v1/accounts/{accountId}/transactions
```

```json
[
  {
    "id": "c3b8b720-6450-449e-80e1-3e02c747a813",
    "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
    "type": "OPENING_BALANCE",
    "amount": 100.00,
    "createdAt": "2026-06-08T11:59:00Z"
  },
  {
    "id": "7a2a4d49-09ac-4f9f-9f6d-280a2eb4e4e9",
    "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
    "type": "DEPOSIT",
    "amount": 25.50,
    "createdAt": "2026-06-08T12:00:00Z"
  },
  {
    "id": "0a739add-d5a4-40ff-aa2f-80e1efdb028f",
    "accountId": "f3fb2d18-2c0f-4389-9f7a-3e9f2bde7d0a",
    "type": "WITHDRAWAL",
    "amount": 40.25,
    "createdAt": "2026-06-08T12:01:00Z"
  }
]
```

## Error handling

Domain and storage failures intentionally use `RuntimeException` with explicit messages to keep this take-home solution small. The API returns them as `400 Bad Request`:

```json
{
  "message": "Insufficient funds"
}
```

Other possible messages include:

- `Amount is required`
- `Amount must not be negative`
- `Amount must be greater than zero`
- `Amount must not have more than two decimal places`
- `Account not found: {accountId}`

This is a deliberate simplification, not the preferred production design. A production version should introduce domain-specific exceptions and map them to API responses by exception type.

## Assumptions

- Accounts are opened explicitly through the API.
- Opening an account records one `OPENING_BALANCE` transaction.
- The opening balance may be zero.
- Deposits and withdrawals must be greater than zero.
- Amounts cannot be negative and support at most two decimal places.
- Withdrawals cannot overdraw the account balance.
- Accounts have independent balances and transaction histories.
- Transaction history is append-only and returned in creation order.
- Data is stored in memory and is lost when the application stops.
- Currency is intentionally not modelled.
- Database transactions, authentication, and logging are outside the assignment scope.
- The API is intentionally a thin transport layer over the application service.
- API tests focus on key business flows and errors rather than exhaustive HTTP, JSON parsing, or Spring framework behaviour.

## Design

### Package structure

```text
api.controller  HTTP endpoints
api.dto         request and response contracts
api.error       API error mapping
application     use-case orchestration
domain          account, money, and ledger model
infrastructure  in-memory storage
```

### Domain model

`Money` wraps `BigDecimal`, normalises values to two decimal places, rejects negative values, and avoids floating-point arithmetic.

`Account` owns the current balance and validates deposits and withdrawals. A successful operation updates the balance and creates a `LedgerTransaction`.

`LedgerTransaction` records the transaction id, account id, type, amount, and creation time.

### Storage

`InMemoryLedgerStore` keeps accounts and transaction histories in separate maps indexed by account id. It returns copies of transaction history so callers cannot mutate the stored collection.

`LedgerService` orchestrates account creation, deposits, withdrawals, balance queries, and transaction history queries.

## Testing strategy

- Domain tests cover money rules and account operations.
- Application tests cover orchestration, account isolation, balances, and transaction history.
- Store tests cover account and transaction persistence in memory.
- API integration tests verify the main ledger flow and important business errors through HTTP.

The API is only an additional way to access the ledger, so tests do not exhaustively cover malformed JSON, serialization details, or Spring framework behaviour.

Some straightforward validation guards, such as null checks on internal constructor arguments, are intentionally not covered because testing each one would add little value to this take-home assignment. A production-ready solution should add tests for every validation branch and error contract.
