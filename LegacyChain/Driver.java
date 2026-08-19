package LegacyChain;

public class Driver {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();

        blockchain.addBlock("Alice pays Bob 10 coins");
        blockchain.addBlock("Bob pays Carol 5 coins");
        blockchain.addBlock("Carol pays Dave 2 coins");
        blockchain.addBlock("Dave pays Eve 8 coins");
        blockchain.addBlock("Eve pays Frank 1 coin");

        System.out.println(blockchain.isValid());
    }
}
