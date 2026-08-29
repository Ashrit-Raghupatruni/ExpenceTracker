# Development Guidelines & Vibe Coding Governance (Rules.md)

**Project Name:** ShakeExpense  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. Vibe Coding Principles for ShakeExpense

"Vibe coding" on ShakeExpense means rapid, iterative development powered by AI while maintaining a reliable **Living Single Source of Truth** through synchronized documentation.

### Core Principles:
1. **Speed & Minimal Taps as the North Star:** Core expense entry must prioritize minimal friction and speed. Any proposed change that meaningfully increases interaction time or taps requires explicit justification.
2. **Spreadsheet Density Over Fluff:** The interface prioritizes legible tabular numbers and clean categorization over decorative filler or empty space.
3. **No Unjustified Scope Creep:** Every feature must directly serve the core purpose of effortless daily expense tracking. Features that add unnecessary complexity are rejected.
4. **Offline-First & Privacy Preserving:** All financial data operates seamlessly offline. Bank notifications are parsed strictly on-device without exfiltrating raw message contents. Cloud sync is restricted to family ledger records only when internet connectivity is active.
5. **Living Documentation:** Whenever the product or technical architecture evolves during development, the documentation must be updated first.

---

## 2. Strict Implementation Rules

1. **8 Documentation Files as Single Source of Truth:**
   - Follow `PRD.md`, `TechSpec.md`, `Appflow.md`, `Design.md`, `schema.md`, `Rules.md`, `Tracker.md`, and `Uptodate.md` strictly.
   - Do not invent or remove requirements.
2. **Incremental Execution:**
   - Build strictly one phase at a time in the exact documented order:
     1. Core database
     2. Basic expense entry
     3. Shake $\rightarrow$ translucent overlay $\rightarrow$ category $\rightarrow$ amount $\rightarrow$ save
     4. Expense sheets + category/total dashboard
     5. Bank notification detection
     6. Family/parental sync
   - Do not implement everything at once.
3. **Offline-First Storage:**
   - Expenses must save locally immediately even without internet access.
4. **Bank Notification Safety & Accuracy:**
   - Notifications must be parsed strictly on-device.
   - Never automatically categorize an uncertain bank transaction.
   - Always ask the user to confirm the detected expense and category.
   - Prevent duplicate bank transactions (deduplicate by timestamp, amount, and reference).
5. **Family Sync Architecture:**
   - Family sync must work through an offline queue + `WorkManager`.
   - Respect documented parent/child permissions, role boundaries, and privacy.
6. **Architectural Separation:**
   - Keep UI, business logic, database, notification parsing, and sync logic strictly separated across packages.
7. **Verification & No Premature Claims:**
   - Do not claim a feature is complete until it has been compiled, runtime-tested, and verified.
   - After each phase, run tests and verify actual behavior.
   - Update `Tracker.md` and `Uptodate.md` after verified implementation.
8. **Documentation Conflict Protocol:**
   - If code or requirements conflict with the documentation, **STOP** and report the conflict to the user instead of silently changing requirements.
9. **Permissions & User Consent:**
   - Permissions (Overlay, Notification Access) must never be forced or automatically enabled. The user must be provided clear educational copy, live status badges (✅ Enabled / ⚠️ Required), and buttons to open Android system settings.
   - All notification parsing must occur 100% on-device; raw notification payloads must never leave the device. Only confirmed expenses may be synced to the family ledger.

---

## 3. End-to-End Verification Flows

During testing and phase sign-off, verify against these exact flows:

1. **Shake Flow:**
   $$\text{Shake} \longrightarrow \text{Category} \longrightarrow \text{Amount} \longrightarrow \text{Database} \longrightarrow \text{Sheet} \longrightarrow \text{Total}$$
2. **Bank Notification Flow:**
   $$\text{Bank Notification} \longrightarrow \text{Parse On-Device} \longrightarrow \text{Confirm Prompt} \longrightarrow \text{Category} \longrightarrow \text{Database} \longrightarrow \text{Sheet}$$
3. **Family / Parental Sync Flow:**
   $$\text{Child Expense} \longrightarrow \text{Offline Queue} \longrightarrow \text{Internet Available} \longrightarrow \text{WorkManager Sync} \longrightarrow \text{Parent View} \longrightarrow \text{Family Total}$$

---

## 4. Phase Completion Reporting Format

When completing each implementation phase, report progress in this exact format:

```markdown
### Phase [X]: [Phase Name] Summary
- **Implemented:** [Summary of components built]
- **Tested:** [Unit and instrumentation tests executed]
- **Runtime Verified:** [Observed runtime behavior on hardware/emulator]
- **Remaining Issues:** [Any open blockers, bugs, or notes, or 'None']
```

---

## 5. Cross References
- Product requirements: [PRD.md](PRD.md)
- User flows: [Appflow.md](Appflow.md)
- Design specifications: [Design.md](Design.md)
- Technical specification: [TechSpec.md](TechSpec.md)
- Database schema: [schema.md](schema.md)
- Implementation status: [Tracker.md](Tracker.md)
- Consistency ledger: [Uptodate.md](Uptodate.md)
