package LegacyChain;

public class Driver {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain(3);

        Block k = blockchain.addBlock("Alice pays Bob 10 coins");
        System.out.println("Block k hash: " + k.hash);

        Block l = blockchain.addBlock("Charlie pays Dana 5 coins");
        System.out.println("Block l hash: " + l.hash);

        Block m = blockchain.addBlock("Eve pays Frank 7 coins");
        System.out.println("Block m hash: " + m.hash);

        Block n = blockchain.addBlock("Grace pays Heidi 2 coins");
        System.out.println("Block n hash: " + n.hash);

        Block o = blockchain.addBlock("Ivan pays Judy 9 coins");
        System.out.println("Block o hash: " + o.hash);

        System.out.println("Validity: " + blockchain.isValid());
    }
}
