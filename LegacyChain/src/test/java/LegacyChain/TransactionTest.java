package LegacyChain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    @Test
    void testValidTransaction() {
        //Arrange:
        Transaction t = new Transaction("Bob", "Alex", 10);

        //Assert:
        assertEquals("Bob", t.getSender());
        assertEquals("Alex", t.getRecipient());
        assertEquals(10, t.getAmount());
    }

    @Test
    void testZeroAmount() {
        //Assert:
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("Bob", "Alex", 0));
    }

    @Test 
    void testNegativeAmount() {
        //Assert
        assertThrows(IllegalArgumentException.class,
            () -> new Transaction("Bob", "Alex", -10));
    }

    @Test 
    void testNullSender() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction(null, "Bob", 10));
    }

    @Test
    void testNullRecipient() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("Alex", null, 10));
    }

    @Test
    void testEmptySender() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("", "Bob", 10));
    }

    @Test
    void testBlankSender() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("   ", "Bob", 10));
    }

    @Test
    void testEmptyRecipient() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("Alex", "", 10));
    }

    @Test
    void testBlankRecipient() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("Bob", "   ", 10));
    }

    @Test
    void testSREquals() {
        //Assert
        assertThrows(IllegalArgumentException.class, 
            () -> new Transaction("Bob", "Bob", 10));
    }

    @Test
    void testSmallestAmount() {
        //Assert
        assertDoesNotThrow(() -> new Transaction("Bob", "Alex", 1));
    }

    @Test
    void testLargeAmount() {
        //Assert
        assertDoesNotThrow(() -> new Transaction("Bob", "Alex", 9223372036857L));
    }

    @Test
    void testTransactionHashDeterminism() {
        Transaction a = new Transaction("Bob", "Bill", 34);
        Transaction b = new Transaction("Bob", "Bill", 34);

        assertEquals(a.calculateHash(), b.calculateHash());
    }

    @Test
    void testTransactionModsChangeHash() {
        Transaction a = new Transaction("Julie", "Bill", 34);
        Transaction b = new Transaction("Bob", "Bill", 34);

        assertNotEquals(a.calculateHash(), b.calculateHash());
    }
}
