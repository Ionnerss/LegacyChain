package LegacyChain;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

public class Transaction {
    private final PublicKey sender;
    private final PublicKey recipient;
    private final long amount;
    private final byte[] signature;
    private TransactionType type;

    //REWARD since every wallet starts at 0, must be able to mine
    private enum TransactionType {
        NORMAL, REWARD
    }

    public Transaction(PublicKey sender, PublicKey recipient, long amount, byte[] signature, TransactionType type) {
        if (recipient == null || amount <= 0)
                throw new IllegalArgumentException("Invalid transaction data.");

        this.recipient = recipient;
        this.amount = amount;

        if (type == TransactionType.NORMAL) {
            if (sender == null || sender.equals(recipient)) throw new IllegalArgumentException("Invalid transaction data.");
            this.sender = sender;
    
            if (signature == null || signature.length == 0)
                throw new IllegalArgumentException("Invalid signature data.");
    
            this.signature = Arrays.copyOf(signature, signature.length);
            this.type = type;
        }
        else if (type == TransactionType.REWARD) {
            this.sender = null;
            this.signature = null;
            this.type = type;
        }
        else throw new IllegalArgumentException("Invalid transaction type.");
    }

    public PublicKey getSender() { return this.sender; }

    public PublicKey getRecipient() { return this.recipient; }

    public long getAmount() { return this.amount; }

    String calculateHash() {
        String candidateHash = signingData(sender, recipient, amount) + Base64.getEncoder().encodeToString(signature);
        return HashUtil.sha256(candidateHash);
    }

    static String signingData(PublicKey sender, PublicKey recipient, long amount) {
        return Base64.getEncoder().encodeToString(sender.getEncoded())
            + "|" + Base64.getEncoder().encodeToString(recipient.getEncoded())
            + "|" + amount;
    }

    public boolean isValid() {
        return SignatureUtil.verify(signingData(this.sender, this.recipient, amount), this.signature, sender);
    }
}
