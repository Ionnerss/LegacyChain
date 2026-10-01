package legacychain.merkle;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import legacychain.crypto.HashUtil;
import legacychain.merkle.MerkleUtil.MerkleProofStep;

public class MerkleUtilTest {
    @Test 
    void testNullTransactionListRejected() {
        assertThrows(IllegalArgumentException.class, () -> MerkleUtil.calculateMerkleRoot(null));
    }

    @Test 
    void testEmptyTransactionIdListProducesEmptyMerkleRoot() {
        assertEquals(HashUtil.sha256(""), MerkleUtil.calculateMerkleRoot(new ArrayList<>()));
    }

    @Test 
    void testSingleTransactionIdIsMerkleRoot() {
        String someTransactionId = HashUtil.sha256("input");
        assertEquals(someTransactionId, MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(someTransactionId))));
    }

    @Test 
    void testTwoTransactionIdsProduceCorrectMerkleRoot() {
        String a = HashUtil.sha256("inputA"), b = HashUtil.sha256("inputB");
        assertEquals(HashUtil.sha256(a + b), 
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(a, b)))
        );
    }

    @Test 
    void testOddTransactionIdCountDuplicatesLastHash() {
        String a = HashUtil.sha256("inputA"), b = HashUtil.sha256("inputB"), c = HashUtil.sha256("inputC");
        assertEquals(
            HashUtil.sha256( HashUtil.sha256(a + b) + HashUtil.sha256(c + c)),
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(a, b, c)))       
        );
    }

    @Test 
    void testSameTransactionIdsProduceSameMerkleRoot() {
        String a = HashUtil.sha256("inputA"), b = HashUtil.sha256("inputB");

        assertEquals(
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(a, b))),
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(a, b)))
        );
    }

    @Test 
    void testDifferentTransactionIdsProduceDifferentMerkleRoots() {
        String a = HashUtil.sha256("inputA"), b = HashUtil.sha256("inputB");

        assertNotEquals(
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(a))),
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(b)))
        );
    }

    @Test 
    void testValidMerkleProofVerifies() {
        List<String> l = new ArrayList<String>(List.of(
            HashUtil.sha256("inputA"),
            HashUtil.sha256("inputB"),
            HashUtil.sha256("inputC"),
            HashUtil.sha256("inputD")
        ));

        assertTrue(
            MerkleUtil.verifyProof(l.get(1), 
            MerkleUtil.generateProof(l, 1), 
            MerkleUtil.calculateMerkleRoot(l))
        );
    }

    @Test 
    void testProofForDifferentTransactionIdFails() {
        List<String> l = new ArrayList<String>(List.of(
            HashUtil.sha256("inputA"),
            HashUtil.sha256("inputB"),
            HashUtil.sha256("inputC"),
            HashUtil.sha256("inputD")
        ));

        assertFalse(MerkleUtil.verifyProof(
            l.get(0), 
            MerkleUtil.generateProof(l, 1), 
            MerkleUtil.calculateMerkleRoot(l))
        );
    }

    @Test 
    void testTamperedSiblingHashFailsVerification() {
        List<String> l = new ArrayList<String>(List.of(
            HashUtil.sha256("inputA"),
            HashUtil.sha256("inputB"),
            HashUtil.sha256("inputC"),
            HashUtil.sha256("inputD")
        ));

        List<MerkleProofStep> proof = new ArrayList<>(MerkleUtil.generateProof(l, 1));
        MerkleProofStep original = proof.get(0);
        proof.set(0, new MerkleProofStep("random", original.getPosition()));

        assertFalse(MerkleUtil.verifyProof(l.get(1), proof, MerkleUtil.calculateMerkleRoot(l)));
    }
    
    @Test 
    void testWrongMerkleRootFailsVerification() {
        List<String> l = new ArrayList<String>(List.of(
            HashUtil.sha256("inputA"),
            HashUtil.sha256("inputB"),
            HashUtil.sha256("inputC"),
            HashUtil.sha256("inputD")
        ));

        assertFalse(MerkleUtil.verifyProof(
            l.get(1), 
            MerkleUtil.generateProof(l, 1), 
            HashUtil.sha256("random"))
        );
    }

    @Test 
    void testSingleTransactionIdProducesEmptyValidProof() {
        List<String> l = new ArrayList<String>(List.of(HashUtil.sha256("inputA")));
        String root = MerkleUtil.calculateMerkleRoot(l);
        List<MerkleProofStep> proof = MerkleUtil.generateProof(l, 0);

        assertTrue(proof.isEmpty());
        assertTrue(MerkleUtil.verifyProof(l.get(0), proof, root));
    }

    @Test
    void testMerkleProofWorksForOddTransactionIdCount() {
        List<String> l = new ArrayList<String>(List.of(
            HashUtil.sha256("inputA"),
            HashUtil.sha256("inputB"),
            HashUtil.sha256("inputC")
        ));

        assertTrue(MerkleUtil.verifyProof(
            l.get(2), 
            MerkleUtil.generateProof(l,2),
            MerkleUtil.calculateMerkleRoot(l)
        ));
    }

    @Test 
    void testNullTransactionIdRejected() {
        List<String> l = new ArrayList<String>();
        l.add(null);
        assertThrows(IllegalArgumentException.class, () -> 
            MerkleUtil.calculateMerkleRoot(l));
    }

    @Test
    void testEmptyTransactionIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> 
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of(""))));
    }

    @Test 
    void testBlankTransactionIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> 
            MerkleUtil.calculateMerkleRoot(new ArrayList<String>(List.of("     "))));
    }
}
