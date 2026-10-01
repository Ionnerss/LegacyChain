package legacychain.merkle;

import java.util.ArrayList;
import java.util.List;
import legacychain.crypto.HashUtil;

public class MerkleUtil {

    private static class MerkleNode {
        MerkleNode parent;
        final MerkleNode left;
        final MerkleNode right;
        final String hash;
        MerkleNode(String hash) { 
            this.hash = hash;
            this.left = null;
            this.right = null;
            this.parent = null;
        }
        MerkleNode(String hash, MerkleNode left, MerkleNode right) {
            this.hash = hash;
            this.left = left;
            this.right = right;
            left.parent = this;
            right.parent = this;
        }
    }

    private static class MerkleTree {
        final MerkleNode root;
        final List<MerkleNode> leaves;

        MerkleTree(MerkleNode root, List<MerkleNode> leaves) {
            this.root = root;
            this.leaves = List.copyOf(leaves);
        }
    }

    static enum MerklePosition { LEFT, RIGHT }

    static class MerkleProofStep {
        final String siblingHash;
        final MerklePosition position;

        MerkleProofStep(String siblingHash, MerklePosition position) {
            this.siblingHash = siblingHash;
            this.position = position;
        }

        public String getSiblingHash() { return this.siblingHash; }

        public MerklePosition getPosition() { return this.position; }
    }

    private static MerkleTree buildMerkleTree(List<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty())
            throw new IllegalArgumentException("Invalid transactions.");
        for (String t : transactionIds) {
            if (t.isBlank()) throw new IllegalArgumentException("Invalid transactions.");
        }

        List<MerkleNode> leaves = new ArrayList<>();
        List<MerkleNode> currentLevel = new ArrayList<>();
        for (String t : transactionIds) {
            if (t == null) throw new IllegalArgumentException("Invalid transaction.");

            MerkleNode leaf = new MerkleNode(t);
            currentLevel.add(leaf);
            leaves.add(leaf);
        }

        while (currentLevel.size() > 1) {
            int i = 0;
            List<MerkleNode> nextLevel = new ArrayList<>();

            while (i < currentLevel.size()) {
                MerkleNode left = currentLevel.get(i), right = null;
                
                if (i + 1 < currentLevel.size())
                    right = currentLevel.get(i + 1);
                else
                    right = new MerkleNode(currentLevel.get(i).hash);

                MerkleNode parent = new MerkleNode(HashUtil.sha256(left.hash + right.hash), left, right);
                nextLevel.add(parent);  
                i += 2;
            }
            currentLevel = nextLevel;
        }
        MerkleNode root = currentLevel.getFirst();
        return new MerkleTree(root, leaves);
    }

    public static String calculateMerkleRoot(List<String> transactionIds) {
        if (transactionIds == null) throw new IllegalArgumentException("Invalid transactions.");
        if (transactionIds.isEmpty()) return HashUtil.sha256("");

        for (String t : transactionIds) {
            if (t == null) throw new IllegalArgumentException("Invalid transactions.");
            if (t.isBlank()) throw new IllegalArgumentException("Invalid transactions.");
        }

        MerkleTree tree = buildMerkleTree(transactionIds);
        return tree.root.hash;
    }

    static List<MerkleProofStep> generateProof(List<String> transactionIds, int transactionIndex) {
        if (transactionIds == null || transactionIds.isEmpty())
            throw new IllegalArgumentException("Invalid transactions.");
        for (String t : transactionIds) {
            if (t.isBlank()) throw new IllegalArgumentException("Invalid transactions.");
        }
        if (transactionIndex < 0 || transactionIndex >= transactionIds.size())
            throw new IllegalArgumentException("Invalid transaction index.");

        MerkleTree tree = buildMerkleTree(transactionIds);
        MerkleNode current = tree.leaves.get(transactionIndex);
        List<MerkleProofStep> proof = new ArrayList<>();

        while (current.parent != null) {
            MerkleNode parent = current.parent;
            MerkleNode sibling = null;
            MerklePosition position = null;

            if (parent.left == current) {
                sibling = parent.right;
                position = MerklePosition.RIGHT;
            }
            else {
                sibling = parent.left;
                position = MerklePosition.LEFT;
            }
            proof.add(new MerkleProofStep(sibling.hash, position));
            current = parent;
        }
        return List.copyOf(proof);
    }

    static boolean verifyProof(String transactionId, List<MerkleProofStep> proof, String expectedMerkleRoot) {
        if (transactionId == null || transactionId.isEmpty() || transactionId.isBlank())
            throw new IllegalArgumentException("Invalid transaction id.");
        if (proof == null)
            throw new IllegalArgumentException("Invalid proof.");
        if (expectedMerkleRoot == null || expectedMerkleRoot.isEmpty() || expectedMerkleRoot.isBlank())
            throw new IllegalArgumentException("Invalid root.");

        String currentHash = transactionId;

        for (MerkleProofStep step : proof) {
            if (step == null || step.siblingHash == null || step.position == null) return false;

            if (step.position == MerklePosition.RIGHT)
                currentHash = HashUtil.sha256(currentHash + step.siblingHash);
            else if (step.position == MerklePosition.LEFT)
                currentHash = HashUtil.sha256(step.siblingHash + currentHash);
            else return false;
        }

        if (!currentHash.equals(expectedMerkleRoot)) return false;
        return true;
    }
}
