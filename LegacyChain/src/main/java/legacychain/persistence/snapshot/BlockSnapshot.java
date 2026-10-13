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
        if (transactions == null) throw new IllegalArgumentException("Invalid transactions.");
        for (TransactionSnapshot ts : transactions) {
            if (ts == null) throw new IllegalArgumentException("Invalid transactions.");
        }
        transactions = List.copyOf(transactions);
    }
}
