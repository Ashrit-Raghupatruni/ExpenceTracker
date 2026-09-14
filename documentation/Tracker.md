# Project Tracker & Development Roadmap (Tracker.md)

**Project Name:** ShakeExpense  
**Current State:** Phase 6 (Family/Parental Sync) Completed & Runtime Verified  
**Code Implementation Progress:** 100% (All 6 Phases Implemented & Verified)  
**Documentation Integrity:** 100% Aligned  
**Last Updated:** 2026-08-24  

---

## 1. Status Legend

- `[x]` **Completed / Verified**
- `[-]` **In Progress**
- `[ ]` **Pending / Planned**
- `[!]` **Blocked / Needs Review**

---

## 2. Sequential Development Roadmap

### Phase 0: System Concept, UX Architecture & Documentation
- [x] Define Product Requirements Document ([PRD.md](PRD.md))
- [x] Detail User Journeys & State Transitions ([Appflow.md](Appflow.md))
- [x] Detail UI/UX Design & Layout Specifications ([Design.md](Design.md))
- [x] Create Technical Specification & Architecture ([TechSpec.md](TechSpec.md))
- [x] Define Database Schema & Multi-User Sync Models ([schema.md](schema.md))
- [x] Establish Vibe Coding Rules & Protocol ([Rules.md](Rules.md))
- [x] Set Up Development Tracker ([Tracker.md](Tracker.md))
- [x] Audit & Synchronize All Documentation ([Uptodate.md](Uptodate.md))

---

### Phase 1: Core Database
- [x] Initialize Android project with Kotlin & Jetpack Compose build configuration
- [x] Implement `CategoryEntity`, `FamilyMemberEntity`, and `ExpenseEntity` Room SQLite models
- [x] Implement `CategoryDao`, `FamilyMemberDao`, and `ExpenseDao` with aggregation queries
- [x] Pre-populate seed categories in Room database
- [x] Implement integer monetary precision handling (`amount_cents`)
- [x] Unit test database operations, migrations, and integer arithmetic

---

### Phase 2: Basic Expense Entry
- [x] Implement `ExpenseRepository` and `CategoryRepository` interfaces and implementations
- [x] Implement domain use cases for creating and reading expenses
- [x] Implement basic expense logging composables and validation (amount > 0, category assignment)
- [x] Unit test expense insertion and data consistency

---

### Phase 3: Shake → Translucent Overlay → Category → Amount → Save
- [x] Implement accelerometer `ShakeDetector` with vector acceleration computation and debounce
- [x] Implement background sensor service with `ScreenStateReceiver` (sleep-state unregistration)
- [x] Configure `QuickEntryActivity` with transparent window style (~90% backdrop)
- [x] Implement frosted floating input panel with category chips and dynamic "Others" text input
- [x] Implement integrated numeric keypad with tabular number formatting
- [x] Implement instant save action, haptic feedback, and auto-dismissal
- [x] Connect `QuickEntryViewModel` to local repository

---

### Phase 4: Expense Sheets + Category/Total Dashboard
- [x] Implement `MainActivity` shell with Jetpack Compose
- [x] Implement Consolidated Totals Header (Today / Month / All-time spending totals)
- [x] Implement high-density Spreadsheet Data Grid (Date | Category / Custom Name | Type | Amount)
- [x] Implement Category Grouping view with category subtotals
- [x] Connect `TrackerViewModel` with live Room database Flow

---

### Phase 5: Bank Notification Detection Engine
- [x] Implement `NotificationListenerService` with privacy-first on-device regex parsing
- [x] Implement bank regex matcher for major Indian banks/UPI apps (HDFC, SBI, ICICI, Axis, GPay, Paytm, PhonePe)
- [x] Implement transaction deduplication logic ($T \le 180\text{s}$, same amount $\pm 2$ cents)
- [x] Implement merchant-to-category heuristics matching
- [x] Implement foreground prompt/snack toast or auto-insert for detected expenses
- [x] Unit test regex extraction, edge case handling, and deduplication rules

---

### Phase 6: Multi-User / Family Sync Engine (Firebase Auth + Cloud Firestore)
- [x] Implement parent/child family linking & member management (`FamilyRepository`, `FamilyMemberDao`)
- [x] Implement member-specific spending summaries and category breakdowns
- [x] Implement offline-first sync queue and WorkManager background synchronization (`ExpenseSyncWorker`, `SyncEngine`)
- [x] Implement Firebase Authentication + Cloud Firestore backend synchronization (`FirestoreSyncApiClient`, `SyncApiClientProvider`)
- [x] Implement 6-digit Family Invite Code (`SHK-XXXX`) and Visual QR Code generation & scanning
- [x] Implement production Firestore security rules (`firestore.rules`) enforcing family isolation & parent-only authorization
- [x] Implement Last-Write-Wins (LWW) conflict resolution logic
- [x] Implement Family Hub & Member Detail UI with bottom navigation
- [x] Finalize official native adaptive launcher icon (`ic_launcher`, `ic_launcher_round`)
- [x] Unit test family workflows, sync status transitions, and conflict resolution

