# Architecture

PesaIQ follows the **MVVM (Model-View-ViewModel)** architectural pattern combined with the **Repository Pattern** to ensure a clean separation of concerns, testability, and maintainability.

## Layers

### 1. Presentation Layer (UI)
- **Activities & Fragments:** Responsible for displaying data and capturing user interactions.
- **ViewModels:** Hold and manage UI-related data in a lifecycle-conscious way. They communicate with the Repository to fetch or update data using Kotlin Coroutines and Flow.
- **ViewBinding:** Used for safe and efficient interaction with UI components.

### 2. Domain Layer (Repository)
- **Repository:** Acts as a mediator between different data sources (Room database and SMS provider). It contains business logic such as duplicate detection and final categorization logic before persistence.

### 3. Data Layer (Storage & External)
- **Room Database:** The primary source of truth. Stores transactions, budgets, and categories locally.
- **SMS Parser & Receiver:** Listens for incoming SMS via a `BroadcastReceiver`, parses the text into structured objects using `MpesaParser`, and passes them to the Repository.

## Data Flow (Incoming SMS)
1. **SMS Receiver:** Intercepts `SMS_RECEIVED_ACTION`.
2. **Parser:** Validates if the message is from M-Pesa and extracts details (ID, Amount, Recipient, Date).
3. **Repository:** Checks for duplicates and saves the transaction to the Room DB.
4. **UI:** ViewModels observing the database via `Flow` automatically update the UI when a new transaction is inserted.

## Diagram
```
Presentation Layer (Fragments)
       │
 ViewModels (State Management)
       │
 Repository (Data Orchestration)
       │
 Room Database (Local Storage) <── SMS Parser (External Input)
```
