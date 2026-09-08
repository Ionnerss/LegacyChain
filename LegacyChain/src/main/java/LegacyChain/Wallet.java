package LegacyChain;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Wallet {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Wallet() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            var keyPair  = kpg.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public PublicKey getPublicKey() { return this.publicKey; }

    public byte[] sign(String data) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);

            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            signature.update(bytes);
            return signature.sign();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    public Transaction createTransaction(PublicKey recipient, long amount) {
        if (recipient == null || recipient == publicKey)
            throw new IllegalArgumentException("Invalid recipient key.");
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid transaction amount.");

        String data = Transaction.signingData(this.publicKey, recipient, amount);
        byte[] signedData = sign(data);

        return new Transaction(this.publicKey, recipient, amount, signedData);
    }
}