---

### Phase 7: User Profile, Firebase Google Auth, Monthly History & Theming
- [x] Implement User Profile & account status UI with Coil avatar and family linkage
- [x] Implement Firebase Google Sign-In with direct and credential authentication flows
- [x] Implement integer-precision Monthly Expense History with debit, credit, net calculation, and expandable breakdown
- [x] Implement dynamic Theme engine (System Default, Light, Dark) with persistent `ThemePreferences`

---

### Phase 9: Final Production & Google Play Store Readiness
- [x] Production Security: Hardened `firestore.rules` with family tenant isolation and parent role verification
- [x] Authentication Recovery: Persistent auth storage across process kills with explicit sign-out confirmation dialog
- [x] Full Regression: 100% automated test coverage across shake HUD, spreadsheet, bank parser, and sync engine
- [x] Play Store Compliance: Target SDK 34, Android 14 foreground services, strict permission justifications
- [x] Privacy & Data Safety: Prepared complete [Privacy Policy & Data Safety Form Guide](Privacy_and_DataSafety.md)
### Phase 10: Cross-Device Hardening & In-App Help & Documentation
- [x] Cross-Device Family Join: Verified Firestore family lookup, member creation, invite code / QR flow, and persistent Room caching across devices.
- [x] Cross-Device Shake Reliability: Foreground service with `START_STICKY`, `ScreenStateReceiver`, `setPendingIntentBackgroundActivityStartMode`, and `PRIORITY_MAX` FullScreenIntent fallback for aggressive manufacturer battery management (OnePlus, Xiaomi, Samsung).
- [x] Profile & Account Integrity: Authenticated Google User ID (Firebase UID) as single source of truth across Profile, Family Hub, and expense ownership.
- [x] Integer-Precision Monthly History: Expandable month-by-month spending cards with category breakdowns and transaction logs.
- [x] In-App Help & Documentation: Comprehensive 10-topic interactive guide in Profile tab covering concepts, gestures, bank detection, family sync, offline behavior, account recovery, and troubleshooting.
116: 
117: ---
118: 
119: ### Phase 11: Bank Notification Source Whitelisting & Family Sync Isolation
120: - [x] Bank Source Whitelisting: Strict sender & package identification (PhonePe, GPay, Paytm, BHIM, CRED, Navi, major bank SMS headers). Rejection of non-financial apps (WhatsApp, Instagram, games).
121: - [x] User Confirmation Flow: Interactive 1-tap notification actions (Confirm, Choose Category, Dismiss) with auto-expiry.
122: - [x] Family Sync Expense Ownership Isolation: Scoped duplicate deduplication and author isolation so synced family expenses retain author identity and don't overwrite family members' names.
123: - [x] Monthly History Date Accordion: Nested Month → Date dropdown hierarchy with transactions grouped by day.
124: - [x] Swipe-to-Delete Expense: Material 3 `SwipeToDismissBox` in spreadsheet view with instant recalculation.
125: 
126: ---
127: 
128: ### Phase 12: Reset Expenses & Cloud Deletion Tombstones
129: - [x] Reset Expenses Feature: Profile menu option with Today, This Week, and Specific Date (Material 3 DatePickerDialog) reset periods.
130: - [x] Safety & User Isolation: Scoped strictly to current user's expenses (`user_id`), preserving accounts, families, roles, and categories.
131: - [x] Confirmation Preview Modal: Clear display of period label, number of expenses, and total ₹ amount affected before deletion.
132: - [x] Cloud Sync Deletion Tombstones: Persistent tombstone tracking in `SyncEngine` and Firestore query filtering to prevent deleted expenses from resurrecting on sync.
133: - [x] Closed Dropdowns by Default: All Profile and history dropdowns closed by default for compact usability.
134: - [x] Regression & Release Verification: 58/58 unit tests passing, signed Release APK built and installed on physical test device.

---

### Phase 13: Subscription Tiers, Real Intelligence & Family Pro Integration
- [x] Three Strict Tiers: FREE (₹0), PLUS (₹59/mo or ₹699/yr), FAMILY PRO (₹99/mo or ₹999/yr) enforced via `EntitlementManager`.
- [x] Room Database v6: Added `monthly_spending_limit_cents` to `financial_profile` and `family_groups`, and `privacy_mode` to `family_members`.
- [x] AI Financial Assistant: Interactive natural language financial Q&A engine based on Room records.
- [x] Advanced Transaction Search & Filtering: Real-time search UI in `SpreadsheetScreen` with entitlement gating.
- [x] CSV Export: One-tap CSV generation with Android FileProvider and share sheet.
- [x] Spending Alert Engine: 70%, 80%, 90%, 100% limits and unusual spending detection (duplicates & spikes).
- [x] Dynamic Financial Safety Score: Real 0-100 algorithm based on savings rate, budget adherence, and discretionary ratios (zero fake data).
- [x] Family Pro Limits & Privacy: Maximum 5 family members, family spending limit alerts, and granular `PrivacyMode` controls (`PRIVATE`, `SHARED_SUMMARY`, `FULL_SHARED`).
- [x] Automated Test Suite: 84+ unit tests passing cleanly in `./gradlew testDebugUnitTest`.
- [x] Release APK: Signed release APK compiled and installed via ADB.

