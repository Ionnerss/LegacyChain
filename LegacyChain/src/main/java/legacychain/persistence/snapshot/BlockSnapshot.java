package legacychain.persistence.snapshot;

import java.util.List;

public record BlockSnapshot(
    String previousHash,
    List<TransactionSnapshot> transactions,
    long timeStamp,
    long nonce,
    int height
) {
    public BlockSnapshot {
        if (transactions == null || transactions.contains(null)) throw new IllegalArgumentException("Invalid transactions.");
        transactions = List.copyOf(transactions);
    }
}
