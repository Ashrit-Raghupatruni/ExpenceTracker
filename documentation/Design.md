# UI/UX Design Specification (Design.md)

**Project Name:** ShakeExpense  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. Design Philosophy: "Ephemeral HUD & Spreadsheet Density"

ShakeExpense's visual language is built around three core interaction models:

1. **The Ephemeral Translucent HUD (Quick Entry & Bank Prompt):**  
   When triggered by a shake or bank notification, the UI feels like a lightweight, non-disruptive layer floating directly above the user's current screen with ~90% background transparency.
2. **The High-Density Spreadsheet (Tracker View):**  
   Presents transactions in a structured, tabular format where numbers, categories, transaction types, and totals are immediately legible and organized.
3. **Family Member Insight Hub (Parental Control):**  
   Provides a clean family overview where tapping any child or member reveals their cumulative expenditure and itemized category breakdown without clutter.

---

## 2. Visual Palette & Hierarchy

### 2.1. Color System

| Token | Light Theme Appearance | Dark Theme Appearance | Usage |
| :--- | :--- | :--- | :--- |
| `overlay.backdrop` | ~90% transparent tint | ~90% transparent dark tint | Translucent veil over the home screen |
| `overlay.card.surface` | High-contrast frosted white | High-contrast frosted charcoal | Input panel containing chips and numpad |
| `tracker.background` | Clean soft white / gray | Deep dark gray / black | Main tracker background |
| `tracker.row.even` | Pure white | Subtle dark gray | Spreadsheet alternating row |
| `tracker.row.odd` | Muted off-white | Contrast dark gray | Spreadsheet alternating row |
| `brand.primary` | High-visibility Blue | Crisp Blue | Active category selection, save action |
| `type.debit` | Rich Crimson / Rose (`#E11D48`) | Soft Coral (`#FB7185`) | Debited transaction badge / text |
| `type.credit` | Forest Green (`#059669`) | Emerald (`#34D399`) | Credited transaction badge / text |
| `sync.pending` | Muted Amber (`#D97706`) | Soft Amber (`#FBBF24`) | Offline sync pending icon |
| `sync.synced` | Subtle Slate (`#94A3B8`) | Dark Slate (`#475569`) | Synced status indicator |
| `brand.amount` | High-contrast Dark Slate | Bright Off-White | Primary numerical currency display |
| `border.subtle` | Light gray divider | Dark gray divider | 1px tabular grid lines |

### 2.2. Category Color Accents
- **Food:** Warm Amber / Orange
- **Transport:** Clean Blue
- **Groceries:** Emerald Green
- **Bills:** Deep Purple
- **Shopping:** Rose / Pink
- **Entertainment:** Violet
- **Others:** Slate Gray

### 2.3. Typographic Hierarchy
- **Primary Numerical Font:** Tabular figures (equal-width digits for vertical decimal alignment in tabular columns).
- **Hero Amount Display:** Large, prominent font size (emphasizing the entered amount clearly).
- **Spreadsheet Rows:** Compact, medium-weight font for dates, category badges, and figures.
- **Category Labels:** Clear, bold uppercase or title-case text on chips.

---

## 3. UI Component Layouts

### 3.1. Translucent Quick-Entry Overlay Layout
```
+-------------------------------------------------------------+
|  [~90% Transparent Background - Home Screen Visible]        |
|                                                             |
|   +-----------------------------------------------------+   |
|   |  [High-Contrast Frosted Input Panel]                |   |
|   |                                                     |   |
|   |  [Food]  [Transport]  [Groceries]  [Bills] [Others] |   |
|   |                                                     |   |
|   |  [ If "Others": "Enter custom category name..."   ] |   |
|   |                                                     |   |
|   |                     ₹ 250                           |   |
|   |                                                     |   |
|   |  +---------+---------+---------+                    |   |
|   |  |    1    |    2    |    3    |                    |   |
|   |  +---------+---------+---------+                    |   |
|   |  |    4    |    5    |    6    |                    |   |
|   |  +---------+---------+---------+                    |   |
|   |  |    7    |    8    |    9    |                    |   |
|   |  +---------+---------+---------+                    |   |
|   |  |    .    |    0    |    ⌫    |                    |   |
|   |  +---------+---------+---------+                    |   |
|   |                                                     |   |
|   |  [                  SAVE (✓)                    ]   |   |
|   +-----------------------------------------------------+   |
|                                                             |
+-------------------------------------------------------------+
```

### 3.2. Bank Notification Quick-Categorization Prompt Layout
```
+-------------------------------------------------------------+
|  [Translucent Heads-Up Notification Overlay / Dialog]       |
|                                                             |
|   +-----------------------------------------------------+   |
|   | 🏦 Bank Alert Detected • [ DEBIT ]                  |   |
|   | ₹ 350.00  (Starbucks Coffee)                        |   |
|   | "What is this expense for?"                         |   |
|   |                                                     |   |
|   | [Food]  [Transport]  [Groceries]  [Shopping] [Other] |   |
|   |                                                     |   |
|   | [ Dismiss ]                  [ Confirm & Add (✓) ]  |   |
|   +-----------------------------------------------------+   |
+-------------------------------------------------------------+
```

