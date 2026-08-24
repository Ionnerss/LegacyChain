package LegacyChain;

public class Transaction {
    private final String sender;
    private final String recipient;
    private final long amount;

    public Transaction(String sender, String recipient, long amount) {
        if (sender == null || sender.isBlank() || recipient == null 
            || recipient.isBlank() || sender.equals(recipient))
            throw new IllegalArgumentException("Invalid transaction data.");
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid transaction data.");
        
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    public String getSender() { return this.sender; }

    public String getRecipient() { return this.recipient; }

    public long getAmount() { return this.amount; }

    String calculateHash() {
        String candidateHash = this.sender.length() + this.sender + this.recipient.length() + this.recipient + this.amount;
        return HashUtil.sha256(candidateHash);
    }
}
