#  Idempotency Gateway | IgirePay Challenge

A pure Java implementation of a payment idempotency layer that ensures every transaction is processed **exactly once**, even when clients retry requests due to network timeouts.


##  Table of Contents

- [Architecture Diagram](#architecture-diagram)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)
- [Developer's Choice Feature](#developers-choice-feature)
- [Bonus: Race Condition Handling](#bonus-race-condition-handling)
- [Testing](#testing)
- [Project Structure](#project-structure)


##  Architecture Diagram

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as Idempotency Gateway
    participant Storage as In-Memory Storage
    participant Processor as Payment Processor

    Client->>Gateway: POST /process-payment<br/>Header: Idempotency-Key: K1<br/>Body: {"amount":100,"currency":"GHS"}
    
    Gateway->>Storage: Lookup Key: K1
    
    alt First Request (Key Not Found)
        Storage-->>Gateway: Not Found
        Gateway->>Processor: Process Payment (2s delay)
        Processor-->>Gateway: Payment Successful
        Gateway->>Storage: Store Response under K1
        Storage-->>Gateway: Stored
        Gateway-->>Client: 200 OK<br/>"Charged 100 GHS"
        
    else Duplicate Request (Key Found, Same Body)
        Storage-->>Gateway: Found + Hash Match
        Gateway-->>Client: 200 OK<br/>X-Cache-Hit: true<br/>"Charged 100 GHS"
        
    else Conflict (Key Found, Different Body)
        Storage-->>Gateway: Found + Hash Mismatch
        Gateway-->>Client: 409 Conflict<br/>"Idempotency key already used<br/>for a different request body"
    end
```
---
## Setup Instructions

```bash
git clone https://github.com/Emmalise1/SheCanCode-associate-Assessment-.git
cd SheCanCode-associate-Assessment-
javac -d . src/com/igirepay/*.java
java com.igirepay.Main
```
> Server runs on `http://localhost:8080`




