package legacychain.persistence.snapshot;

import java.util.List;

public record BlockchainSnapshot(
    int difficulty,
    List<BlockSnapshot> chain,
    List<TransactionSnapshot> pendingTransactions
) {
    public BlockchainSnapshot {
        if (chain == null || chain.contains(null)) throw new IllegalArgumentException("Invalid chain.");
        if (pendingTransactions == null || pendingTransactions.contains(null))
            throw new IllegalArgumentException("Invalid mempool.");
        chain = List.copyOf(chain);
        pendingTransactions = List.copyOf(pendingTransactions);
    }
}