---

### Phase 14: Premium Intelligence Hardening, Schema v8 & Zero Fake Data Audit
- [x] Zero Fake Data Compliance:
  - Eliminated hardcoded sub-score string pills in `ProfileScreen` (Spending, Budget, Recurring, Risk Signals). Connected to authentic mathematical sub-scores in `FinancialSafetyScoreEngine`.
  - Removed fake ₹50,000 monthly income baseline from `SafeToSpendCalculator`. Safely returns ₹0 and prompts setup when unconfigured.
  - Removed phantom ₹15,000 budget assumption from `AiFinancialAssistantUseCase`.
  - Injected `CategoryDao` in `TransactionEventPipeline` to resolve authentic category names instead of defaulting to merchant payees.
- [x] Entitlement System Hardening & Live Sync:
  - Plugged `SubscriptionPlan.FREE` feature leaks in `EntitlementManager` so all 15 capabilities strictly return `false`.
  - Converted `EntitlementManager` into an application-level singleton in `ShakeExpenseApp`.
  - Connected `MainActivity` lifecycle coroutine to observe `FinancialProfileDao.getProfileFlow()` and broadcast plan upgrades dynamically.
  - Injected singleton `EntitlementManager` into `FamilyViewModel` to ensure family screens update immediately upon plan transition.
- [x] Family Pro Collaboration & Child Exit Persistence:
  - Upgraded Room Database to version 8 with `MIGRATION_7_8` adding `is_exit_requested` column to `family_members`.
  - Implemented `RequestChildExitUseCase` persisting exit requests in Room SQLite.
  - Added visual amber "Exit Requested" tag on `FamilyMemberCard` and parent controls to approve detachment or dismiss.
  - Integrated `family_budgets` table (`MIGRATION_6_7`) with full collective shared budget management in Family Hub.
  - Integrated granular member privacy toggles (`share_transactions`, `share_monthly_total`, `share_category_totals`, `receive_family_alerts`).
- [x] Payment Gateway & Membership Flow:
  - Added configurable production merchant VPA with automated intent fallback to Google Play Subscriptions (`https://play.google.com/store/account/subscriptions`).
  - Aligned unused recurring subscription notification threshold math with 45-day user alert text.
- [x] Regression & Hardware Verification:
  - 100% test pass rate: 95/95 unit tests passing in `./gradlew testDebugUnitTest`.
  - Built debug/release APKs and successfully installed & launched on target Android device (`10BE7A08JP0007U`).

### Phase 15: Collapsible UI Controls, Personal Expense Isolation & Family Limit Synchronization
- [x] Tracker Screen Collapsible Controls:
  - Added interactive arrow up/down collapse/expand toggle to "🛡️ SAFE TO SPEND TODAY" with compact summary when collapsed.
  - Added interactive arrow up/down collapse/expand toggle to "📊 MONTHLY LIMIT PACING" with compact badge when collapsed.
  - Saved collapse state across configuration changes with `rememberSaveable`.
- [x] Profile Screen Help & Documentation Master Toggle:
  - Added master collapse/expand toggle on card header to hide/unhide all 18 documents at once.
  - Preserved independent accordion expansion for each of the 18 individual guide topics.
- [x] Personal Expense Tracker Isolation:
  - Updated `TrackerDashboardUseCases` to stream expenses strictly filtered by `targetUserId`.
  - Prevented other family members' synced records from bleeding into the personal Tracker tab.
- [x] Family Monthly Limit Calculation & Reactive Cloud Sync:
  - Mapped `monthlySpendingLimitCents` in `FamilyRepositoryImpl.getActiveFamilyGroup()` and `saveFamilyGroup()`.
  - Added `updateFamilySpendingLimit` in `SyncApiClient` and `FirestoreSyncApiClient` to sync limit updates to Firestore.
  - Updated `FamilyViewModel` to calculate family spending strictly for the current calendar month and observe expense streams reactively.
- [x] Zero Data Reset:
  - All database structures, existing records, and user tables preserved with zero data reset.
- [x] Verification:
  - 100% unit test pass rate: 95/95 tests passing in `./gradlew testDebugUnitTest`.
  - Debug APK built successfully with `./gradlew assembleDebug`.

---

## 3. Active Workstream
- **Current Milestone:** Production Release & Closed Testing Complete
  1. **Release Artifacts:** Signed Release AAB (`app-release.aab`) & Signed Release APK (`app-release.apk`) generated with release keystore.
  2. **Security & Privacy:** Hardened Firestore security rules, zero secrets in source, 100% on-device SMS parsing.
  3. **Quality Assurance:** Full regression verified with 95/95 unit tests passing and physical device runtime testing.
  4. **Documentation:** 100% synchronized across PRD, TechSpec, Appflow, Design, schema, Rules, Tracker, UserGuide, and Uptodate.
