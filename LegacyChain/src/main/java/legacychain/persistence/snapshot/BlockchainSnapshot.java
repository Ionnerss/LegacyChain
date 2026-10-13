package legacychain.persistence.snapshot;

import java.util.List;

public record BlockchainSnapshot(
    int difficulty,
    List<BlockSnapshot> chain,
    List<TransactionSnapshot> pendingTransactions
) {
    public BlockchainSnapshot {
        if (chain == null) throw new IllegalArgumentException("Invalid chain.");
        for (BlockSnapshot blockSnap : chain) {
            if (blockSnap == null) throw new IllegalArgumentException("Invalid chain.");
        }
        if (pendingTransactions == null) throw new IllegalArgumentException("Invalid mempool.");
        for (TransactionSnapshot ts : pendingTransactions) {
            if (ts == null) throw new IllegalArgumentException("Invalid mempool.");
        }
        chain = List.copyOf(chain);
        pendingTransactions = List.copyOf(pendingTransactions);
    }
}
