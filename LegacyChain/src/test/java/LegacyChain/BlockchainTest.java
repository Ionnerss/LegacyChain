package LegacyChain;

import org.junit.jupiter.api.Test;

import LegacyChain.Transaction.TransactionType;

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
        Wallet w = new Wallet(), j = new Wallet(), miner = new Wallet();
        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

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
        Wallet w = new Wallet(), j = new Wallet(), miner = new Wallet();
        Block b = k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

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
        Wallet w = new Wallet(), j = new Wallet();
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
        Wallet w = new Wallet();
        Block j = k.addBlock(new ArrayList<Transaction>(), new Wallet().getPublicKey());

        //Assert:
        assertTrue(j.getHash().startsWith(target));
        assertTrue(k.getBlock(1).getHash().startsWith(target));                                                                                                                                                                                                                
    }

    @Test
    void testBlockTransactionListImmutability() {
        Blockchain k = new Blockchain(2);

        Wallet w = new Wallet(), j = new Wallet();
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
                miner.createTransaction(w.getPublicKey(), (long)51, 1))), new Wallet().getPublicKey());
        });
    }

    @Test 
    void testZeroBalanceCannotSpend() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                w.createTransaction(miner.getPublicKey(), (long)1, 1))), new Wallet().getPublicKey());
        });
    }

    @Test
    void testRejectsCombinedOverspendingInSameBlock() {
        Blockchain k = new Blockchain(2);
        Wallet miner = new Wallet(), w = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 1),
                miner.createTransaction(w.getPublicKey(), 30, 2)
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
        Wallet miner = new Wallet(), w = new Wallet(), j = new Wallet();

        k.addBlock(new ArrayList<Transaction>(), miner.getPublicKey());

        assertThrows(IllegalArgumentException.class, () -> {
            k.addBlock(new ArrayList<Transaction>(List.of(
                miner.createTransaction(w.getPublicKey(), (long)30, 1),
                miner.createTransaction(w.getPublicKey(), 30, 2)
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
}
