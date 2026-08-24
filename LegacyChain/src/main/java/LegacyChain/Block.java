package LegacyChain;

import java.util.Date;
import java.util.List;

public class Block {
    private String hash;
	private String previousHash;
	private List<Transaction> transactions;
	private long timeStamp; //as number of milliseconds since 1/1/1970.
	private long nonce;

	//Block Constructor.
	public Block(List<Transaction> transactions, String previousHash) {
		if (transactions.contains(null))
			throw new IllegalArgumentException("Invalid transactions data.");

		this.transactions = List.copyOf(transactions);
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.nonce = 0;
        this.hash = calculateHash();
	}

	public String getHash() { return this.hash; }

	public String getPreviousHash() { return this.previousHash; }

	public List<Transaction> getTransactions() { return this.transactions; }

	public long getTimeStamp() { return this.timeStamp; }

	public long getNonce() { return this.nonce; }

    String calculateHash() {
        String ts = Long.toString(this.timeStamp);

		StringBuilder sb = new StringBuilder();
		for (Transaction t : transactions) {
			sb.append(t.calculateHash());
		}

        String input = this.previousHash + ts + sb.toString() + this.nonce;
        return HashUtil.sha256(input);
    }

	void mineBlock(int difficulty) {
		String candidateHash = calculateHash(), start = "0".repeat(difficulty);

		while (!candidateHash.startsWith(start)) {
			this.nonce++;
			candidateHash = calculateHash();
		}
		this.hash = candidateHash;
	}
}
