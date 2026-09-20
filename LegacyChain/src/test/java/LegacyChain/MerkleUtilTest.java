package LegacyChain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class MerkleUtilTest {
    @Test 
    void testNullTransactionListRejected() {
        assertThrows(IllegalArgumentException.class, () -> MerkleUtil.calculateMerkleRoot(null));
    }

    @Test 
    void testEmptyListProducesEmptyMerkleRoot() {
        assertEquals(HashUtil.sha256(""), MerkleUtil.calculateMerkleRoot(new ArrayList<>()));
    }

    @Test 
    void testSingleTransactionIsMerkleRoot() {
        Wallet w = new Wallet();
        Transaction t = w.createTransaction(new Wallet().getPublicKey(), 10, 0);
        assertEquals(t.getTransactionId(), MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(t))));
    }

    @Test 
    void testTwoTransactionsProduceCorrectMerkleRoot() {
        Wallet w = new Wallet();
        Transaction a = w.createTransaction(new Wallet().getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(new Wallet().getPublicKey(), 20, 0);
        assertEquals(HashUtil.sha256(a.getTransactionId() + b.getTransactionId()), 
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(a, b)))
        );
    }

    @Test 
    void testOddTransactionCountDuplicatesLastHash() {
        Wallet w = new Wallet();
        Transaction a = w.createTransaction(new Wallet().getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(new Wallet().getPublicKey(), 20, 0);
        Transaction c = w.createTransaction(new Wallet().getPublicKey(), 30, 0);

        assertEquals(
            HashUtil.sha256( HashUtil.sha256(a.getTransactionId() + b.getTransactionId()) 
            + HashUtil.sha256(c.getTransactionId() + c.getTransactionId())
            ),
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(a, b, c)))       
        );
    }

    @Test 
    void testSameTransactionsProduceSameMerkleRoot() {
        Wallet w = new Wallet();
        Transaction a = w.createTransaction(new Wallet().getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(new Wallet().getPublicKey(), 20, 0);

        assertEquals(
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(a, b))),
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(a, b)))
        );
    }

    @Test 
    void testDifferentTransactionProducesDifferentMerkleRoot() {
        Wallet w = new Wallet();
        Transaction a = w.createTransaction(new Wallet().getPublicKey(), 10, 0);
        Transaction b = w.createTransaction(new Wallet().getPublicKey(), 20, 0);

        assertNotEquals(
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(a))),
            MerkleUtil.calculateMerkleRoot(new ArrayList<Transaction>(List.of(b)))
        );
    }

    @Test 
    void testValidMerkleProof() {
        
    }
}
