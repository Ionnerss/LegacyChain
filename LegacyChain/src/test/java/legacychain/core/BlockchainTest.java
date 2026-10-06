package legacychain.core;

import org.junit.jupiter.api.Test;
import legacychain.merkle.MerkleUtil;
import legacychain.wallet.Wallet;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class BlockchainTest {
    @Test
    void testBlockchainSize() {
        //Arrange:
        Blockchain k = new Blockchain(2);

        /*
        Assert:
            Expected size == 1
            Actual size = k.size();
        */
        assertEquals(1, k.size());
    }

    @Test
    void testGenesisPrevHash() {
        //Arrange:
        Blockchain k = new Blockchain(2);

        /*
        Assert:
            Expected prev hash == 0
            Actual prev hash = k.get(0).getPreviousHash();
        */
        assertEquals("0", k.getBlock(0).getPreviousHash());
    }

    @Test
    void testInvalidDifficulty() {
        /*
        Assert:
            Expected bounds: 1 <= difficukty <= 64
            Should throw IllegalArgumentException since 0 < 1
        */
       assertThrows(IllegalArgumentException.class, 
            () -> new Blockchain(0));
    }

    @Test
    void testAddBlockIncreaseSize() {
        //Arrange:
        Blockchain k = new Blockchain(2);

        //Act:
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        /*
        Assert:
            Expected size == 2
            Actual size = k.size();
        */
       assertEquals(2, k.size());
    }

    @Test
    void testLinkedBlocksHashes() {
        //Arrange:
        Blockchain k = new Blockchain(2);

        /*
        Act:
            Add one block
            Keep the Block returned by addBlock()
        */
        Block b = k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        /*
        Assert:
            Expected: block1 prev hash = genesis hash
        */
       assertEquals(k.getBlock(0).getHash(), b.getPreviousHash());
    }

    @Test
    void testMultipleBlocksChainIsValid() {
        //Arrange
        Blockchain k = new Blockchain(2);

        // Act: add four blocks and print their hashes for manual verification
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        // Assert: genesis + 4 correctly linked and secure
        assertTrue(k.isValid());
    }

    @Test
    void testProofOfWork() {
        //Arrange:
        Blockchain k = new Blockchain(2);
        String target = "00";

        //Act:
        Block j = k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        //Assert:
        assertTrue(j.getHash().startsWith(target));
        assertTrue(k.getBlock(1).getHash().startsWith(target));                                                                                                                                                                                                                
    }

    @Test
    void testBlockTransactionListImmutability() {
        Blockchain k = new Blockchain(2);

        ArrayList<Transaction> b = new ArrayList<Transaction>();
        Block s = k.addBlock(b, new Wallet().getPublicKey());
        b.add(new Wallet().createTransaction(new Wallet().getPublicKey(), 300, 0));

        assertNotEquals(s.getTransactions(), b);
        assertEquals(1, s.getTransactions().size()); //3 because of addition of the reward transaction for the miner
    }

    @Test 
    void testAcceptedBlock() {
        Blockchain k = new Blockchain(2);

        ArrayList<Transaction> b = new ArrayList<Transaction>();

        assertDoesNotThrow(() -> k.addBlock(b, new Wallet().getPublicKey()));
        assertEquals(2, k.size());
    }

    @Test 
    void testInvalidTransaction() {
        Blockchain k = new Blockchain(2);
        ArrayList<Transaction> b = new ArrayList<Transaction>();

        Wallet w = new Wallet();
        b.add(new Transaction(new Wallet().getPublicKey(), new Wallet().getPublicKey(), 100, w.sign("yolo"), 0));

        assertThrows(IllegalArgumentException.class, () -> k.addBlock(b, new Wallet().getPublicKey()));
    }

    @Test 
    void testTransactionsOneInvalid() {
        Blockchain k = new Blockchain(2);
        ArrayList<Transaction> b = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new Wallet().getPublicKey(), 100, 1),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 150, 1),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 200, 1)
        ));
        b.add(null);
        b.add(new Wallet().createTransaction(new Wallet().getPublicKey(), 15, 1));

        assertThrows(IllegalArgumentException.class,
            () -> k.addBlock(b, new Wallet().getPublicKey()));
        assertEquals(1, k.size());
    }

    @Test 
    void testInvalidOwner() {
        Blockchain k = new Blockchain(2);
        assertThrows(IllegalArgumentException.class, () -> k.getBalance(null));
    }

    @Test 
    void testValidWallet() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();
        assertEquals(0, k.getBalance(w.getPublicKey()));
    }

    @Test
    void testWalletDoesNotAppearInAnyTransactions() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        assertEquals(0, k.getBalance(w.getPublicKey()));
    }

    @Test 
    void testOneMinedBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertEquals(50, k.getBalance(miner.getPublicKey()));
    }

    @Test 
    void testTwoMinedBlocks() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertEquals(100, k.getBalance(miner.getPublicKey()));
    }

    @Test
    void testRandomWalletBalance() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), random = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertEquals(0, k.getBalance(random.getPublicKey()));
    }

    @Test
    void testMinerCanSpendRewardInNextBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertDoesNotThrow(() -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)20, 0))), new Wallet().getPublicKey());
        });

        assertEquals(TransactionType.REWARD, k.getBlock(1).getTransactions().get(0).getType());
        assertEquals(TransactionType.NORMAL, k.getBlock(2).getTransactions().get(0).getType());
        assertEquals(20, k.getBalance(w.getPublicKey()));
        assertEquals(30, k.getBalance(miner.getPublicKey()));
    }

    @Test
    void testRejectsInsufficientBalance() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)51, 0))), new Wallet().getPublicKey());
        });
    }

    @Test 
    void testZeroBalanceCannotSpend() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                w.createTransaction(miner.getPublicKey(), (long)1, 0))), new Wallet().getPublicKey());
        });
    }

    @Test
    void testRejectsCombinedOverspendingInSameBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 0),
                miner.createTransaction(w.getPublicKey(), 30, 1)
            )), new Wallet().getPublicKey());
        });
    }

    @Test
    void testAllowsExactBalanceSpendingInSameBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertDoesNotThrow(() -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 0),
                miner.createTransaction(w.getPublicKey(), (long)20, 1)
            )), new Wallet().getPublicKey());
        });
    }

    @Test 
    void testRecipientCanSpendReceivedFundsInSameBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertDoesNotThrow(() -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 0),
                w.createTransaction(j.getPublicKey(), 20, 0)
            )), new Wallet().getPublicKey());
        });

        assertAll("Wallet balances",
            () -> assertEquals(20, k.getBalance(miner.getPublicKey())),
            () -> assertEquals(10, k.getBalance(w.getPublicKey())),
            () -> assertEquals(20, k.getBalance(j.getPublicKey()))
        );
    }

    @Test 
    void testRejectedOverspendingBlockDoesNotChangeChainSize() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 0),
                miner.createTransaction(w.getPublicKey(), 30, 1)
            )), new Wallet().getPublicKey());
        });

        assertEquals(2, k.size());
    }

    @Test 
    void testValidChainWithBalances() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), a = new Wallet(), b = new Wallet(), c = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(a.createTransaction(b.getPublicKey(), 20, 0))), miner.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(b.createTransaction(c.getPublicKey(), 10, 0))), miner.getPublicKey());

        assertAll("Wallet balances",
            () -> assertEquals(30, k.getBalance(a.getPublicKey())),
            () -> assertEquals(10, k.getBalance(b.getPublicKey())),
            () -> assertEquals(10, k.getBalance(c.getPublicKey())),
            () -> assertEquals(100, k.getBalance(miner.getPublicKey()))
        );
    }

    @Test
    void testFirstTransactionNonceAccepted() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), r = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());
        assertDoesNotThrow(() -> {
            k.addBlock(new ArrayList<Transaction>(
                List.of(miner.createTransaction(r.getPublicKey(), 10, 0))
            ), new Wallet().getPublicKey());
        });

        assertEquals(1, k.getNextNonce(miner.getPublicKey()));
    }

    @Test 
    void testSequentialNoncesInSameBlockAccepted() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), r = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());
        assertDoesNotThrow(() -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(r.getPublicKey(), 10, 0),
                miner.createTransaction(r.getPublicKey(), 20, 1))
            ), new Wallet().getPublicKey());
        });

        assertEquals(2, k.getNextNonce(miner.getPublicKey()));
    }

    @Test
    void testDuplicateNonceInSameBlockRejected() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), r = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());
        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(r.getPublicKey(), 10, 0),
                miner.createTransaction(r.getPublicKey(), 20, 0))
            ), new Wallet().getPublicKey());
        });
        
    }

    @Test 
    void testOldNonceRejected() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), r = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(
            List.of(miner.createTransaction(r.getPublicKey(), 10, 0))), new Wallet().getPublicKey());

        assertEquals(1, k.getNextNonce(miner.getPublicKey()));

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(
                List.of(miner.createTransaction(r.getPublicKey(), 10, 0))), new Wallet().getPublicKey());
        });
    }

    @Test 
    void testSkippedNonceRejected() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), r = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());
        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(r.getPublicKey(), 10, 0),
                miner.createTransaction(r.getPublicKey(), 20, 2))
            ), new Wallet().getPublicKey());
        });
    }

    @Test 
    void testGenesisHeightInChain() {
        Blockchain k = new Blockchain(2);
        
        assertEquals(0, k.getBlock(0).getHeight());
    }

    @Test 
    void testBlockHeightsIncreasedSequentially() {
        Blockchain k = new Blockchain(2);

        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        assertEquals(1, k.getBlock(1).getHeight());
        assertEquals(2, k.getBlock(2).getHeight());
    }

    @Test 
    void testBlockchainValidityWithProperHeights() {
        Blockchain k = new Blockchain(2);

        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        assertTrue(k.isValid());
    }

    @Test 
    void testRewardHeightMatchesBlockHeight() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction t = k.getLatestBlock().getTransactions().get(k.getBlock(1).getTransactions().size() - 1);


        assertEquals(t.getRewardHeight(), k.getLatestBlock().getHeight());
    }

    @Test 
    void testBlockStoresCorrectMerkleRoot() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(
            w.createTransaction(new Wallet().getPublicKey(), 5, 0),
            w.createTransaction(new Wallet().getPublicKey(), 10, 1)
        )), new Wallet().getPublicKey());

        assertEquals(MerkleUtil.calculateMerkleRoot(k.getLatestBlock().getTransactionIds()), k.getLatestBlock().getMerkleRoot());

    }

    @Test 
    void testGenesisHasEmptyMerkleRoot() {
        Blockchain k = new Blockchain(2);
        assertEquals(MerkleUtil.calculateMerkleRoot(new ArrayList<String>()), k.getLatestBlock().getMerkleRoot());
    }

    @Test 
    void testMultipleTransactionBlockHasCorrectMerkleRoot() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(
            w.createTransaction(new Wallet().getPublicKey(), 5, 0),
            w.createTransaction(new Wallet().getPublicKey(), 10, 1),
            w.createTransaction(new Wallet().getPublicKey(), 15, 2),
            w.createTransaction(new Wallet().getPublicKey(), 12, 3),
            w.createTransaction(new Wallet().getPublicKey(), 7, 4)
        )), new Wallet().getPublicKey());

        assertEquals(MerkleUtil.calculateMerkleRoot(k.getLatestBlock().getTransactionIds()), k.getLatestBlock().getMerkleRoot());
    }

    @Test 
    void testBlockchainWithMerkleRootsIsValid() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(
            w.createTransaction(new Wallet().getPublicKey(), 5, 0),
            w.createTransaction(new Wallet().getPublicKey(), 10, 1)
        )), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(
            w.createTransaction(new Wallet().getPublicKey(), 15, 2),
            w.createTransaction(new Wallet().getPublicKey(), 12, 3)
        )), new Wallet().getPublicKey());
        k.addBlock(new ArrayList<Transaction>(List.of(
            w.createTransaction(new Wallet().getPublicKey(), 7, 4)
        )), new Wallet().getPublicKey());

        assertTrue(k.isValid());
    }

    @Test 
    void testPendingTransactionsInitiallyEmpty() {
        Blockchain k = new Blockchain(2);
        assertEquals(0, k.getPendingTransactions().size());
    }

    @Test 
    void testPendingTransactionListCannotBeModified() {
        Blockchain k = new Blockchain(2);
        List<Transaction> l = k.getPendingTransactions();
        assertThrows(UnsupportedOperationException.class, 
            () -> l.add(new Wallet().createTransaction(new Wallet().getPublicKey(), 10, 0)));
    }

    @Test 
    void testValidTransactionAddedToMempool() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction t = w.createTransaction(new Wallet().getPublicKey(), 10, 0);

        assertDoesNotThrow(() -> k.submitTransaction(t));
        assertEquals(1, k.getPendingTransactions().size());
    }

    @Test 
    void testNullTransactionRejected() {
        Blockchain k = new Blockchain(2);
        Transaction t = null;
        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(t));
    }

    @Test 
    void testRewardTransactionRejectedFromMempool() {
        Blockchain k = new Blockchain(2);
        Transaction t = new Transaction(new Wallet().getPublicKey(), 50, 1);

        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(t));
    }

    @Test 
    void testInvalidTransactionRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = new Transaction(w.getPublicKey(), new Wallet().getPublicKey(), 
            10, new Wallet().sign("random"), 0);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(a));
        assertEquals("Invalid transaction.", ex.getMessage());
    }

    @Test 
    void testDuplicatePendingTransactionRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction t = w.createTransaction(new Wallet().getPublicKey(), 10, 0);

        assertDoesNotThrow(() -> k.submitTransaction(t));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(t));
        assertEquals("Invalid, transaction already pending.", ex.getMessage());
        assertEquals(1, k.getPendingTransactions().size());
    }

    @Test 
    void testSequentialPendingNoncesAccepted() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 15, 1);
        
        assertDoesNotThrow(() -> k.submitTransaction(a));
        assertDoesNotThrow(() -> k.submitTransaction(b));
        assertEquals(2, k.getPendingTransactions().size());
        assertEquals(a, k.getPendingTransactions().get(0));
        assertEquals(b, k.getPendingTransactions().get(1));
    }

    @Test 
    void testSkippedPendingNonceRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 15, 2);

        assertDoesNotThrow(() -> k.submitTransaction(a));
        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(b));
    }

    @Test 
    void testOldPendingNonceRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 15, 0);

        assertDoesNotThrow(() -> k.submitTransaction(a));
        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(b));
    }

    @Test 
    void testPendingTransactionsCannotOverspend() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 70, 1);

        assertDoesNotThrow(() -> k.submitTransaction(a));
        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(b));
    }

    @Test 
    void testExactPendingBalanceCanBeSpent() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), w.getPublicKey());
        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 40, 1);

        assertDoesNotThrow(() -> k.submitTransaction(a));
        assertDoesNotThrow(() -> k.submitTransaction(b));
    }

    @Test 
    void testPendingIncomingFundsCanBeSpent() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet(), c = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());

        Transaction t1 = a.createTransaction(b.getPublicKey(), 30, 0);
        k.submitTransaction(t1);
        assertEquals(0, k.getBalance(b.getPublicKey()));

        Transaction t2 = b.createTransaction(c.getPublicKey(), 20, 0);
        assertDoesNotThrow(() -> k.submitTransaction(t2));
    }

    @Test 
    void testCannotSpendPendingFundsBeforeTheyArrive() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet(), c = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        Transaction t2 = b.createTransaction(c.getPublicKey(), 20, 0);

        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(t2));
        assertEquals(0, k.getPendingTransactions().size());
    }

    @Test 
    void testAlreadyMinedTransactionCannotBeResubmitted() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        Transaction t1 = a.createTransaction(b.getPublicKey(), 10, 0);
        k.addBlock(new ArrayList<Transaction>(List.of(t1)), new Wallet().getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> k.submitTransaction(t1));
    }

    @Test 
    void testGetNextNonceIgnoresPendingTransactions() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        assertEquals(0, k.getNextNonce(a.getPublicKey()));

        Transaction t1 = a.createTransaction(b.getPublicKey(), 20, 0);
        k.submitTransaction(t1);
        assertTrue(k.getPendingTransactions().contains(t1));
        assertEquals(0, k.getNextNonce(a.getPublicKey()));
    }

    @Test 
    void testMinePendingTransactionsAddsBlockAndClearsMempool() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        Transaction t1 = a.createTransaction(b.getPublicKey(), 10, 0);
        Transaction t2 = a.createTransaction(b.getPublicKey(), 15, 1);
        k.submitTransaction(t1);
        k.submitTransaction(t2);

        assertEquals(2, k.getPendingTransactions().size());
        assertEquals(2, k.size());

        k.minePendingTransactions(w.getPublicKey());

        assertEquals(0, k.getPendingTransactions().size());
        assertEquals(3, k.size());
        assertEquals(3, k.getLatestBlock().getTransactions().size());
        assertEquals(t1, k.getLatestBlock().getTransactions().get(0));
        assertEquals(t2, k.getLatestBlock().getTransactions().get(1));
        assertEquals(TransactionType.REWARD, k.getLatestBlock().getTransactions().getLast().getType());
        assertEquals(w.getPublicKey(), k.getLatestBlock().getTransactions().getLast().getRecipient());
    }

    @Test 
    void testMinePendingTransactionsNullMinerPreservesMempool() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), a.getPublicKey());
        Transaction t1 = a.createTransaction(b.getPublicKey(), 10, 0);
        k.submitTransaction(t1);
        assertEquals(1, k.getPendingTransactions().size());
        assertEquals(2, k.size());

        assertThrows(IllegalArgumentException.class, () -> k.minePendingTransactions(null));
        assertEquals(1, k.getPendingTransactions().size());
        assertEquals(t1, k.getPendingTransactions().get(0));
        assertEquals(2, k.size());
    }

    @Test 
    void testMinePendingTransactionsEmptyMempoolMinesRewardOnlyBlock() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        assertEquals(0, k.getPendingTransactions().size());
        assertEquals(1, k.size());

        assertDoesNotThrow(() -> k.minePendingTransactions(w.getPublicKey()));
        assertEquals(0, k.getPendingTransactions().size());
        assertEquals(2, k.size());
        assertEquals(1, k.getLatestBlock().getTransactions().size());
        assertEquals(TransactionType.REWARD, k.getLatestBlock().getTransactions().get(0).getType());
        assertEquals(w.getPublicKey(), k.getLatestBlock().getTransactions().get(0).getRecipient());
        assertEquals(50, k.getBalance(w.getPublicKey()));
    }

    @Test 
    void testMiningPendingTransactionsUpdatesConfirmedState() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet(), w = new Wallet();

        k.minePendingTransactions(a.getPublicKey());
        Transaction t1 = a.createTransaction(b.getPublicKey(), 20, 0);
        k.submitTransaction(t1);

        assertEquals(50, k.getBalance(a.getPublicKey()));
        assertEquals(0, k.getBalance(b.getPublicKey()));
        assertEquals(0, k.getNextNonce(a.getPublicKey()));
        assertEquals(1, k.getPendingTransactions().size());

        k.minePendingTransactions(w.getPublicKey());
        assertEquals(30, k.getBalance(a.getPublicKey()));
        assertEquals(20, k.getBalance(b.getPublicKey()));
        assertEquals(1, k.getNextNonce(a.getPublicKey()));
        assertEquals(0, k.getPendingTransactions().size());
        assertTrue(k.isValid());
    }

    @Test 
    void testMinePendingDependentTransactionsInOrder() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet(), b = new Wallet(), c = new Wallet(), w = new Wallet();

        k.minePendingTransactions(a.getPublicKey());
        Transaction t1 = a.createTransaction(b.getPublicKey(), 30, 0);
        Transaction t2 = b.createTransaction(c.getPublicKey(), 20, 0);
        k.submitTransaction(t1);
        k.submitTransaction(t2);
        assertEquals(t1, k.getPendingTransactions().get(0));
        assertEquals(t2, k.getPendingTransactions().get(1));

        assertDoesNotThrow(() -> k.minePendingTransactions(w.getPublicKey()));
        assertEquals(20, k.getBalance(a.getPublicKey()));
        assertEquals(10, k.getBalance(b.getPublicKey()));
        assertEquals(20, k.getBalance(c.getPublicKey()));
        assertEquals(0, k.getPendingTransactions().size());
        assertEquals(1, k.getNextNonce(a.getPublicKey()));
        assertEquals(1, k.getNextNonce(b.getPublicKey()));
        assertEquals(0, k.getNextNonce(c.getPublicKey()));
        assertTrue(k.isValid());
    }

    @Test 
    void testCanSubmitNextNonceAfterMiningPendingTransactions() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet();

        k.minePendingTransactions(a.getPublicKey());
        Transaction t1 = a.createTransaction(new Wallet().getPublicKey(), 11, 0);
        Transaction t2 = a.createTransaction(new Wallet().getPublicKey(), 22, 1);
        k.submitTransaction(t1);
        k.submitTransaction(t2);
        k.minePendingTransactions(new Wallet().getPublicKey());
        assertEquals(2, k.getNextNonce(a.getPublicKey()));

        Transaction t3 = a.createTransaction(new Wallet().getPublicKey(), 6, 2);
        assertDoesNotThrow(() -> k.submitTransaction(t3));
        assertEquals(1, k.getPendingTransactions().size());
        assertEquals(t3, k.getPendingTransactions().get(0));
    }

    @Test 
    void testSubmittedProcessedBlockIsSameAsBlockchainLastBlock() {
        Blockchain k = new Blockchain(2);
        Wallet a = new Wallet();

        k.minePendingTransactions(a.getPublicKey());
        Transaction t1 = a.createTransaction(new Wallet().getPublicKey(), 11, 0);
        k.submitTransaction(t1);
        Block b = k.minePendingTransactions(new Wallet().getPublicKey());
        assertSame(b, k.getLatestBlock());
    }
}
