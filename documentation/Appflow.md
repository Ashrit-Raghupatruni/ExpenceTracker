# Application Flow & State Transitions (Appflow.md)

**Project Name:** ShakeExpense  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. High-Level Experience Map

ShakeExpense provides multiple frictionless entry points and centralized family financial tracking:

1. **The Fast Entry Overlay (Triggered by Shake on Home Screen):** A transient, ~90% transparent HUD allowing instant expense capture without opening a full application window.
2. **Bank Notification Auto-Prompt:** Heads-up categorization prompt triggered whenever a bank transaction alert (debited/credited) is detected.
3. **The Spreadsheet Tracker (Main App):** A high-density data screen displaying recorded expenses in a spreadsheet format with category breakdowns and consolidated totals.
4. **Family Hub & Parental Monitoring (Main App):** Linked family screen where parents can click on individual members/children to see their cumulative spending and detailed transaction history, synced offline-first.

```
[Android Home Screen]
        │
   (Shake Device)
        ▼
[90% Translucent Quick-Entry Overlay]
   ├── [Select Standard Category] ──> [Enter Amount] ──> [Save & Dismiss]
   └── [Select "Others"] ──> [Enter Custom Category Name] ──> [Enter Amount] ──> [Save & Dismiss]

[Bank Notification Arrives]
        │
 (Parser Extracts Amount & Type)
        ▼
[Heads-Up Quick Categorization Prompt]
   ├── [Tap Category Chip] ──> [Confirm & Auto-Append to Sheets]
   └── [Dismiss] ──> [Moved to Pending Review Queue]

[App Icon Launch (Main Hub)]
        │
        ├── [Spreadsheet Tracker Tab]
        │      ├── [Consolidated Totals Banner] (Today / Month / All-time)
        │      ├── [Spreadsheet Data Grid] (Date | Category | Type | Amount)
        │      └── [Grouped by Category View]
        │
        └── [Family Hub Tab]
               ├── [Family Overview Banner]
               ├── [Member Cards List] (Child A, Child B, Parent)
               │      │ (Click Member Card)
               │      ▼
               └── [Member Detail View] (Member Total Spending + Category Breakdown + Sheets)
```

---

## 2. Detailed User Journeys

### Journey 1: Standard Quick-Entry (Predefined Category)
1. **Trigger:** The user shakes their phone while on the Android home screen.
2. **Overlay Appearance:** The overlay renders over the home screen with ~90% transparency, keeping the wallpaper and icons visible beneath.
3. **Category Selection:** The user taps a category option (e.g., *Food*, *Transport*, *Groceries*, *Bills*, *Shopping*).
   - The selected category highlights.
   - Focus immediately advances to amount entry.
4. **Amount Entry:** The user enters the numerical expense amount using the integrated numeric keypad.
5. **Immediate Save:** The user taps the save confirmation action.
   - The expense is written to local storage.
   - A brief haptic confirmation triggers.
   - The overlay immediately dismisses, leaving the user back on their home screen.

---

### Journey 2: Quick-Entry with Custom Category ("Others")
1. **Trigger:** The user shakes their phone on the home screen; the translucent overlay appears.
2. **Select "Others":** The user taps the **Others** category option.
3. **Custom Name Entry:**
   - An inline text field expands directly within the overlay.
   - The user types the specific custom category name (e.g., *Book*, *Coffee Bean*, *Gym Pass*).
4. **Amount Entry:** The user transitions to entering the amount on the numeric keypad.
5. **Immediate Save:** The user confirms save; the record is stored with the custom category name; the overlay closes.

---

### Journey 3: Bank Notification Capture & 1-Tap Categorization
1. **Trigger:** A bank SMS or app push notification arrives (e.g. *"Acct XX1234 debited by Rs 350.00 at Starbucks"*).
2. **Detection & Extraction:** The background listener intercepts the notification, extracting `Amount = 350.00`, `Type = DEBIT`, `Merchant = Starbucks`.
3. **Prompt Display:** A non-intrusive prompt appears:
   - *"Detected DEBIT of ₹350.00. What is this expense for?"*
