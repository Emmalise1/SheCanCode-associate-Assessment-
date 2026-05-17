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

flowchart TD
    A[Client Request with Idempotency-Key] --> B{Check Storage}
    
    B -->|New Key| C[Acquire Lock for Key]
    B -->|Existing Key| D{Compare Body Hash}
    
    C --> E[Simulate Payment Processing<br/>2 second delay]
    E --> F[Store Response in Cache]
    F --> G[Release Lock]
    G --> H[Return 200 OK]
    
    D -->|Hash Matches| I[Return Cached Response<br/>X-Cache-Hit: true]
    D -->|Hash Mismatch| J[Return 409 Conflict<br/>Error Message]
    
    I --> K[End]
    J --> K
    H --> K
