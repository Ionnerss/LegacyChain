package LegacyChain;

import java.util.Date;

public class Block {
    public String hash;
	public String previousHash;
	private String data; //data will be a simple message.
	private long timeStamp; //as number of milliseconds since 1/1/1970.

	//Block Constructor.
	public Block(String data, String previousHash) {
		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
	}

    public String calculateHash() {
        String ts = Long.toString(this.timeStamp);
        String input = this.previousHash + ts + this.data;
        return HashUtil.sha256(input);
    }
}