4. **1-Tap Selection:** The user taps a category chip (e.g., *Food*).
5. **Auto-Append to Sheets:** The record is instantly inserted into the local expense ledger / sheet with source tagged as `BANK_NOTIFICATION`.

---

### Journey 4: Family Setup, Offline Sync & Parental Monitoring
1. **Family Linking:** Parent creates a Family Group and generates an invite QR/code. Child scans code to link device under role `CHILD`.
2. **Offline Logging:** Child spends money and logs transactions (via Shake Overlay or Bank Notification) without needing active Wi-Fi/data.
3. **Background Sync:** Once the device reconnects to the internet, `WorkManager` automatically pushes child records to the family cloud ledger.
4. **Parental Review:** Parent opens the **Family Hub** in the main app:
   - Sees family members listed with their total expenditures.
   - Parent taps on a specific child.
   - The app displays that child's **Total Expenses**, **Category-wise breakdown**, and **Itemized transaction sheet**.

---

### Journey 5: Exploring the Spreadsheet Tracker (Main Application)
1. **Open Application:** The user taps the ShakeExpense app icon.
2. **Consolidated Overview:** Top banner displays total consolidated expenditure (Today, This Month, All Time).
3. **Spreadsheet Grid:** Clean tabular grid showing Date/Time, Category (with custom names), Transaction Type, and Amount.
4. **Category Grouping:** Switch between tabular chronological rows and grouped category view with category subtotals.

---

### Journey 6: Permissions & Device Setup (Profile Screen)
1. **Navigation:** User navigates to the **Profile** bottom tab.
2. **Setup Card:** User views the **"Permissions & Setup"** card displaying live status indicators:
   - **Shake Anywhere:** ✅ Enabled or ⚠️ Required ("Open Overlay Settings").
   - **Bank Notification Detection:** ✅ Enabled or ⚠️ Required ("Enable Notification Access").
3. **Granting Access:**
   - User taps the action button to open the appropriate Android Settings page (`ACTION_MANAGE_OVERLAY_PERMISSION` or `ACTION_NOTIFICATION_LISTENER_SETTINGS`).
   - User toggles the permission in Android Settings and returns to ShakeExpense.
4. **Auto-Refresh:** On resume, `ProfileScreen` immediately detects the granted permission and updates the badge to ✅ **Enabled**.
5. **Privacy Assurance:** Card displays explicit guarantee that notification parsing is 100% on-device and raw text is never transmitted to the cloud.

---

## 3. Interaction State Machines

### 3.1. Shake Overlay State Machine
```
              ┌────────────────────────┐
              │   IDLE (Home Screen)   │
              └───────────┬────────────┘
                          │ Shake Detected (Screen On)
                          ▼
              ┌────────────────────────┐
    ┌─────────┤   OVERLAY_DISPLAYED    ├─────────┐
    │ Back /  └───────────┬────────────┘         │ Tap Backdrop
    │ Dismiss             │ Select Category      │
    ▼                     ▼                      ▼
[DISMISSED]   ┌────────────────────────┐   [DISMISSED]
              │      CATEGORY_SET      │
              └───────────┬────────────┘
                          ├──────────────────────────────┐
             [Standard]   │                              │ ["Others"]
                          ▼                              ▼
              ┌────────────────────────┐     ┌────────────────────────┐
              │      AMOUNT_ENTRY      │     │  CUSTOM_CATEGORY_INPUT │
              └───────────┬────────────┘     └───────────┬────────────┘
                          │ Type Amount                  │ Type Custom Name
                          │                              ▼
                          │                  ┌────────────────────────┐
                          │                  │      AMOUNT_ENTRY      │
                          │                  └───────────┬────────────┘
                          │                              │ Type Amount
                          ▼                              ▼
              ┌───────────────────────────────────────────────────────┐
              │                      READY_TO_SAVE                    │
              └──────────────────────────┬────────────────────────────┘
                                         │ Tap Save
                                         ▼
              ┌───────────────────────────────────────────────────────┐
              │                     SAVE_AND_EXIT                     │
              └───────────┬──────────────┬────────────┘
                          │              │
                          ▼              ▼
                    [DISMISSED]    [Haptic Feedback]
```

