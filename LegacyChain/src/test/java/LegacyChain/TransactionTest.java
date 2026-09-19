package LegacyChain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    @Test
    void testValidTransaction() {
        Wallet sender = new Wallet(), recipient = new Wallet();
        long amount = 100;
        Transaction t = sender.createTransaction(recipient.getPublicKey(), amount, 0);

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
                Transaction t = sender.createTransaction(recipient.getPublicKey(), 0, 0);
            });
    }

    @Test 
    void testNegativeAmount() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet(), recipient = new Wallet();
                Transaction t = sender.createTransaction(recipient.getPublicKey(), -100, 0);
            });
    }

    @Test 
    void testNullSender() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet(), recipient = new Wallet();
                Transaction t = new Transaction(null, recipient.getPublicKey(), 100, sender.sign("yolo"), 0);
            });
    }

    @Test
    void testNullRecipient() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet();
                Transaction t = new Transaction(sender.getPublicKey(), null, 100, sender.sign("yolo"), 0);
            });
    }

    @Test
    void testSREquals() {
        assertThrows(IllegalArgumentException.class, 
            () -> {
                Wallet sender = new Wallet();
                Transaction t = new Transaction(sender.getPublicKey(), sender.getPublicKey(), 100, sender.sign("yolo"), 0);
            });
    }

    @Test
    void testSmallestAmount() {
        Wallet w = new Wallet(), j = new Wallet();
        assertDoesNotThrow(() -> new Transaction(w.getPublicKey(), j.getPublicKey(), 1, w.sign("yolo"), 0));
    }

    @Test
    void testLargeAmount() {
        Wallet w = new Wallet(), j = new Wallet();
        assertDoesNotThrow(() -> new Transaction(w.getPublicKey(), j.getPublicKey(), 9223372036857L, w.sign("yolo"), 0));
    }

    @Test
    void testTransactionHashDeterminism() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction a = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"), 0);
        Transaction b = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"), 0);

        assertEquals(a.calculateHash(), b.calculateHash());
    }

    @Test
    void testTransactionModsChangeHash() {
        Wallet w = new Wallet(), j = new Wallet(), k = new Wallet();
        Transaction a = new Transaction(w.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"), 0);
        Transaction b = new Transaction(k.getPublicKey(), j.getPublicKey(), 34, w.sign("yolo"), 0);

        assertNotEquals(a.calculateHash(), b.calculateHash());
    }

    @Test 
    void testTransactionIdMatchesCalculatedHash() {
        Wallet a = new Wallet();
        Transaction b = a.createTransaction(new Wallet().getPublicKey(), 34, 0);
        assertEquals(b.calculateHash(), b.getTransactionId());
    }

    @Test 
    void testSameTransactionDataProducesSameId() {
        Wallet w = new Wallet(), j = new Wallet();

        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 10, 0);

        assertEquals(a.getTransactionId(), b.getTransactionId());
    }

    @Test 
    void testDifferentNonceProducesDifferentId() {
        Wallet w = new Wallet(), j = new Wallet();

        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 10, 1);

        assertNotEquals(a.getTransactionId(), b.getTransactionId());
    }

    @Test 
    void testDifferentAmountProducesDifferentId() {
        Wallet w = new Wallet(), j = new Wallet();

        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(j.getPublicKey(), 20, 0);

        assertNotEquals(a.getTransactionId(), b.getTransactionId());
    }

    @Test
    void testDifferentRecipientProducesDifferentId() {
        Wallet w = new Wallet(), j = new Wallet();

        Transaction a = w.createTransaction(j.getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(new Wallet().getPublicKey(), 10, 0);

        assertNotEquals(a.getTransactionId(), b.getTransactionId());
    }

    @Test 
    void testRewardAtDifferentHeightsProducesDifferentIds() {
        Wallet w = new Wallet();

        Transaction a = new Transaction(w.getPublicKey(), 50, 1);
        Transaction b = new Transaction(w.getPublicKey(), 50, 2);

        assertNotEquals(a.getTransactionId(), b.getTransactionId());
    }
}
