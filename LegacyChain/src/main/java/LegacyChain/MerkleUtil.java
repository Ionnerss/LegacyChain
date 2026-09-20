package LegacyChain;

import java.util.ArrayList;
import java.util.List;

public class MerkleUtil {
    static String calculateMerkleRoot(List<Transaction> transactions) {
        if (transactions == null) 
            throw new IllegalArgumentException("Invalid transactions.");
        else if (transactions.isEmpty())
            return HashUtil.sha256("");
        else {
            List<String> currentLevel = new ArrayList<>();
            for (Transaction t : transactions) {
                if (t == null)
                    throw new IllegalArgumentException("Invalid transaction data.");
                currentLevel.add(t.getTransactionId());
            }

            while (currentLevel.size() > 1) {
                int i = 0;
                List<String> nextLevel = new ArrayList<>();

                while (i < currentLevel.size()) {
                    String left = currentLevel.get(i), right = "";
                    if (i + 1 < currentLevel.size())
                        right = currentLevel.get(i + 1);
                    else
                        right = left;

                    String parent = HashUtil.sha256(left + right);
                    nextLevel.add(parent);

                    i += 2;
                }
                currentLevel = nextLevel;
            }
            return currentLevel.getLast();
        }

    }
}
