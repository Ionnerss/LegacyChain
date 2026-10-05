package legacychain.persistence;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import legacychain.core.Transaction;
import legacychain.core.TransactionType;
import legacychain.persistence.snapshot.TransactionSnapshot;

public class TransactionMapper {
    public TransactionSnapshot toSnapshot(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("Invalid transaction.");
        if (transaction.getType() == TransactionType.NORMAL) {
            return new TransactionSnapshot(
                transaction.getType(), 
                encodePublickKey(transaction.getSender()), 
                encodePublickKey(transaction.getRecipient()), 
                transaction.getAmount(), Base64.getEncoder().encodeToString(transaction.getSignature()), 
                transaction.getTransactionNonce(), null
            );
        }
        else if (transaction.getType() == TransactionType.REWARD) {
            return new TransactionSnapshot(
                transaction.getType(), 
                null, 
                encodePublickKey(transaction.getRecipient()), 
                transaction.getAmount(),
                null,
                null, 
                transaction.getRewardHeight()
            );
        }
        else throw new IllegalArgumentException("Invalid transaction.");
    }

    public Transaction fromSnapshot(TransactionSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");
        if (snapshot.type() == TransactionType.NORMAL) {
            if (snapshot.sender() == null 
                || snapshot.recipient() == null 
                || snapshot.signature() == null 
                || snapshot.transactonNonce() == null 
                || snapshot.rewardHeight() != null)
                throw new IllegalArgumentException("Invalid snapshot.");

            PublicKey sender = decodePublicKey(snapshot.sender()), recipient = decodePublicKey(snapshot.recipient());
            byte[] signature = Base64.getDecoder().decode(snapshot.signature());

            Transaction nTransaction = new Transaction(sender, recipient, snapshot.amount(), signature, snapshot.transactonNonce());
            if (!nTransaction.isValid())
                throw new IllegalArgumentException("Invalid snapshot.");
            return nTransaction;
        }
        else if (snapshot.type() == TransactionType.REWARD) {
            if (snapshot.sender() != null
                || snapshot.recipient() == null
                || snapshot.signature() != null
                || snapshot.transactonNonce() != null
                || snapshot.rewardHeight() == null)
                throw new IllegalArgumentException("Invalid snapshot.");

            PublicKey recipient = decodePublicKey(snapshot.recipient());
            Transaction nTransaction = Transaction.restoreReward(recipient, snapshot.amount(), snapshot.rewardHeight());
            if (!nTransaction.isValid())
                throw new IllegalArgumentException("Invalid snapshot.");
            return nTransaction;
        }
        else throw new IllegalArgumentException("Invalid snapshot.");
    }

    private String encodePublickKey(PublicKey key) {
        byte[] bytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private PublicKey decodePublicKey(String keyString) {
        if (keyString == null || keyString.isBlank()) 
            throw new IllegalArgumentException("Invalid transaction data.");
        try {
            byte[] bytes = Base64.getDecoder().decode(keyString);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
