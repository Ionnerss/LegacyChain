package legacychain.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import legacychain.core.Blockchain;
import legacychain.core.Transaction;
import legacychain.core.TransactionType;
import legacychain.persistence.snapshot.TransactionSnapshot;
import legacychain.wallet.Wallet;

public class TransactionMapperTest {
    @Test 
    void testNormalTransactionMapsToSnapshot() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction t = w.createTransaction(j.getPublicKey(), 10, 0);

        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);

        assertEquals(TransactionType.NORMAL, ts.type());
        assertArrayEquals(t.getSender().getEncoded(), Base64.getDecoder().decode(ts.sender()));
        assertArrayEquals(t.getRecipient().getEncoded(), Base64.getDecoder().decode(ts.recipient()));
        assertEquals(t.getAmount(), ts.amount());
        assertEquals(0, ts.transactonNonce());
        assertNull(ts.rewardHeight());
        assertArrayEquals(t.getSignature(), Base64.getDecoder().decode(ts.signature()));
    }

    @Test
    void testRewardTransactionMapsToSnapshot() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();
        k.minePendingTransactions(w.getPublicKey());
        Transaction t = k.getLatestBlock().getTransactions().getLast();

        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);

        assertEquals(TransactionType.REWARD, ts.type());
        assertNull(ts.sender());
        assertArrayEquals(t.getRecipient().getEncoded(), Base64.getDecoder().decode(ts.recipient()));
        assertEquals(t.getAmount(), ts.amount());
        assertNull(ts.transactonNonce());
        assertEquals(t.getRewardHeight(), ts.rewardHeight());
        assertNull(ts.signature());
    }

    @Test 
    void testNullTransactionRejected() {
        TransactionMapper tm = new TransactionMapper();
        assertThrows(IllegalArgumentException.class, () -> {TransactionSnapshot ts = tm.toSnapshot(null);});
    }

    @Test 
    void testNormalTransactionRoundTrip() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction t = w.createTransaction(j.getPublicKey(), 10, 0);

        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);
        Transaction fromSnap = tm.fromSnapshot(ts);

        assertEquals(t.getType(), fromSnap.getType());
        assertEquals(t.getSender(), fromSnap.getSender());
        assertEquals(t.getRecipient(), fromSnap.getRecipient());
        assertEquals(t.getAmount(), fromSnap.getAmount());
        assertEquals(t.getTransactionNonce(), fromSnap.getTransactionNonce());
        assertEquals(t.getRewardHeight(), fromSnap.getRewardHeight());
        assertArrayEquals(t.getSignature(), fromSnap.getSignature());
        assertEquals(t.getTransactionId(), fromSnap.getTransactionId());
        assertTrue(fromSnap.isValid());
    }

    @Test 
    void testTamperedNormalSnapshotSignatureRejected() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction t = w.createTransaction(j.getPublicKey(), 10, 0);

        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);

        byte[] bytes = Base64.getDecoder().decode(ts.signature());
        bytes[2] = (byte)(bytes[2] + 1);
        String moddedSignature = Base64.getEncoder().encodeToString(bytes);

        TransactionSnapshot tsModded = new TransactionSnapshot(
            ts.type(),
            ts.sender(),
            ts.recipient(),
            ts.amount(),
            moddedSignature,
            ts.transactonNonce(),
            ts.rewardHeight()
        );

        assertThrows(IllegalArgumentException.class, () -> tm.fromSnapshot(tsModded));
    }

    @Test 
    void testMalformedNormalSnapshotSenderRejected() {
        Wallet w = new Wallet(), j = new Wallet();
        Transaction t = w.createTransaction(j.getPublicKey(), 10, 0);

        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);

        TransactionSnapshot tsModded = new TransactionSnapshot(
            ts.type(),
            Base64.getEncoder().encodeToString(new byte[] {1, 2, 3}),
            ts.recipient(),
            ts.amount(),
            ts.signature(),
            ts.transactonNonce(),
            ts.rewardHeight()       
        );
        assertThrows(IllegalArgumentException.class, () -> tm.fromSnapshot(tsModded));
    }

    @Test
    void testRewardTransactionRoundTrip() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.minePendingTransactions(w.getPublicKey());
        Transaction t = k.getLatestBlock().getTransactions().getLast();
        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);
        Transaction fromSnap = tm.fromSnapshot(ts);

        assertEquals(t.getType(), fromSnap.getType());
        assertEquals(t.getSender(), fromSnap.getSender());
        assertEquals(t.getRecipient(), fromSnap.getRecipient());
        assertEquals(t.getAmount(), fromSnap.getAmount());
        assertEquals(t.getTransactionNonce(), fromSnap.getTransactionNonce());
        assertEquals(t.getRewardHeight(), fromSnap.getRewardHeight());
        assertArrayEquals(t.getSignature(), fromSnap.getSignature());
        assertEquals(t.getTransactionId(), fromSnap.getTransactionId());
        assertTrue(fromSnap.isValid());
    }

    @Test 
    void testMalformedRewardSnapshotRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet();

        k.minePendingTransactions(w.getPublicKey());
        Transaction t = k.getLatestBlock().getTransactions().getLast();
        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(t);

        TransactionSnapshot malformed = new TransactionSnapshot(
            ts.type(),
            ts.sender(),
            ts.recipient(),
            ts.amount(),
            ts.signature(),
            0,
            ts.rewardHeight()
        );
        assertThrows(IllegalArgumentException.class, () -> tm.fromSnapshot(malformed));
    }
}
