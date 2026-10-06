package legacychain.persistence;

import java.util.List;
import java.util.ArrayList;
import legacychain.core.Block;
import legacychain.core.Blockchain;
import legacychain.core.Transaction;
import legacychain.persistence.snapshot.*;

public class BlockchainMapper {
    public BlockchainSnapshot toSnapshot(Blockchain blockchain) {
        if (blockchain == null) throw new IllegalArgumentException("Invalid blockchain");

        List<BlockSnapshot> chain = new ArrayList<>();
        BlockMapper bm = new BlockMapper();
        for (int i = 0; i < blockchain.size(); i++) {
            chain.add(bm.toSnapshot(blockchain.getBlock(i)));
        }

        List<TransactionSnapshot> ts = new ArrayList<>();
        TransactionMapper tm = new TransactionMapper();
        List<Transaction> pending = List.copyOf(blockchain.getPendingTransactions());
        for (int i = 0; i < pending.size(); i++) {
            ts.add(tm.toSnapshot(pending.get(i)));
        }

        return new BlockchainSnapshot(blockchain.getDifficulty(), chain, ts);
    }

    public Blockchain fromSnapshot(BlockchainSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");

        List<Block> chain = new ArrayList<>();
        BlockMapper blockMapper = new BlockMapper();
        List<Transaction> pendingTransactions = new ArrayList<>();
        TransactionMapper transactionMapper = new TransactionMapper();

        if (snapshot.chain() == null) throw new IllegalArgumentException("Invalid snapshot.");
        for (BlockSnapshot bs : snapshot.chain()) {
            chain.add(blockMapper.fromSnapshot(bs));
        }

        for (TransactionSnapshot ts : snapshot.pendingTransactions()) {
            pendingTransactions.add(transactionMapper.fromSnapshot(ts));
        }

        return Blockchain.restoreBlockchain(snapshot.difficulty(), chain, pendingTransactions);
    }
}