### 3.2. Bank Notification Prompt State Machine
```
              ┌───────────────────────────────────┐
              │      BANK NOTIFICATION RECEIVED   │
              └─────────────────┬─────────────────┘
                                │ Match Filter & Parse Regex
                                ▼
              ┌───────────────────────────────────┐
              │      HEADS-UP PROMPT SHOWN        │
              │  "Debit of ₹350. What is it for?" │
              └────────┬──────────────────┬───────┘
                       │ Tap Category     │ Dismiss / Timeout
                       ▼                  ▼
              ┌─────────────────┐   ┌───────────────────────┐
              │ CATEGORY CHOSEN │   │ PENDING REVIEW QUEUE  │
              └────────┬────────┘   │ (Saved as Unassigned) │
                       │ Save       └───────────────────────┘
                       ▼
              ┌─────────────────┐
              │ WRITTEN TO ROOM │
              │ (AUTO-SYNCED)   │
              └─────────────────┘
```

---

### 3.4 Reset Expenses User Flow

```
┌─────────────────┐
│  PROFILE SCREEN │
└────────┬────────┘
         │ Tap "Reset Expenses..."
         ▼
┌───────────────────────────────────────┐
│     RESET PERIOD SELECTION MODAL      │
│  [ Today ]  [ This Week ]  [ Date ]   │
└────────┬──────────────────────┬───────┘
         │ Today / This Week    │ Specific Date
         │                      ▼
         │             ┌─────────────────┐
         │             │   DATE PICKER   │
         │             └────────┬────────┘
         │                      │ Confirm Date
         ▼                      ▼
┌───────────────────────────────────────┐
│    PRE-DELETION CONFIRMATION MODAL    │
│  - Period: Selected Period Label      │
│  - Total Items: X expenses            │
│  - Total Amount: ₹Y                   │
│  - Warning: Cannot be undone          │
│  [ Reset Expenses ]     [ Cancel ]    │
└────────┬──────────────────────┬───────┘
         │ Confirm Reset        │ Dismiss / Cancel
         ▼                      ▼
┌─────────────────────────┐  ┌─────────────┐
│ 1. Delete Local (Room)  │  │ NO CHANGES  │
│ 2. Propagate to Cloud   │  └─────────────┘
│ 3. Record Tombstone     │
│ 4. Refresh UI Totals    │
└─────────────────────────┘
```

### 3.5 Child Exit Request & Parent Approval Flow

```
┌───────────────────────────────────────┐
│     CHILD MEMBER (FAMILY HUB)         │
│ Tap Member Profile -> "Request Exit"  │
└──────────────────┬────────────────────┘
                   │ Confirms Request
                   ▼
┌───────────────────────────────────────┐
│ Room DB: is_exit_requested = true     │
│ Cloud Firestore synced with flag      │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│      FAMILY HUB (PARENT VIEW)         │
│ Member Card: ⚠️ "Exit Requested" Badge│
│ Parent opens Member Details           │
└──────────┬─────────────────┬──────────┘
           │                 │
           │ Approve & Detach│ Dismiss Request
           ▼                 ▼
┌───────────────────────┐  ┌───────────────────────┐
│ Detaches Child Device │  │ Clears Exit Flag      │
│ Deletes Family Link   │  │ Keeps Child Linked    │
└───────────────────────┘  └───────────────────────┘
```

### 3.6 Shared Family Budgets Management Flow

```
┌───────────────────────────────────────┐
│        FAMILY HUB: SHARED BUDGETS     │
│ Tap "+ Add Budget"                    │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│ ADD / EDIT SHARED BUDGET DIALOG       │
│ - Select Category (Food, Bills, etc.) │
│ - Enter Monthly Limit (₹)             │
│ Tap "Save Budget"                     │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│ Persisted in Room `family_budgets`    │
│ Aggregated Real-Time Progress Bar:    │
│ Family Spent vs Category Limit        │
└───────────────────────────────────────┘
```

