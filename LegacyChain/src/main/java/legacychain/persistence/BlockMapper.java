package legacychain.persistence;

import java.util.ArrayList;
import java.util.List;
import legacychain.core.Block;
import legacychain.core.Transaction;
import legacychain.persistence.snapshot.BlockSnapshot;
import legacychain.persistence.snapshot.TransactionSnapshot;

public class BlockMapper {
    public BlockSnapshot toSnapshot(Block block) {
        if (block == null) throw new IllegalArgumentException("Invalid block.");

        List<TransactionSnapshot> transactions = new ArrayList<>();
        TransactionMapper tm = new TransactionMapper();
        for (Transaction transaction : block.getTransactions())
            transactions.add(tm.toSnapshot(transaction));

        return new BlockSnapshot(
            block.getPreviousHash(), 
            transactions, 
            block.getTimeStamp(),
            block.getNonce(), 
            block.getHeight()
        );
    }

    public Block fromSnapshot(BlockSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");

        List<Transaction> transactions = new ArrayList<>();
        TransactionMapper tm = new TransactionMapper();
        for (TransactionSnapshot ts : snapshot.transactions()) {
            transactions.add(tm.fromSnapshot(ts));
        }
        return Block.restoreBlock(
            transactions, 
            snapshot.previousHash(), 
            snapshot.timeStamp(), 
            snapshot.nonce(),
            snapshot.height());
    }
}
