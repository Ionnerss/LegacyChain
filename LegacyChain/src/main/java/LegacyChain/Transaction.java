package LegacyChain;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

public class Transaction {
    private final PublicKey sender;
    private final PublicKey recipient;
    private final long amount;
    private final byte[] signature;

    public Transaction(PublicKey sender, PublicKey recipient, long amount, byte[] signature) {
        if (sender == null || recipient == null || sender.equals(recipient))
            throw new IllegalArgumentException("Invalid transaction data.");
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid transaction data.");
        
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;

        if (signature == null || signature.length == 0)
            throw new IllegalArgumentException("Invalid signature data.");

        this.signature = Arrays.copyOf(signature, signature.length);
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