### 3.7 Subscription Upgrade & Payment Gateway Flow

```
┌───────────────────────────────────────┐
│ PROFILE / PAYWALL DIALOG              │
│ Select Tier: PLUS (₹59) / FAMILY PRO  │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│ MEMBERSHIP PAYMENT MODAL              │
│ - Choose Cadence: Monthly / Yearly    │
│ - Total Payable Display               │
│ Tap "Proceed to Pay (UPI / Gateway)"  │
└──────────┬────────────────────────────┘
           │
           ├──────────────────────────────┐
           ▼ (UPI App Installed)          ▼ (No UPI App)
┌──────────────────────┐        ┌─────────────────────────┐
│ Launches UPI Chooser │        │ Redirects to Google     │
│ (GPay/PhonePe/Paytm) │        │ Play Subscriptions Web  │
└──────────┬───────────┘        └─────────────────────────┘
           │ Payment Completed / Verified
           ▼
┌───────────────────────────────────────┐
│ Room `financial_profile.tier` updated │
│ `EntitlementManager` broadcasts plan  │
│ All premium features unlocked live    │
└───────────────────────────────────────┘
```

---

## 4. Edge Cases & Interaction Handling

| Scenario | UX Handling Strategy |
| :--- | :--- |
| **Accidental Shake Dismissal** | Tapping transparent background or pressing Back dismisses overlay without saving data. |
| **Empty Amount Attempt** | Save action remains disabled until amount $> 0$. |
| **"Others" Selected with Empty Custom Name** | Defaults cleanly to category name "Others". |
| **Rapid Shake Repeated** | Debounce window ignores consecutive physical shakes within a short time window. |
| **Unrecognized Bank Notification Format** | If parser cannot extract amount or type reliably, notification is ignored to prevent corrupt entries. |
| **No Internet During Family Logging** | All logs stored locally with `sync_status = PENDING`. `WorkManager` retries automatically with exponential backoff once online. |
| **Duplicate Notification (SMS + App Push)** | Deduplication window (based on timestamp within 60s + exact amount + reference) ignores redundant alerts. |
| **Phone Screen Turned Off** | Shake listening is disabled while screen is off to prevent pocket triggers. |
| **Reset with 0 Expenses in Range** | Confirmation dialog shows "0 items found", deletion completes cleanly with 0 deleted. |
| **Sync Resurrecting Deleted Expenses** | Deletions are tracked via persistent tombstones in `SyncEngine` and Firestore queries filter out deleted records. |
| **Child Attempts Unilateral Exit** | Child cannot leave unilaterally; request is stored in SQLite Room and marked with an amber badge until Parent approves. |
| **Free User Accesses Premium Feature** | Triggers clear Paywall Modal highlighting tier benefits; prevents access with zero feature leakage. |
| **No Monthly Income Configured** | Safe-to-Spend and AI Assistant display ₹0 runway and prompt the user to configure income instead of inventing fake estimates. |
| **Threshold Crossed Rapidly** | Anti-spam engine triggers push notifications once per threshold per calendar month to prevent notification floods. |

---

## 5. Cross References
- System Health: [Health.md](Health.md)
- Product goals and requirements: [PRD.md](PRD.md)
- UI and visual specifications: [Design.md](Design.md)
- Technical architecture: [TechSpec.md](TechSpec.md)
- Persistence schema: [schema.md](schema.md)
- Development principles: [Rules.md](Rules.md)
- Implementation status: [Tracker.md](Tracker.md)
- Consistency ledger: [Uptodate.md](Uptodate.md)
- Engineering architecture: [TechSpec.md](TechSpec.md)
- Persistence schema: [schema.md](schema.md)
- Development principles: [Rules.md](Rules.md)
- Project roadmap and progress: [Tracker.md](Tracker.md)
- Consistency ledger: [Uptodate.md](Uptodate.md)
