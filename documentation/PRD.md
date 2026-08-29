# Product Requirements Document (PRD)

**Project Name:** ShakeExpense (Fast Android Expense Tracker)  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. Product Vision & Value Proposition

Logging daily micro-expenses (coffee, transit, snacks, groceries) in existing apps takes too many steps and too much time. Users frequently abandon logging because opening an app, waiting for loading screens, and navigating nested menus interrupts their daily flow.

**ShakeExpense** provides frictionless expense capture:
- **Zero-Launch Trigger:** The user shakes their phone from the Android home screen.
- **Translucent Quick HUD:** An expense entry interface opens directly over the home screen as a highly transparent (~90% transparent) overlay. The user's home screen remains visible behind it.
- **Streamlined Capture:** The user selects a category (or enters a custom name if "Others" is selected), enters the amount, and the entry is saved immediately with minimal interaction.
- **Spreadsheet-Like Clarity:** Logged expenses are structured in a dense, clean, spreadsheet-style tracker that makes categorized spending and consolidated totals immediately understandable.
- **Smart Notification Detection:** Automatically intercepts bank transaction alerts (debited/credited), asking the user to confirm the category with a single tap before appending to sheets.
- **Family Sync & Parental Monitoring:** Seamlessly links parent and child devices with offline-first synchronization, allowing parents to review cumulative spending and detailed category breakdowns per child.

---

## 2. Target Persona & Problem Statement

- **Target Persona:** 
  1. Individuals wanting instant, effortless personal expense capture.
  2. Families / Parents seeking visibility into household or children's spending without manual consolidation or constant manual check-ins.
- **Core Problem:** High interaction friction leads to untracked transactions, and multi-user household/child expense monitoring is fragmented across disjointed accounts.
- **Success Goal:** Make capturing an expense so fast and lightweight (target capture time: under 3 seconds) that logging becomes an effortless reflex, while offering automated bank notification logging and offline-first family sync.

---

## 3. Core Functional Requirements

### 3.1. Home Screen Shake Detection
- The app must detect a physical shake gesture when the user is on the Android home screen.
- Shake detection must only be active when the device display is on and unlocked.
- Detection must include debounce and noise-filtering logic to prevent accidental triggers (such as normal walking or setting the phone down).

### 3.2. Translucent Quick-Entry Overlay (~90% Transparent)
- When triggered by a shake, the expense entry interface must appear as a floating, highly transparent overlay (~90% transparent backdrop).
- The user's home screen, wallpaper, and app icons must remain clearly visible behind the overlay.
- **Category Selection:**
  - Displays primary predefined categories for fast 1-tap selection.
  - Includes an **"Others"** category.
- **Custom Category ("Others"):**
  - When the user selects "Others", an inline input must appear to allow entering a custom category name.
- **Amount Entry:**
  - An integrated numeric entry interface allows typing the expense amount directly.
- **Instant Save & Auto-Dismiss:**
  - Saving occurs immediately with minimal taps.
  - Upon saving, the overlay dismisses cleanly, returning the user to their home screen without intermediate confirmation screens.

### 3.3. Spreadsheet-Style Expense Tracker (Main Application)
- **Tabular Data Presentation:**
  - Expenses are organized in a clean, high-density layout inspired by spreadsheets.
  - Columns display: Date/Time, Category (or custom category name), Transaction Type (Debited/Credited), and Amount.
- **Categorical Breakdown:**
  - Expenses must be separately understandable and grouped by category, allowing the user to quickly see where money was spent.
- **Consolidated Total View:**
  - A consolidated summary displays total expenses clearly at a glance (e.g., total spent today, this month, all-time).

### 3.4. Family Sync & Parental Control
- **Family Group & Role Linking:**
  - Users can create or join a family group with roles: **Parent** or **Child / Member**.
  - Linking is established via a secure invite code or QR code.
- **Offline-First Synchronization:**
  - Expenses are recorded locally immediately on the child/parent device.
  - When an internet connection becomes available, local records sync automatically in the background with the family cloud ledger.
- **Member Overview & Drill-Down:**
  - In the family hub, parents can see all linked members.
  - Clicking on a specific child/member opens their individual expense dashboard, displaying:
    - **Total Cumulative Expenses** for that user.
    - **Category-wise breakdown** of their spending.
    - **Chronological spreadsheet transaction list** logged by that member.

