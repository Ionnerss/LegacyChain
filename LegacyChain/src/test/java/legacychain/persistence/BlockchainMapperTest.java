package legacychain.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import legacychain.core.Blockchain;
import legacychain.core.Block;
import legacychain.core.Transaction;
import legacychain.persistence.snapshot.BlockchainSnapshot;
import legacychain.wallet.Wallet;

public class BlockchainMapperTest {
    @Test 
    void testBlockchainMapsToSnapshot() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();
        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(new Wallet().getPublicKey(), 15, 0));

        BlockchainMapper bm = new BlockchainMapper();
        BlockchainSnapshot bs = bm.toSnapshot(k);
        BlockMapper blockMapper = new BlockMapper();

        assertEquals(k.getDifficulty(), bs.difficulty());
        assertEquals(k.size(), bs.chain().size());
        for (int i = 0; i < k.size(); i++) {
            assertEquals(k.getBlock(i).getHeight(), bs.chain().get(i).height());

            Block b = blockMapper.fromSnapshot(bs.chain().get(i));
            assertEquals(k.getBlock(i).getHash(), b.getHash());
        }
        Block gen = blockMapper.fromSnapshot(bs.chain().getFirst());
        assertEquals(k.getBlock(0).getHeight(), gen.getHeight());
        assertEquals("0", bs.chain().getFirst().previousHash());
        assertEquals(new ArrayList<Transaction>(), bs.chain().getFirst().transactions());
        assertEquals(k.getBlock(0).getHash(), gen.getHash());
        assertEquals(k.getPendingTransactions().size(), bs.pendingTransactions().size());

        TransactionMapper tm = new TransactionMapper();
        Transaction pending = tm.fromSnapshot(bs.pendingTransactions().getFirst());
        assertEquals(k.getPendingTransactions().getFirst().getTransactionId(), pending.getTransactionId());
    }

    @Test 
    void testBlockchainRoundTripPreservesState() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot bcSnapshot = blockchainMapper.toSnapshot(k);
        Blockchain snapBlockchain = blockchainMapper.fromSnapshot(bcSnapshot);

        assertEquals(k.getDifficulty(), snapBlockchain.getDifficulty());
        assertEquals(k.size(), snapBlockchain.size());
        for (int i = 0; i < k.size(); i++) {
            assertEquals(k.getBlock(i).getHash(), snapBlockchain.getBlock(i).getHash());
        }
        assertEquals(k.getPendingTransactions().size(), snapBlockchain.getPendingTransactions().size());
        for (int i = 0; i < k.getPendingTransactions().size(); i++) {
            assertEquals(
                k.getPendingTransactions().get(i).getTransactionId(),
                snapBlockchain.getPendingTransactions().get(i).getTransactionId()
            );
        }
        assertEquals(k.getNextNonce(w.getPublicKey()), snapBlockchain.getNextNonce(w.getPublicKey()));
        assertEquals(k.getBalance(w.getPublicKey()), snapBlockchain.getBalance(w.getPublicKey()));
        assertEquals(k.getNextNonce(j.getPublicKey()), snapBlockchain.getNextNonce(j.getPublicKey()));
        assertEquals(k.getBalance(j.getPublicKey()), snapBlockchain.getBalance(j.getPublicKey()));
        assertTrue(snapBlockchain.isValid());
    }

    @Test 
    void testRestoredBlockchainCanMinePendingTransactions() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet(), s = new Wallet();
        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot bcSnapshot = blockchainMapper.toSnapshot(k);
        Blockchain snapBlockchain = blockchainMapper.fromSnapshot(bcSnapshot);

        assertDoesNotThrow(() -> snapBlockchain.minePendingTransactions(s.getPublicKey()));
        assertEquals(0, snapBlockchain.getPendingTransactions().size());
        assertEquals(k.size() + 1, snapBlockchain.size());
        assertEquals(
            k.getPendingTransactions().getLast().getTransactionId(), 
            snapBlockchain.getLatestBlock().getTransactions().getFirst().getTransactionId()
        );
        assertEquals(2, snapBlockchain.getNextNonce(w.getPublicKey()));
        assertTrue(snapBlockchain.isValid());
    }
}