### 3.3. Spreadsheet Tracker View Layout (Main App)
```
+-------------------------------------------------------------+
| ShakeExpense Tracker               [ Sync: Synced ● ]       |
+-------------------------------------------------------------+
| CONSOLIDATED TOTALS                                         |
| Total Spent: ₹ 14,850                                       |
| Today: ₹ 450   |   This Month: ₹ 14,850                     |
+-------------------------------------------------------------+
| [ View: All Entries (Spreadsheet) | Grouped by Category ]   |
+-------------------+--------------------+--------+-----------+
| DATE / TIME       | CATEGORY           | TYPE   | AMOUNT    |
+-------------------+--------------------+--------+-----------+
| 24 Aug, 14:15     | [Food] (Starbucks) | DEBIT  |  ₹ 350.00 |
| 23 Aug, 18:30     | [Food]             | DEBIT  |  ₹ 150.00 |
| 23 Aug, 14:15     | [Groceries]        | DEBIT  |  ₹ 850.00 |
| 22 Aug, 20:00     | [Salary / Bonus]   | CREDIT | ₹50,000.0 |
| 22 Aug, 09:30     | [Transport]        | DEBIT  |  ₹ 220.00 |
+-------------------+--------------------+--------+-----------+
```

### 3.4. Family / Parental Control Hub & Member Detail View Layout
```
+-------------------------------------------------------------+
| Family Expense Hub                 [ + Invite Member ]      |
+-------------------------------------------------------------+
| FAMILY TOTAL THIS MONTH: ₹ 38,400                           |
| 3 Linked Members • Last Synced: 2 mins ago                  |
+-------------------------------------------------------------+
| MEMBERS                                                     |
|                                                             |
| ┌─────────────────────────────────────────────────────────┐ |
| │ 👤 Rahul (Child)                         Total: ₹ 4,200 │ |
| │ Top Category: Food (₹2,100) • Last active: Today 14:10  │ |
| └────────────────────────────┬────────────────────────────┘ |
|                              │ (Tapped)                     |
|                              ▼                              |
| ┌─────────────────────────────────────────────────────────┐ |
| │ RAHUL'S EXPENSES BREAKDOWN                              │ |
| │ Total Spent: ₹ 4,200 (This Month)                       │ |
| │ - Food:        ₹ 2,100 (50%)                            │ |
| │ - Transit:     ₹ 1,100 (26%)                            │ |
| │ - Books:       ₹ 1,000 (24%)                            │ |
| │ ------------------------------------------------------- │ |
| │ 24 Aug 14:10 | [Food] (Cafeteria)       | ₹ 120.00      │ |
| │ 24 Aug 08:30 | [Transit] (Metro Pass)   | ₹  50.00      │ |
| │ 23 Aug 17:00 | [Books] (Math Textbook)  | ₹ 850.00      │ |
| └─────────────────────────────────────────────────────────┘ |
|                                                             |
| ┌─────────────────────────────────────────────────────────┐ |
| │ 👤 Ananya (Child)                        Total: ₹ 2,850 │ |
| │ Top Category: Groceries • Last active: Yesterday        │ |
| └─────────────────────────────────────────────────────────┘ |
+-------------------------------------------------------------+
### 3.4. Profile Screen: Permissions & Setup Card Design

```
+-------------------------------------------------------------+
| 🛡️ Permissions & Setup                                      |
| Configure device permissions for instant HUD & smart parsing|
|-------------------------------------------------------------|
| 📳 Shake Anywhere                               [ Enabled ] |
| Required to trigger Quick Entry from Home Screen/apps.     |
| [ Manage Setting ]                                          |
|-------------------------------------------------------------|
| 🔔 Bank Notification Detection                  [ Required ]|
| Reads supported bank notifications to detect transactions   |
| and prompt for 1-tap confirmation.                          |
| [ Enable Notification Access ]                              |
|-------------------------------------------------------------|
| 🔒 Privacy Guarantee: All parsing is 100% on-device. Raw    |
| notification text is NEVER uploaded to cloud servers.       |
+-------------------------------------------------------------+
```

---

## 4. Interaction & Tactile Feedback

1. **Shake Trigger:** Distinct physical haptic pulse acknowledging shake recognition.
2. **Category Tap:** Light tactile tick upon selecting a category chip.
3. **Notification Prompt Tap:** Instant confirmation click when categorizing a bank alert.
4. **Member Card Tap:** Fluid slide transition into the member's detailed spending dashboard.
5. **Save Confirmation:** Positive haptic feedback confirming transaction storage.

---

## 5. Accessibility & Readability Considerations

- **Contrast on Transparent Backgrounds:** Because the overlay sits on arbitrary user wallpapers (which may be bright, dark, or busy), all interactive controls and text are contained within a dedicated frosted panel with high opacity to guarantee legibility.
- **Touch Target Sizes:** Keypad buttons and category chips maintain generous touch areas to prevent miss-taps during rapid entry.

---

## 6. Cross References
- Requirements and scope: [PRD.md](PRD.md)
- User journeys and state transitions: [Appflow.md](Appflow.md)
- Engineering architecture: [TechSpec.md](TechSpec.md)
- Storage schema: [schema.md](schema.md)
- Vibe coding rules: [Rules.md](Rules.md)
- Implementation roadmap: [Tracker.md](Tracker.md)
- Documentation audit: [Uptodate.md](Uptodate.md)
