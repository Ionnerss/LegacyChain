package LegacyChain;

import java.util.ArrayList;

public class Blockchain {
    private ArrayList<Block> chain;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.chain.add(new Block("Genesis", "0"));
    }

    public Block addBlock(String data) {
        int index = chain.size() - 1;
        String lastHash = chain.get(index).hash;

        Block nBlock = new Block(data, lastHash);
        chain.add(nBlock);
        return nBlock;
    }

    public boolean isValid() {
        Block genesis = chain.get(0);
        if (!genesis.hash.equals(genesis.calculateHash())) return false;

        int i = 1;
        while (i < chain.size()) {
            Block currBlock = chain.get(i);
            Block prevBlock = chain.get(i - 1);

            if (!currBlock.hash.equals(currBlock.calculateHash()) ||
                !currBlock.previousHash.equals(prevBlock.hash))
                return false;
            
            i++;
        }
        return true;
    }
}
