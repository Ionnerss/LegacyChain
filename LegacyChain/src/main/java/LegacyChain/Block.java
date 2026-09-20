package LegacyChain;

import java.util.Date;
import java.util.List;

public class Block {
    private String hash;
	private String previousHash;
	private List<Transaction> transactions;
	private long timeStamp; //as number of milliseconds since 1/1/1970.
	private long nonce;
	private final int height;
	private String merkleRoot;

	//Block Constructor.
	public Block(List<Transaction> transactions, String previousHash, int height) {
		if (transactions.contains(null))
			throw new IllegalArgumentException("Invalid transactions data.");

		this.transactions = List.copyOf(transactions);
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.nonce = 0;
        this.hash = calculateHash();
		this.merkleRoot = MerkleUtil.calculateMerkleRoot(transactions);
		this.height = height;
	}

	public String getHash() { return this.hash; }

	public String getPreviousHash() { return this.previousHash; }

	public List<Transaction> getTransactions() { return this.transactions; }

	public long getTimeStamp() { return this.timeStamp; }

	public long getNonce() { return this.nonce; }

	public int getHeight() { return this.height; }

	public String getMerkleRoot() { return this.merkleRoot; }

    String calculateHash() {
        String ts = Long.toString(this.timeStamp);

        String input = this.previousHash + ts + this.height + this.merkleRoot + this.nonce;
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
