# Technical Specification (TechSpec.md)

**Project Name:** ShakeExpense  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. Architecture Overview & Proposed Tech Stack

ShakeExpense is designed as a native Android application prioritizing rapid startup, minimal interaction latency, low system resource overhead, and reliable offline-first multi-device synchronization.

### 1.1. Proposed Technology Stack
- **Language:** Kotlin (leveraging Coroutines and Flow for asynchronous reactive operations)
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Architecture Pattern:** Pragmatic MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Local Persistence:** Android Jetpack Room (SQLite abstraction) with support for multi-user sync metadata
- **Background Sync Engine:** Android Jetpack `WorkManager` (periodic & network-constrained background sync)
- **Notification Integration:** Android `NotificationListenerService` (for bank notification parsing & auto-prompting)
- **Sensor Integration:** Android `SensorManager` with Accelerometer sensor
- **Platform Targets (Proposed):** Min SDK 26 (Android 8.0), Target SDK 34 / 35

---

## 2. Technical Architecture & Layers

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ Translucent Overlay   │ │ Main Spreadsheet /    │ │ Bank Prompt   │ │
│  │ (QuickEntryActivity)  │ │ Family Hub (MainAct)  │ │ Heads-up HUD  │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └───────┬───────┘ │
│              │                         │                     │         │
│              ▼                         ▼                     ▼         │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ QuickEntryViewModel   │ │ Tracker / Family VM   │ │ NotifPromptVM │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └───────┬───────┘ │
└──────────────┼─────────────────────────┼─────────────────────┼─────────┘
               │                         │                     │
┌──────────────▼─────────────────────────▼─────────────────────▼─────────┐
│                           REPOSITORY LAYER                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ ExpenseRepository │ CategoryRepository │ FamilySyncRepository    │  │
│  └───────────┬─────────────────────────┬────────────────────────────┘  │
└──────────────┼─────────────────────────┼───────────────────────────────┘
               │                         │
┌──────────────▼─────────────────────────▼───────────────────────────────┐
│                          SERVICES & DATA LAYER                         │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ Room Database (SQLite)│ │ Sensor Service        │ │ Notification  │ │
│  │ Local Expense Ledger  │ │ Shake Detection       │ │ Listener Svc  │ │
│  └───────────▲───────────┘ └───────────────────────┘ └───────────────┘ │
│              │                                                         │
│  ┌───────────▼──────────────────────────────────────────────────────┐  │
│  │ Background Sync Worker (WorkManager) ──> Cloud Family Sync API   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Subsystem Technical Designs

### 3.1. Shake Detection Subsystem

#### Proposed Strategy:
- **Sensor Listener:** Accelerometer vector magnitude computation:
  $$\text{Net Acceleration} = \left|\sqrt{a_x^2 + a_y^2 + a_z^2} - 9.81\right|$$
- **Initial Calibration Parameters:**
  - Trigger Threshold: $\sim 13.0\text{ m/s}^2$ to $14.0\text{ m/s}^2$
  - Direction Reversals: Minimum 2 consecutive peak reversals within $400\text{ms}$
  - Debounce Window: $1000\text{ms} - 1200\text{ms}$ quiet period
- **Battery Optimization Strategy:**
  - `ScreenStateReceiver` unregisters the sensor listener immediately upon `ACTION_SCREEN_OFF` and registers on `ACTION_USER_PRESENT`.

---

### 3.2. Translucent Overlay Strategy

- **Translucent Activity (`Theme.Translucent`):**
  - Single-task Activity with a transparent window style (~90% alpha backdrop).
  - Full Jetpack Compose lifecycle support, native IME keyboard handling, and instantaneous dismissal.

---

### 3.3. Bank Notification Interception & Parsing Subsystem

#### Proposed Strategy:
- **Service Integration:** Android `NotificationListenerService` (`BankNotificationListenerService`).
- **Sender & App Filtering:** Filters notifications matching verified financial package names (e.g. UPI, NetBanking, SMS shortcodes).
- **Regex & Token Extraction Pipeline:**
  - **Amount Extraction:** Regex matching currency signs and numeric values (e.g., `(?:Rs\.?|INR|₹|\$)\s*([\d,]+(?:\.\d{1,2})?)`).
  - **Transaction Type Classification:** Keywords detecting `debited`, `paid`, `spent`, `sent` ($\rightarrow$ `DEBIT`) vs `credited`, `received`, `refund` ($\rightarrow$ `CREDIT`).
  - **Merchant Extraction:** Regex parsing `to / at / Info: [Merchant Name]`.
- **Interactive Quick Categorization Prompt:**
  - Triggers a transient heads-up Compose HUD or high-priority actionable notification.
  - Presents 1-tap category chips. Upon user tap, writes directly to Room SQLite as a verified expense.

