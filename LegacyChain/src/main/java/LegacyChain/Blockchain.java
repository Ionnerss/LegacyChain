package LegacyChain;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import LegacyChain.Transaction.TransactionType;

public class Blockchain {
    private ArrayList<Block> chain;
    private int difficulty;
    private final int BLOCK_REWARD = 50;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();

        if (difficulty < 1 || difficulty > 64) 
            throw new IllegalArgumentException("Invalid difficulty setting.");
        this.difficulty = difficulty;

        Block genesis = new Block(new ArrayList<>(), "0");
        genesis.mineBlock(difficulty);
        this.chain.add(genesis);
    }

    public int size() { return chain.size(); }

    public Block getBlock(int index) { return chain.get(index); }

    public Block getLatestBlock() { return chain.get(chain.size() - 1); }

    public Block addBlock(List<Transaction> transactions, PublicKey miner) {
        if (transactions == null) throw new IllegalArgumentException("Invalid transactions list.");
        if (miner == null) throw new IllegalArgumentException("Invalid miner key.");
        
        for (Transaction t : transactions) {
            if (t == null) throw new IllegalArgumentException("Invalid transaction in block.");
            else if (t.getType() == TransactionType.REWARD) throw new IllegalArgumentException("Invalid transaction in block.");
            else if (!t.isValid()) throw new IllegalArgumentException("Invalid transaction in block.");
        }

        int index = chain.size() - 1;
        String lastHash = chain.get(index).getHash();

        List<Transaction> tCopy = new ArrayList<>(transactions);
        Transaction minerRewardTrans = new Transaction(miner, BLOCK_REWARD);
        tCopy.add(minerRewardTrans);

        Block nBlock = new Block(tCopy, lastHash);
        nBlock.mineBlock(difficulty);
        chain.add(nBlock);
        return nBlock;
    }

    public boolean isValid() {
        String target = "0".repeat(difficulty);
        Block genesis = chain.get(0);
        if (!genesis.getHash().equals(genesis.calculateHash()) 
            || !genesis.getHash().startsWith(target)
            || !genesis.getTransactions().isEmpty()
            || !"0".equals(genesis.getPreviousHash()))
            return false;

        int i = 1;
        while (i < chain.size()) {
            Block currBlock = chain.get(i);
            Block prevBlock = chain.get(i - 1);

            /*
            - !currBlock.hash.equals(currBlock.calculateHash() checks if data has been modified | protects block's own contents
            - !currBlock.previousHash.equals(prevBlock.hash) checks if full block has been modified and rehashed | protects link to prev block
            */
            if (!currBlock.getHash().equals(currBlock.calculateHash()) 
                || !currBlock.getPreviousHash().equals(prevBlock.getHash()) 
                || !currBlock.getHash().startsWith(target))
                return false;

            for (Transaction t : currBlock.getTransactions()) {
                if (t == null) return false;
                else if (t.getType() == TransactionType.NORMAL) 
                    if (!t.isValid()) return false;
                else if (t.getType() == TransactionType.REWARD)
                    if (currBlock.getTransactions().indexOf(t) != currBlock.getTransactions().size() - 1
                        || t.getAmount() != BLOCK_REWARD)
                        return false;
                else return false;
            }

            i++;
        }
        return true;
    }

    public long getBalance(PublicKey owner) {
        if (owner == null) throw new IllegalArgumentException("Invalid public key.");
        long balance = 0;

        for (Block b : chain) {
            for (Transaction t : b.getTransactions()) {
                if (t.getType() == TransactionType.REWARD)
                    if (t.getRecipient().equals(owner))
                        balance += t.getAmount();
                else if (t.getType() == TransactionType.NORMAL) {
                    if (t.getSender().equals(owner))
                        balance -= t.getAmount();
                    if (t.getRecipient().equals(owner))
                        balance += t.getAmount();
                }
            }
        }
        return balance;  
    }
}