### 3.5. Bank Notification Detection & 1-Tap Categorization
- **Notification Detection & Filtering:**
  - Intercepts incoming push/SMS notifications from recognized banking and payment applications (e.g., Google Pay, PhonePe, Paytm, HDFC, SBI, ICICI, Chase, etc.).
- **Transaction Parsing:**
  - Parses message contents to extract:
    - **Amount** (e.g., ₹250.00 / $15.00).
    - **Transaction Type** (Debited / Credited).
    - **Merchant / Beneficiary name** (if available).
- **Interactive Quick Categorization Prompt:**
  - Presents a non-intrusive prompt (Heads-up notification action or floating banner) stating: *"Detected [Debited/Credited] of [Amount]. What is this for?"*
  - Offers fast 1-tap category chips (e.g., Food, Transit, Groceries, Shopping, Others).
- **Confirmation & Automatic Sheet Logging:**
  - Once the user selects a category and confirms, the transaction is automatically written to the local database / spreadsheet sheet.
  - Unconfirmed or dismissed prompts can be reviewed later from a "Pending Review" queue in the main tracker.

---

## 4. Product Performance & Experience Goals

- **Target Interaction Latency:** The entire quick-entry flow (shake $\rightarrow$ select category $\rightarrow$ enter amount $\rightarrow$ save) aims for an overall completion time of under 3 seconds.
- **Instant Overlay Launch:** The overlay should render smoothly and immediately upon shake detection without perceptual lag.
- **Low Overhead:** Background shake sensing and notification listeners must minimize battery consumption by pausing sensor evaluation whenever the device screen is off.
### 3.7. Permissions & Setup (Profile Screen)
- **Centralized Device Setup:**
  - Dedicated "Permissions & Setup" card on the Profile screen showing the status of essential features.
- **Shake Anywhere (Overlay / Draw Over Other Apps):**
  - Live status indicator (✅ Enabled / ⚠️ Required).
  - One-tap button opening Android's "Display over other apps" settings.
  - Explains that the permission is required to trigger Quick Entry from the Home Screen or other apps.
- **Bank Notification Detection (Notification Access):**
  - Live status indicator (✅ Enabled / ⚠️ Required).
  - One-tap button opening Android's Notification Access settings.
  - Explains: *"ShakeExpense reads supported bank/payment notifications on your device to detect debit/credit transactions and ask for confirmation before adding them to your expenses."*
- **Explicit User Control & Privacy Guarantee:**
  - Permissions are never automatically enabled; the user must grant them explicitly in Android Settings.
  - 100% on-device parsing guarantee: raw notification contents are never uploaded to Firebase or external servers; only user-confirmed expenses are persisted.

---

## 4. Non-Functional Requirements

## 5. Scope & Feature Boundaries

| Feature Area | In Scope | Out of Scope |
| :--- | :--- | :--- |
| **Trigger** | Shake from Android home screen, Bank notification intercept | Custom hardware buttons, voice commands |
| **Entry Overlay** | Category selection + "Others" custom name + Amount + Instant Save | Photo receipts, OCR bill scanning, split bill calculators |
| **Bank Notifications** | Credited/Debited extraction + 1-tap category prompt + auto-sheet logging | Direct Open Banking / Plaid API integrations, automated bank credential scraping |
| **Family Sync** | Parent-Child linking, offline-first sync on internet available, member total & breakdown view | Shared bank account credential pooling, debit card remote spending freeze |
| **Tracker** | Spreadsheet-like table, category grouping, consolidated totals, member breakdowns | Full enterprise accounting ledgers, investment portfolio tracking |
| **Data Storage** | Local on-device Room SQLite + background cloud sync for family ledger | Mandatory sign-in for single-user offline usage |

---

## 6. Assumptions & Open Questions

### Assumptions
- Android permissions required for background sensor listening, notification listening (`NotificationListenerService`), and drawing overlays can be granted during initial onboarding.
- A single default currency matching the user's locale is sufficient for personal and family tracking.

### Open Questions (To Validate During Prototyping)
- What regex / pattern matching pipeline ensures the highest accuracy across diverse international and regional bank SMS / notification templates?
- What cloud sync backend (e.g., Firebase Firestore, Supabase, or lightweight WebSocket service) provides the cleanest conflict resolution for offline-first family sync?

---

