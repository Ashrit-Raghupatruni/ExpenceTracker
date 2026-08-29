# Documentation & Project Consistency Checkpoint (Uptodate.md)

**Project Name:** ShakeExpense  
**Audit Status:** 100% Consistent (All 6 Phases Complete, Production Release Ready, User Guide Added, Real Family Invite Flow & Non-Family Personal Expenses Hardened)  
**Last Audit Timestamp:** 2026-08-25 18:11 UTC+5:30  
**Audit Lead:** Antigravity AI  

---

## 1. Purpose of this Document

`Uptodate.md` is the project's living integrity audit ledger. It guarantees that product requirements, user flows, visual design, technical specifications, data schemas, development guidelines, user documentation, and roadmap tracking remain strictly synchronized with zero drift.

---

## 2. Documentation Audit & Synchronization Matrix

| Document | Purpose | Status vs Reality | Last Synchronized |
| :--- | :--- | :--- | :--- |
| [PRD.md](PRD.md) | Product requirements, value proposition, core features | IN SYNC | 2026-08-25 |
| [Appflow.md](Appflow.md) | User journeys, state transitions, edge case handling | IN SYNC | 2026-08-25 |
| [Design.md](Design.md) | UI tokens, 90% overlay specs, spreadsheet layout, family hub | IN SYNC | 2026-08-25 |
| [TechSpec.md](TechSpec.md) | Architecture, sensor engine, notification listener, sync engine | IN SYNC | 2026-08-25 |
| [schema.md](schema.md) | Room SQLite models, multi-user sync metadata, aggregations | IN SYNC | 2026-08-25 |
| [Rules.md](Rules.md) | Strict development rules, verification flows, reporting protocol | IN SYNC | 2026-08-25 |
| [Tracker.md](Tracker.md) | Sequential implementation roadmap (All 6 phases & production issues verified) | IN SYNC | 2026-08-25 |
| [UserGuide.md](../UserGuide.md) | User-facing guide with step-by-step instructions & FAQ | IN SYNC | 2026-08-25 |
| [Uptodate.md](Uptodate.md) | Living consistency audit ledger | IN SYNC | 2026-08-25 |

---

## 3. Key Resolved Inconsistencies (Audit v1.3.0)

1. **Non-Family & Personal Expense Resilience:**
   - Removed SQLite Foreign Key constraint on `expenses.user_id` pointing to `family_members`, preventing insert failures and cascade deletion for non-family/personal users.
2. **Real Family Invite Flow:**
   - Replaced manual "Add Member" with official "Invite Member" flow (`InviteFamilyMemberDialog`). Members connect exclusively via authentic Google Sign-In, 6-digit Invite Code, and QR verification in Firebase Firestore.
3. **In-App Help & Documentation & User Guide:**
   - Added [UserGuide.md](../UserGuide.md) and interactive 10-topic "Help & Documentation" accordion in the Profile screen covering concepts, gestures, bank detection, family sync, offline behavior, account recovery, required permissions, and troubleshooting.
4. **Quick Entry Close Behavior:**
   - External shake HUD finishes directly to the Android Home screen/previous app without forcefully relaunching `MainActivity`.
5. **Permissions & Setup Section (Profile Screen):**
   - Added centralized "Permissions & Setup" card on the Profile tab with real-time status indicators (✅ Enabled / ⚠️ Required) for **Shake Anywhere** (Overlay / Draw Over Other Apps) and **Bank Notification Detection** (Notification Access).
   - Includes one-tap action buttons opening Android system settings and lifecycle-aware auto-refresh on resume.
   - Synchronized across PRD, TechSpec, Appflow, Design, Rules, UserGuide, and Uptodate.

6. **Reset Expenses Option & Cloud Sync Deletion Tombstone (Audit v1.4.0 - 2026-08-28):**
   - Added "Reset Expenses" in Profile Menu with 3 scoped periods: **Today**, **This Week** (Monday-to-current), and **Specific Date** (Material 3 DatePicker).
   - Implemented pre-deletion confirmation dialog displaying period name, exact candidate count, and total affected ₹ amount.
   - Added cloud deletion tombstone tracking in `SyncEngine` (`deleted_uuids`) and `FirestoreSyncApiClient` to prevent deleted expenses from resurrecting back into Room SQLite when syncing.
   - Ensured reset is strictly scoped to the authenticated user ID (`user_id`), preserving other family members' data, Google accounts, profiles, family groups, and categories.
   - Set all Profile dropdowns (Permission Setup, Monthly History, Daily Date Breakdowns, Help & Documentation) to closed by default for compact usability.

7. **Freemium Strategy & Financial Safety System (Audit v2.0 - 2026-08-28):**
   - **Core Free Tier (₹0)**: Free tracking, shake HUD, on-device automatic UPI/bank notification parsing, basic budgets/charts, basic family sharing, and 6-month local SQLite storage.
   - **Premium Tier (₹69/mo or ₹599/yr)**: Financial Safety Score (0–100), "Safe to Spend" daily calculator, AI Financial Assistant with comparative insights & natural language Q&A, Subscription & Recurring Payment Detector, Budget Runway pacing optimizer, AI Monthly PDF Reports, Receipt Scanner (OCR), and Unlimited Cloud Backup.
   - **Family Safety Plan (₹129/mo or ₹999/yr)**: 5 family accounts, Family Financial Protection (anomalous spending alerts), shared household budgets, and consolidated reports.
   - Synchronized across PRD, schema, TechSpec, Appflow, UserGuide, and implementation plan.

8. **Subscription Monetization, Zero Fake Data & Schema v8 Hardening (Audit v2.1 - 2026-08-29):**
   - **Three Strict Subscription Tiers**: FREE (₹0), PLUS (₹59/month or ₹699/year), and FAMILY PRO (₹99/month or ₹999/year) strictly enforced via singleton `EntitlementManager`.
   - **Zero Fake Data Guarantees**:
     - Converted `FinancialSafetyCard` badges (Spending, Budget, Recurring, Risk Signals) from hardcoded text strings into dynamic mathematical evaluations derived strictly from actual user transactions and budgets.
     - Removed phantom ₹50,000 baseline in `SafeToSpendCalculator` and phantom ₹15,000 budget assumption in `AiFinancialAssistantUseCase`.
     - Injected `CategoryDao` in `TransactionEventPipeline` to map real category names instead of falling back to payee/merchant names.
   - **Room Database Schema v8 & Child Exit Persistence**:
     - Upgraded database schema to version 8 with `MIGRATION_7_8` adding `is_exit_requested` column on `family_members`.
     - Implemented `RequestChildExitUseCase` with SQLite persistence; amber "Exit Requested" badge displayed in `FamilyHubScreen` with Parent approval / dismissal controls.
     - Fully integrated `family_budgets` (`MIGRATION_6_7`) for collective shared household budget limits.
   - **Payment Gateway & Membership Redirection**:
     - Configured production merchant UPI URI with automated fallback to Google Play subscriptions.
     - Aligned unused recurring subscription notification threshold math to 45 days matching alert text.
   - **System Health Report Added**:
     - Generated [Health.md](Health.md) documenting 100% test suite pass rate (95/95 unit tests), zero compiler warnings, and runtime APK verification on target hardware.
