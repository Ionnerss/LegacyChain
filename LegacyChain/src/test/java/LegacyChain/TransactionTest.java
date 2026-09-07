package LegacyChain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    @Test
    void testValidTransaction() {
        Wallet sender = new Wallet(), recipient = new Wallet();
        long amount = 100;
        Transaction t = sender.createTransaction(recipient.getPublicKey(), amount);

        assertEquals(sender.getPublicKey(), t.getSender());
        assertEquals(recipient.getPublicKey(), t.getRecipient());
        assertEquals(100, t.getAmount());
        assertTrue(t.isValid());
    }

    @Test
    void testZeroAmount() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet(), recipient = new Wallet();
                Transaction t = sender.createTransaction(recipient.getPublicKey(), 0);
            });
    }

    @Test 
    void testNegativeAmount() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet(), recipient = new Wallet();
                Transaction t = sender.createTransaction(recipient.getPublicKey(), -100);
            });
    }

    @Test 
    void testNullSender() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet(), recipient = new Wallet();
                Transaction t = new Transaction(null, recipient.getPublicKey(), 100, sender.sign(null));
            });
    }

    // @Test
    // void testNullRecipient() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("Alex", null, 10));
    // }

    // @Test
    // void testEmptySender() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("", "Bob", 10));
    // }

    // @Test
    // void testBlankSender() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("   ", "Bob", 10));
    // }

    // @Test
    // void testEmptyRecipient() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("Alex", "", 10));
    // }

    // @Test
    // void testBlankRecipient() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("Bob", "   ", 10));
    // }

    // @Test
    // void testSREquals() {
    //     //Assert
    //     assertThrows(IllegalArgumentException.class, 
    //         () -> new Transaction("Bob", "Bob", 10));
    // }

    // @Test
    // void testSmallestAmount() {
    //     //Assert
    //     assertDoesNotThrow(() -> new Transaction("Bob", "Alex", 1));
    // }

    // @Test
    // void testLargeAmount() {
    //     //Assert
    //     assertDoesNotThrow(() -> new Transaction("Bob", "Alex", 9223372036857L));
    // }

    // @Test
    // void testTransactionHashDeterminism() {
    //     Transaction a = new Transaction("Bob", "Bill", 34);
    //     Transaction b = new Transaction("Bob", "Bill", 34);

    //     assertEquals(a.calculateHash(), b.calculateHash());
    // }

    // @Test
    // void testTransactionModsChangeHash() {
    //     Transaction a = new Transaction("Julie", "Bill", 34);
    //     Transaction b = new Transaction("Bob", "Bill", 34);

    //     assertNotEquals(a.calculateHash(), b.calculateHash());
    // }
}
