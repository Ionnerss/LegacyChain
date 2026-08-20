package LegacyChain;

import java.util.ArrayList;

public class Blockchain {
    private ArrayList<Block> chain;
    private int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();

        if (difficulty < 1 || difficulty > 64) 
            throw new IllegalArgumentException("Invalid difficulty setting.");
        this.difficulty = difficulty;

        Block genesis = new Block("Genesis", "0");
        genesis.mineBlock(difficulty);
        this.chain.add(genesis);
    }

    public Block addBlock(String data) {
        int index = chain.size() - 1;
        String lastHash = chain.get(index).hash;

        Block nBlock = new Block(data, lastHash);
        nBlock.mineBlock(difficulty);
        chain.add(nBlock);
        return nBlock;
    }

    public boolean isValid() {
        String target = "0".repeat(difficulty);
        Block genesis = chain.get(0);
        if (!genesis.hash.equals(genesis.calculateHash()) 
            || !genesis.hash.startsWith(target)) 
            return false;

        int i = 1;
        while (i < chain.size()) {
            Block currBlock = chain.get(i);
            Block prevBlock = chain.get(i - 1);

            /*
            - !currBlock.hash.equals(currBlock.calculateHash() checks if data has been modified | protects block's own contents
            - !currBlock.previousHash.equals(prevBlock.hash) checks if full block has been modified and rehashed | protects link to prev block
            */
            if (!currBlock.hash.equals(currBlock.calculateHash()) 
                || !currBlock.previousHash.equals(prevBlock.hash) 
                || !currBlock.hash.startsWith(target))
                return false;
            
            i++;
        }
        return true;
    }
}
