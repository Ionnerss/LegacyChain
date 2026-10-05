package legacychain.persistence.snapshot;

import legacychain.core.TransactionType;

public record TransactionSnapshot(
    TransactionType type,
    String sender,
    String recipient,
    long amount,
    String signature,
    Integer transactonNonce,
    Integer rewardHeight
) {}
