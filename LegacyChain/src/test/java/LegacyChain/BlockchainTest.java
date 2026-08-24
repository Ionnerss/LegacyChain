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
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Bob", "Alex", 10))
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
        Block b = k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Bob", "Alex", 10))
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
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Bob", "Alex", 10))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Bill", "Alex", 10))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Galaxy Eater", "Alex", 10))
        ));
        k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Star Destroyer", "Alex", 10))
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
        Block j = k.addBlock(new ArrayList<Transaction>(
            List.of(new Transaction("Star Destroyer", "Alex", 10))
        ));

        //Assert:
        assertTrue(j.getHash().startsWith(target));
        assertTrue(k.getBlock(1).getHash().startsWith(target));                                                                                                                                                                                                                
    }

    @Test
    void testBlockTransactionListImmutability() {
        Blockchain k = new Blockchain(2);

        ArrayList<Transaction> b = new ArrayList<Transaction>(List.of(
            new Transaction("Star Destroyer", "Alex", 10),
            new Transaction("Bob", "Joe", 30)
        ));

        Block j = k.addBlock(b);

        b.add(new Transaction("Billy", "Nicky", 25));

        assertNotEquals(j.getTransactions(), b);
    }
}