## 8. Freemium Architecture & Financial Safety System (v2.0)

### 8.1. Monetization Philosophy: Free to Form Habits, Paid for Intelligence & Safety
- **Core App is Free (₹0)**:
  - Frictionless manual expense entry and shake overlay HUD.
  - Automatic on-device UPI/bank transaction detection with 1-tap confirmation.
  - Core categories, daily/monthly views, basic budgets, and basic charts.
  - Basic family expense sharing, date filters, on-device SQLite storage, and transaction search.
- **Premium Tier (₹69/month or ₹599/year) — "Understand + Protect + Optimize"**:
  - **🛡️ Financial Safety Score & Engine (USP)**: 0–100 score combining spending pace, budget discipline, recurring expense load, savings cushion, and suspicious transaction risk signals.
  - **💡 Safe to Spend**: Dynamic calculation showing true disposable spending per day (`Balance - upcoming bills - EMI - subscriptions - savings goal`).
  - **🤖 AI Financial Assistant & Insights**: Comparative monthly anomalies (e.g., *"₹1,840 more on food this month than last (+27% above normal)"*) and conversational financial inquiries.
  - **🔄 Subscription & Recurring Payment Detector**: Auto-detection of OTT, SIP, EMI, Rent, and mobile recharges with unused subscription alerts.
  - **📊 Budget Runway & Pacing Intelligence**: Predictive exhaustion forecasts and daily spending adjustment targets.
  - **📑 AI Monthly Financial Report**: Formatted summaries, category shifts, comparison against previous months, and PDF/CSV export.
  - **📸 Receipt Scanner**: On-device OCR for item-level extraction and automated categorization.
  - **☁️ Unlimited History & Cloud Backup**: Unlimited historical records and multi-device cloud synchronization.
- **Family Safety Plan (₹129/month or ₹999/year) — Household Protection**:
  - All Premium capabilities for up to 5 family member accounts.
  - Family Financial Protection: Parent alerts for unusual spending spikes or unexpected categories.
  - Shared household budgets, collaborative family goals, and consolidated monthly reports.

### 8.2. Final Production Subscription Matrix (v2.1)

| Plan | Pricing | Target Audience | Key Entitlements |
| :--- | :--- | :--- | :--- |
| **FREE** | ₹0 | General Users | Manual entry, shake HUD, on-device bank SMS listener, basic spreadsheet, 6-month history. Strictly zero premium leaks. |
| **PLUS** | ₹59/mo or ₹699/yr | Individual Power Users | Safe-to-Spend daily runway, 0-100 Financial Safety Score, AI Assistant, Next-Month Prediction, 70-100% Spending Alerts, CSV Export, Advanced Search, Recurring Detection. |
| **FAMILY PRO** | ₹99/mo or ₹999/yr | Families & Households | All PLUS features + up to 5 family members, Shared Family Budgets, Granular Privacy Controls, Family Limit Alerts, and Child Exit Request management. |

### 8.3. Core Architectural Principle: Zero Fake / Demo Data
- No hardcoded financial estimates (no ₹50,000 baseline, no phantom ₹15,000 budget).
- Financial Safety Score returns empty states if no transactions exist.
- Dynamic Sub-Scores (Spending, Budget, Recurring, Risk Signals) compute mathematically from actual user debit entries.
- Category names map dynamically via database references.

### 8.4. Advanced Family Pro Governance
- **Hard Cap of 5 Members**: Enforced on group creation and member join.
- **Shared Collective Budgets**: Set category spending caps for the whole household with live aggregation.
- **Granular Member Privacy Toggles**: Each member can independently toggle `shareTransactions`, `shareMonthlyTotal`, `shareCategoryTotals`, and `receiveFamilyAlerts`.
- **Persistent Child Exit Request Flow**: Children cannot detach silently; exit requests are persisted in SQLite Room and require explicit Parent approval.

---

## 9. Cross References
- System Health: [Health.md](Health.md)
- User interaction flows: [Appflow.md](Appflow.md)
- Visual hierarchy and UI layout: [Design.md](Design.md)
- Engineering architecture and decisions: [TechSpec.md](TechSpec.md)
- Data models and storage: [schema.md](schema.md)
- Development principles: [Rules.md](Rules.md)
- Implementation progress: [Tracker.md](Tracker.md)
- Consistency ledger: [Uptodate.md](Uptodate.md)

