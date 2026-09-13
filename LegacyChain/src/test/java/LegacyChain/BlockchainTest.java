package LegacyChain;

import org.junit.jupiter.api.Test;
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
        Wallet w = new Wallet(), j = new Wallet();
        k.addBlock(new ArrayList<Transaction>(
            List.of(w.createTransaction(j.getPublicKey(), 100))
        ));

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
        Wallet w = new Wallet(), j = new Wallet();
        Block b = k.addBlock(new ArrayList<Transaction>(
            List.of(w.createTransaction(j.getPublicKey(), 100))
        ));

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
        k.addBlock(new ArrayList<Transaction>(
            List.of(w.createTransaction(j.getPublicKey(), 100))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(w.createTransaction(new Wallet().getPublicKey(), 100))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Wallet().createTransaction(new Wallet().getPublicKey(), 150))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Wallet().createTransaction(new Wallet().getPublicKey(), 200))
        ));

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
        Block j = k.addBlock(new ArrayList<Transaction>(
            List.of(w.createTransaction(new Wallet().getPublicKey(), 100))
        ));

        //Assert:
        assertTrue(j.getHash().startsWith(target));
        assertTrue(k.getBlock(1).getHash().startsWith(target));                                                                                                                                                                                                                
    }

    @Test
    void testBlockTransactionListImmutability() {
        Blockchain k = new Blockchain(2);

        Wallet w = new Wallet(), j = new Wallet();
        ArrayList<Transaction> b = new ArrayList<Transaction>(List.of(
            w.createTransaction(j.getPublicKey(), 100),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 150)
        ));

        Block s = k.addBlock(b);

        b.add(new Wallet().createTransaction(new Wallet().getPublicKey(), 300));

        assertNotEquals(s.getTransactions(), b);
        assertEquals(2, s.getTransactions().size());
    }

    @Test 
    void testAcceptedBlock() {
        Blockchain k = new Blockchain(2);

        ArrayList<Transaction> b = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new Wallet().getPublicKey(), 100),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 150),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 200),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 10),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 15)
        ));

        assertDoesNotThrow(() -> k.addBlock(b));
        assertEquals(2, k.size());
    }

    @Test 
    void testInvalidTransaction() {
        Blockchain k = new Blockchain(2);
        ArrayList<Transaction> b = new ArrayList<Transaction>();

        Wallet w = new Wallet();
        b.add(new Transaction(new Wallet().getPublicKey(), new Wallet().getPublicKey(), 100, w.sign("yolo")));

        assertThrows(IllegalArgumentException.class, () -> k.addBlock(b));
    }

    @Test 
    void testTransactionsOneInvalid() {
        Blockchain k = new Blockchain(2);
        ArrayList<Transaction> b = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new Wallet().getPublicKey(), 100),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 150),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 200)
        ));
        b.add(null);
        b.add(new Wallet().createTransaction(new Wallet().getPublicKey(), 15));

        assertThrows(IllegalArgumentException.class,
            () -> k.addBlock(b));
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

        k.addBlock(new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new Wallet().getPublicKey(), 700),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 150),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 320)
        )));

        assertEquals(0, k.getBalance(w.getPublicKey()));
    }
}
