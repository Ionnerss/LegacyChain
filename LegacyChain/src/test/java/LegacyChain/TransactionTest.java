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
                Transaction t = new Transaction(null, recipient.getPublicKey(), 100, sender.sign("yolo"));
            });
    }

    @Test
    void testNullRecipient() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet();
                Transaction t = new Transaction(sender.getPublicKey(), null, 100, sender.sign("yolo"));
            });
    }

    @Test
    void testSREquals() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet();
                Transaction t = new Transaction(sender.getPublicKey(), sender.getPublicKey(), 100, sender.sign("yolo"));
            });
    }

    @Test
    void testSmallestAmount() {
        Wallet w = new Wallet(), j = new Wallet();
        assertDoesNotThrow(() -> new Transaction(w.getPublicKey(), j.getPublicKey(), 1, w.sign("yolo")));
    }

    @Test
    void testLargeAmount() {
        Wallet w = new Wallet(), j = new Wallet();
        assertDoesNotThrow(() -> new Transaction(w.getPublicKey(), j.getPublicKey(), 9223372036857L, w.sign("yolo")));
    }

    @Test
    void testTransactionHashDeterminism() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction a = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"));
        Transaction b = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"));

        assertEquals(a.calculateHash(), b.calculateHash());
    }

    @Test
    void testTransactionModsChangeHash() {
        Wallet w = new Wallet(), j = new Wallet(), k = new Wallet();
        Transaction a = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"));
        Transaction b = new Transaction(k.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"));

        assertNotEquals(a.calculateHash(), b.calculateHash());
    }
}
