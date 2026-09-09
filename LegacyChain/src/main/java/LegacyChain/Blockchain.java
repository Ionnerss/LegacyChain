package LegacyChain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private ArrayList<Block> chain;
    private int difficulty;

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

    public Block addBlock(List<Transaction> transactions) {
        if (transactions == null) throw new IllegalArgumentException("Invalid transactions list.");
        
        for (Transaction t : transactions) {
            if (t == null) throw new IllegalArgumentException("Invalid transaction in block.");
            else if (!t.isValid()) throw new IllegalArgumentException("Invalid transaction in block.");
            
        }

        int index = chain.size() - 1;
        String lastHash = chain.get(index).getHash();

        Block nBlock = new Block(transactions, lastHash);
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
            || !genesis.getPreviousHash().equals("0")) 
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
            
            for (Transaction t : currBlock.getTransactions())
                if (t == null || !t.isValid()) return false;

            i++;
        }
        return true;
    }
}