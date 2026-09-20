package LegacyChain;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

public class Transaction {
    private final PublicKey sender;
    private final PublicKey recipient;
    private final long amount;
    private final byte[] signature;
    private final TransactionType type;
    private final int transactionNonce;
    private final String transactionId;
    private final int rewardHeight;

    //REWARD since every wallet starts at 0, must be able to mine
    enum TransactionType {
        NORMAL, REWARD
    }

    public Transaction(PublicKey sender, PublicKey recipient, long amount, byte[] signature, int transactionNonce) {
        if (recipient == null || amount <= 0 || sender == null || sender.equals(recipient))
                throw new IllegalArgumentException("Invalid transaction data.");

        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;

        if (signature == null || signature.length == 0)
                throw new IllegalArgumentException("Invalid transaction data.");
        this.signature = Arrays.copyOf(signature, signature.length);

        if (transactionNonce < 0)
            throw new IllegalArgumentException("Invalid transaction data.");
        this.transactionNonce = transactionNonce;

        this.rewardHeight = -1; //not applicable
        this.type = TransactionType.NORMAL;
        this.transactionId = calculateHash();
    }

    Transaction(PublicKey recipient, long amount, int rewardHeight) {
        if (recipient == null || amount <= 0)
            throw new IllegalArgumentException("Invalid transaction data.");

        this.recipient = recipient;
        this.amount = amount;
        this.sender = null;
        this.signature = null;

        if (rewardHeight <= 0)
            throw new IllegalArgumentException("Invalid transaction data.");
        this.rewardHeight = rewardHeight;

        this.transactionNonce = -1;
        this.type = TransactionType.REWARD;
        this.transactionId = calculateHash();
    }

    public PublicKey getSender() { return this.sender; }

    public PublicKey getRecipient() { return this.recipient; }

    public long getAmount() { return this.amount; }

    public TransactionType getType() { return this.type; }

    public int getTransactionNonce() { return this.transactionNonce; }

    public String getTransactionId() { return this.transactionId; }
    
    public int getRewardHeight() { return this.rewardHeight; }

    String calculateHash() {
        if (this.type == TransactionType.NORMAL) {
            String candidateHash = signingData(sender, recipient, amount, transactionNonce) + "|" + Base64.getEncoder().encodeToString(signature);
            return HashUtil.sha256(candidateHash);
        }
        else if (this.type == TransactionType.REWARD)
            return HashUtil.sha256(rewardData(recipient, amount, rewardHeight));
        else throw new IllegalArgumentException("Invalid transaction data.");
    }

    static String signingData(PublicKey sender, PublicKey recipient, long amount, int transactionNonce) {
        return TransactionType.NORMAL + "|" + Base64.getEncoder().encodeToString(sender.getEncoded())
            + "|" + Base64.getEncoder().encodeToString(recipient.getEncoded())
            + "|" + amount + "|" + transactionNonce;
    }

    static String rewardData(PublicKey recipient, long amount, int rewardHeight) {
        return TransactionType.REWARD + "|" + Base64.getEncoder().encodeToString(recipient.getEncoded())
            + "|" + amount + "|" + rewardHeight;
    }

    public boolean isValid() {
        if (!transactionId.equals(calculateHash())) return false;

        if (this.type == TransactionType.NORMAL)
            return SignatureUtil.verify(signingData(this.sender, this.recipient, this.amount, this.transactionNonce), this.signature, sender);
        else if (this.type == TransactionType.REWARD) {
            if (this.sender != null || this.recipient == null || amount <= 0 || this.signature != null || this.rewardHeight <= 0)
                return false;
            return true;
        }
        else return false;
    }
}
