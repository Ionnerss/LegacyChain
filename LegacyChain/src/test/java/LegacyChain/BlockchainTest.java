package LegacyChain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
        k.addBlock("Block 1");

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
        Block b = k.addBlock("Block 1");

        /*
        Assert:
            Expected: block1 prev hash = genesis hash
        */
       assertEquals(k.getBlock(0).getHash(), b.getPreviousHash());
    }

    @Test
    void testProofOfWork() {
        //Arrange
        Blockchain k = new Blockchain(2);

        // Act: add four blocks and print their hashes for manual verification
        k.addBlock("Block A");
        k.addBlock("Block B");
        k.addBlock("Block C");
        k.addBlock("Block D");

        // Assert: genesis + 4 correctly linked and secure
        assertTrue(k.isValid());
    }
}
