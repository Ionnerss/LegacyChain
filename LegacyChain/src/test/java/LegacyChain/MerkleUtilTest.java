package LegacyChain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import LegacyChain.MerkleUtil.MerkleProofStep;

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
        Wallet w = new Wallet();
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0),
            w.createTransaction(new Wallet().getPublicKey(), 15, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0)
        ));

        assertTrue(MerkleUtil.verifyProof(
            l.get(1).getTransactionId(), 
            MerkleUtil.generateProof(l, 1), 
            MerkleUtil.calculateMerkleRoot(l))
        );
    }

    @Test 
    void testProofForDifferentTransaction() {
        Wallet w = new Wallet();
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0),
            w.createTransaction(new Wallet().getPublicKey(), 15, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0)
        ));

        assertFalse(MerkleUtil.verifyProof(
            l.get(0).getTransactionId(), 
            MerkleUtil.generateProof(l, 1), 
            MerkleUtil.calculateMerkleRoot(l))
        );
    }

    @Test 
    void testTamperedSiblingHashRejected() {
        Wallet w = new Wallet();
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0),
            w.createTransaction(new Wallet().getPublicKey(), 15, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0)
        ));

        List<MerkleProofStep> proof = new ArrayList<>(MerkleUtil.generateProof(l, 1));
        MerkleProofStep original = proof.get(0);

        proof.set(0, new MerkleProofStep("random", original.getPosition()));

        assertFalse(MerkleUtil.verifyProof(
            l.get(1).getTransactionId(), 
            proof, 
            MerkleUtil.calculateMerkleRoot(l))
        );
    }
    
    @Test 
    void testWrongMerkleRootRejected() {
        Wallet w = new Wallet();
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0),
            w.createTransaction(new Wallet().getPublicKey(), 15, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0)
        ));

        assertFalse(MerkleUtil.verifyProof(
            l.get(1).getTransactionId(), 
            MerkleUtil.generateProof(l, 1), 
            HashUtil.sha256("random"))
        );
    }

    @Test 
    void testSingleTransactionEmptyProofValid() {
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0)));

        String root = MerkleUtil.calculateMerkleRoot(l);
        List<MerkleProofStep> proof = MerkleUtil.generateProof(l, 0);

        assertTrue(proof.isEmpty());

        assertTrue(MerkleUtil.verifyProof(l.get(0).getTransactionId(), proof, root));
    }

    @Test
    void testProofWorksForOddTransactionCount() {
        Wallet w = new Wallet();
        List<Transaction> l = new ArrayList<Transaction>(List.of(
            new Wallet().createTransaction(new  Wallet().getPublicKey(), 10, 0),
            w.createTransaction(new Wallet().getPublicKey(), 15, 0),
            new Wallet().createTransaction(new Wallet().getPublicKey(), 30, 0)
        ));

        assertTrue(MerkleUtil.verifyProof(
            l.get(2).getTransactionId(), 
            MerkleUtil.generateProof(l,2),
            MerkleUtil.calculateMerkleRoot(l)
        ));
    }
}
