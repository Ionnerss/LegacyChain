package LegacyChain;

import java.util.Date;

public class Block {
    private String hash;
	private String previousHash;
	private String data; //data will be a simple message.
	private long timeStamp; //as number of milliseconds since 1/1/1970.
	private long nonce;

	//Block Constructor.
	public Block(String data, String previousHash) {
		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.nonce = 0;
        this.hash = calculateHash();
	}

	public String getHash() { return this.hash; }

	public String getPreviousHash() { return this.previousHash; }

	public String getData() { return this.data; }

	public long getTimeStamp() { return this.timeStamp; }

	public long getNonce() { return this.nonce; }

    String calculateHash() {
        String ts = Long.toString(this.timeStamp);
        String input = this.previousHash + ts + this.data + this.nonce;
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
