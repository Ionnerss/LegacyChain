package legacychain.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import legacychain.core.Block;
import legacychain.core.Blockchain;
import legacychain.core.Transaction;
import legacychain.persistence.snapshot.BlockSnapshot;
import legacychain.persistence.snapshot.TransactionSnapshot;
import legacychain.wallet.Wallet;

public class BlockMapperTest {
    @Test 
    void testBlockMapsToSnapshot() {
        Blockchain k = new Blockchain(2);
        k.minePendingTransactions(new Wallet().getPublicKey());

        Block b = k.getLatestBlock();
        BlockMapper bm = new BlockMapper();
        BlockSnapshot bs = bm.toSnapshot(b);

        assertEquals(b.getPreviousHash(), bs.previousHash());
        assertEquals(b.getTimeStamp(), bs.timeStamp());
        assertEquals(b.getNonce(), bs.nonce());
        assertEquals(b.getHeight(), bs.height());
        assertEquals(b.getTransactions().size(), bs.transactions().size());

        //For transaction snapshot side of saving the block
        Transaction t = b.getTransactions().getLast();
        TransactionSnapshot ts = bs.transactions().getLast();
        assertEquals(t.getType(), ts.type());
        assertArrayEquals(t.getRecipient().getEncoded(), Base64.getDecoder().decode(ts.recipient()));
        assertEquals(t.getAmount(), ts.amount());
        assertEquals(t.getRewardHeight(), ts.rewardHeight());
    }

    @Test 
    void testBlockRoundTrip() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.minePendingTransactions(w.getPublicKey());
        k.minePendingTransactions(j.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 10, 0));
        k.submitTransaction(j.createTransaction(new Wallet().getPublicKey(), 25, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());

        Block b = k.getLatestBlock();
        BlockMapper bm = new BlockMapper();
        BlockSnapshot bs = bm.toSnapshot(b);
        Block snapBlock = bm.fromSnapshot(bs);

        assertEquals(b.getPreviousHash(), snapBlock.getPreviousHash());
        assertEquals(b.getTimeStamp(), snapBlock.getTimeStamp());
        assertEquals(b.getNonce(), snapBlock.getNonce());
        assertEquals(b.getHeight(), snapBlock.getHeight());
        assertEquals(b.getTransactions().size(), snapBlock.getTransactions().size());
        
        for (int i = 0; i < b.getTransactions().size(); i++) {
            assertEquals(b.getTransactions().get(i).getTransactionId(), snapBlock.getTransactions().get(i).getTransactionId());
        }
        assertEquals(b.getMerkleRoot(), snapBlock.getMerkleRoot());
        assertEquals(b.getHash(), snapBlock.getHash());
    }
}
