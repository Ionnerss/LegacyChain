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
    enum TransactionType {
        NORMAL, REWARD
    }

    public Transaction(PublicKey sender, PublicKey recipient, long amount, byte[] signature) {
        if (recipient == null || amount <= 0 || sender == null || sender.equals(recipient))
                throw new IllegalArgumentException("Invalid transaction data.");

        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;

        if (signature == null || signature.length == 0)
                throw new IllegalArgumentException("Invalid signature data.");
    
        this.signature = Arrays.copyOf(signature, signature.length);
        this.type = TransactionType.NORMAL;
    }

    Transaction(PublicKey recipient, long amount) {
        if (recipient == null || amount <= 0)
            throw new IllegalArgumentException("Invalid transaction data.");

        this.recipient = recipient;
        this.amount = amount;
        this.sender = null;
        this.signature = null;
        this.type = TransactionType.REWARD;
    }

    public PublicKey getSender() { return this.sender; }

    public PublicKey getRecipient() { return this.recipient; }

    public long getAmount() { return this.amount; }

    public TransactionType getType() { return this.type; }

    String calculateHash() {
        if (this.type == TransactionType.NORMAL) {
            String candidateHash = signingData(sender, recipient, amount) + "|" + Base64.getEncoder().encodeToString(signature);
            return HashUtil.sha256(candidateHash);
        }
        else
            return HashUtil.sha256(rewardData(recipient, amount));
    }

    static String signingData(PublicKey sender, PublicKey recipient, long amount) {
        return TransactionType.NORMAL + "|" + Base64.getEncoder().encodeToString(sender.getEncoded())
            + "|" + Base64.getEncoder().encodeToString(recipient.getEncoded())
            + "|" + amount;
    }

    static String rewardData(PublicKey recipient, long amount) {
        return TransactionType.REWARD + "|" + Base64.getEncoder().encodeToString(recipient.getEncoded())
            + "|" + amount;
    }

    public boolean isValid() {
        if (this.type == TransactionType.NORMAL)
            return SignatureUtil.verify(signingData(this.sender, this.recipient, amount), this.signature, sender);
        else if (this.type == TransactionType.REWARD) {
            if (this.sender != null || this.recipient == null || amount <= 0 || this.signature != null)
                return false;
            return true;
        }
        else return false;
    }
}
