# ADR 0012 — Create Vault Uses Ephemeral Recovery Acknowledgment Before Explicit Creation

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA must clearly tell users during new-vault creation that the vault cannot be recovered or decrypted if the Master Password is forgotten. The owner requires an intentional swipe acknowledgment before the final Create Vault action becomes available.

The acknowledgment is a UX safety gate, not a recovery mechanism, not a cryptographic state, and not a reason to persist additional sensitive workflow state.

## Decision

1. Create Vault keeps the existing Master Password and confirmation inputs, then presents a clear unrecoverability warning before final creation.
2. The warning requires a swipe-to-acknowledge interaction using an existing Material 3/platform swipe primitive or established project pattern. RAHSA does not add a custom gesture framework or dependency for this requirement.
3. Swipe completion only sets an ephemeral acknowledgment state for the current flow. It never creates the vault directly.
4. The final `Create Vault` action remains a separate explicit action and is enabled only when the existing password validation passes and the recovery warning has been acknowledged.
5. Android accessibility/TalkBack must expose an equivalent intentional semantic action so the acknowledgment does not depend exclusively on a physical swipe gesture.
6. The acknowledgment is not persisted. Back/navigation away, abandoned Home/background flow, recreation that does not preserve the screen state, process death, or force-close returns the flow to `acknowledged = false` when reopened.
7. Password draft fields do not need to be persisted merely to preserve this acknowledgment flow.
8. Before the explicit Create Vault action starts, abandoning the flow performs no authoritative vault mutation.
9. After Create Vault starts, duplicate execution is prevented and Back/Home/navigation is not treated as a second create trigger or a reason to invent a new transaction system.
10. T127 reuses the existing reviewed vault-creation/safe-write behavior. It does not redesign vault persistence.
11. No `CREATE_VAULT_IN_PROGRESS` or other new recovery marker is introduced for this UX requirement.

## Consequences

### Positive

- Users must explicitly acknowledge the no-recovery consequence before vault creation.
- A separate Create action prevents the swipe itself from performing a security-sensitive operation.
- Ephemeral state avoids unnecessary persistence of acknowledgment or password drafts.
- Accessibility users receive an equivalent intentional action.
- Existing Material/platform behavior is reused instead of introducing another gesture dependency.

### Costs / limitations

- The user must acknowledge again after leaving/restarting the flow.
- Compose/accessibility tests are needed to ensure the semantic action matches the intended acknowledgment behavior.
- The exact existing Material 3 primitive must be verified against the project dependency version during the JIT implementation kit; no dependency upgrade is implied by this ADR.

## Rejected alternatives

- Passive checkbox acknowledgment — does not match the owner-approved swipe requirement.
- Let the swipe immediately create the vault — conflates acknowledgment with the final security-sensitive action.
- Persist acknowledgment across process death — unnecessary state persistence for a one-time creation gate.
- Persist Master Password drafts solely to resume the flow — unnecessary secret-persistence risk.
- Add a third-party swipe component or bespoke gesture engine — unnecessary unless the existing approved platform/project primitive proves technically unsuitable during implementation review.
- Add a new recovery/transaction marker specifically for T127 — unnecessary because this task does not replace an existing authoritative vault and should reuse existing safe creation semantics.