---

### 3.4. Family Sync & Parental Control Subsystem

#### Proposed Strategy:
- **Offline-First Synchronization (`WorkManager`):**
  - All transactions are stored locally with `sync_status = PENDING`.
  - A `SyncWorker` triggers on network connectivity (`NetworkType.CONNECTED`).
  - Sends updated local records to the family cloud ledger and pulls updates made by other family members.
  - Upon successful server response, records are updated to `sync_status = SYNCED`.
- **Conflict Resolution:**
  - Client-side UUIDs (`uuid`) and monotonic timestamps (`updated_at`) using Last-Write-Wins (LWW) resolution.
- **Parental Summary Aggregation:**
  - Optimized SQLite queries group records by `user_id` to compute individual member totals and category breakdowns in real-time.

---

### 3.5. Local Persistence & Monetary Precision

- **Monetary Precision:**
  - Expense amounts are stored as integer values (`amount_cents`) representing minor units (cents/paise) to prevent IEEE 754 floating-point drift.
- **Transaction Types:**
  - Stored with an explicit `transaction_type` enum (`DEBIT`, `CREDIT`).

---

## 4. Proposed Package Structure

```
com.shakeexpense.app/
├── data/
│   ├── database/         # AppDatabase, DAOs, Entities (Expense, Category, User, SyncQueue)
│   ├── repository/       # ExpenseRepository, CategoryRepository, FamilySyncRepository
│   └── sync/             # SyncWorker, SyncManager, CloudSyncApi
├── domain/               # Domain models, TransactionParser, UseCases
├── notification/         # BankNotificationListenerService, NotificationParser
├── sensor/               # ShakeDetector, SensorService, ScreenStateReceiver
└── ui/
    ├── overlay/          # QuickEntryActivity, QuickEntryViewModel, OverlayScreen
    ├── notification/     # BankPromptHUD, NotificationPromptViewModel
    ├── tracker/          # MainActivity, TrackerViewModel, SpreadsheetScreen
    ├── family/           # FamilyHubScreen, MemberDetailScreen, FamilyViewModel
    └── theme/            # Color, Type, Theme tokens
```

### 4.5. Permissions & Device Setup Architecture
- **Overlay Permission (`SYSTEM_ALERT_WINDOW`):**
  - Checked via `Settings.canDrawOverlays(context)`.
  - Triggered via `Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))`.
- **Notification Access (`NotificationListenerService`):**
  - Checked via `NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)`.
  - Triggered via `Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)`.
- **Reactive Lifecycle Status Updates:**
  - `ProfileScreen` uses `LifecycleEventObserver` on `ON_RESUME` to automatically refresh status indicators when returning from Android system settings.
- **Privacy & Security Constraints:**
  - 100% on-device notification parsing pipeline; no notification metadata or raw text is dispatched to network services. Only confirmed expenses are recorded to Room/Firestore.

### 4.6. Expense Reset & Cloud Deletion Architecture
- **Scoped Reset (`ResetExpensesUseCase`):**
  - Calculates time ranges for `TODAY` (00:00:00 - 23:59:59), `THIS_WEEK` (Monday 00:00:00 - now), and `SPECIFIC_DATE` (selected date 00:00:00 - 23:59:59).
  - Queries local Room database with `getExpensesInTimeRange(userId, startTime, endTime)` for pre-deletion preview (item count and debit total).
  - Deletes candidate UUIDs locally via `deleteExpensesByUuids(uuids)`.
- **Cloud Deletion & Resurrection Prevention:**
  - `SyncEngine` maintains a persistent set of `deleted_uuids` in `SharedPreferences` as tombstones.
  - Remote pull routines (`pullFamilyExpenses` and `pullUserExpenses`) explicitly filter out records where `uuid in deleted_uuids` or `doc.getBoolean("deleted") == true`.
  - Batch deletion is propagated to Firestore via `SyncApiClient.deleteExpenses(uuids)` using `firestore.batch().delete(docRef)`.
  - Guaranteed user scoping: resets strictly target the active `userId`, preventing any deletion of other family members' records.

### 4.7. Singleton Entitlement Architecture & Real-Time Sync
- **Single Source of Truth:**
  - `EntitlementManager` instantiated as an application-level singleton in `ShakeExpenseApp`.
  - Exposes `currentEntitlement: StateFlow<UserEntitlement>`.
- **Live Reactive Observation:**
  - `MainActivity` launches a coroutine observing `database.financialProfileDao().getProfileFlow(userId)`.
  - On emission, executes `app.entitlementManager.updatePlan(plan)` which immediately propagates to all subscribed ViewModels (`FamilyViewModel`, `TrackerViewModel`, `ProfileViewModel`).
- **Strict Lockdown:**
  - `SubscriptionPlan.FREE` returns `false` across all 15 `FeatureCapability` types. Zero premium leakage.

### 4.8. Zero Fake Data Computing & Dynamic Sub-Scores Engine
- **Financial Safety Score Sub-Scores:**
  - `spendingSubScore`: Evaluated from discretionary debits ratio (`<= 25%` -> `"🟢 Healthy"`, `<= 45%` -> `"🟡 Moderate"`, else `"🔴 High"`).
  - `budgetSubScore`: Evaluated from budget usage (`effectiveBudget <= 0L || budgetUsage <= 70` -> `"🟢 Healthy"`, `<= 90` -> `"🟡 Review"`, else `"🔴 Exceeded"`).
  - `recurringSubScore`: Evaluated from recurring burden ratio (`<= 20%` -> `"🟢 Healthy"`, `<= 35%` -> `"🟡 Review"`, else `"🔴 Heavy"`).
  - `riskSignalsSubScore`: Evaluated from budget overruns (`> 100` -> `"🔴 Alert"`, `> 85` or discretionary `> 50` -> `"🟡 Warning"`, else `"🟢 None"`).
  - Strict null check: returns `null` if no records, income, or expenses exist.
- **Safe-to-Spend Runway:**
  - Strict formula: `Net Pool = Income - (RecurringDebits + SavingsTarget)`.
  - If `incomeCents <= 0L`, returns `0` and prompts user setup.

### 4.9. Event-Driven Spending Limit Thresholds & Anti-Spam Pipeline
- **Pipeline Integration:**
  - Every inserted expense triggers `TransactionEventPipeline`.
  - Calculates budget utilization: `percentage = (currentMonthExpenses / activeLimit) * 100`.
- **Anti-Spam State Machine:**
  - `SpendingAlertNotificationManager` maintains an in-memory hash set of triggered thresholds: `"$userId-$scope-$threshold"`.
  - Thresholds: 70%, 80%, 90%, 100%. Duplicate notifications suppressed until month rollover.
  - Category name is dynamically resolved via `CategoryDao.getAllCategoriesSync()`.

### 4.10. Shared Family Budgets & Persistent Child Exit Architecture
- **Shared Family Budgets:**
  - `FamilyBudgetEntity` stores category-level limits per family group.
  - Aggregates all family member debits per category for shared threshold tracking in Family Hub.
- **Child Exit Request Lifecycle:**
  - Child initiates exit $\to$ `RequestChildExitUseCase` sets `family_members.is_exit_requested = true` in SQLite Room.
  - Real-time Flow triggers UI update: member card shows amber "Exit Requested" badge.
  - Parent action: "Approve & Remove" detaches member via `deleteFamilyMember(id)`; "Dismiss Request" clears flag via `setMemberExitRequested(id, false)`.

---

## 5. Decision & Validation Status

| Decision / Technical Component | Status | Notes |
| :--- | :--- | :--- |
| **Kotlin + Jetpack Compose UI** | Decided | Modern declarative standard for Android |
| **Translucent Activity Overlay** | Selected for Implementation | To be validated for smoothness and ~90% backdrop feel |
| **Room Database (SQLite)** | Decided | Robust offline local persistence with multi-user sync support |
| **Integer Storage for Amounts** | Decided | Prevents floating-point currency drift |
| **NotificationListenerService** | Selected for Implementation | Standard Android service for bank transaction capture |
| **WorkManager Sync Engine** | Selected for Implementation | Battery-efficient, network-constrained offline sync |
| **Last-Write-Wins (LWW) Sync** | Decided | Lightweight conflict resolution with monotonic timestamps |
| **Screen State Receiver for Sensor**| Selected for Implementation | Essential for zero sensor battery draw while screen is off |
| **Cloud Deletion Tombstones** | Decided & Implemented | Prevents Firestore two-way sync from resurrecting deleted records |
| **Room Schema Migration v8** | Decided & Implemented | Migrations 5->6->7->8 with child exit & family budgets |
| **Zero Fake Data Architecture** | Decided & Implemented | Authentic dynamic calculation of scores, runways, and budgets |
| **Singleton Entitlement Engine** | Decided & Implemented | Real-time reactive tier broadcasting with zero premium leaks |

## 6. Cross References
- System Health: [Health.md](Health.md)
- Product requirements: [PRD.md](PRD.md)
- User flows: [Appflow.md](Appflow.md)
- UI and layout specs: [Design.md](Design.md)
- Persistence schema: [schema.md](schema.md)
- Development principles: [Rules.md](Rules.md)
- Implementation tracker: [Tracker.md](Tracker.md)
- Consistency ledger: [Uptodate.md](Uptodate.md)
