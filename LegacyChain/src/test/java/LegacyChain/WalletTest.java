package LegacyChain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WalletTest {
    @Test
    void testWalletCreation() {
        assertDoesNotThrow(() -> new Wallet());
    }

    @Test
    void testPublicKeyIsNotNull() {
        Wallet w = new Wallet();
        assertNotNull(w.getPublicKey());
    }
    
    @Test
    void testTwoDifferentPublicKeys() {
        Wallet w = new Wallet(), j = new Wallet();
        assertNotEquals(w.getPublicKey(), j.getPublicKey());
    }

    @Test
    void testMultipleWalletKeysValidity() {
        for (int i = 0; i < 100; i++) {
            Wallet w = new Wallet();

            assertNotNull(w.getPublicKey());
            assertTrue(w.getPublicKey().getEncoded().length > 0);
        }
    }

    @Test
    void testSignatureAndPublicKeyVerif() {
        Wallet w = new Wallet();
        assertTrue(SignatureUtil.verify("hello", w.sign("hello"), w.getPublicKey()));
    }

    @Test
    void testSignatureAndDiffPublicKey() {
        Wallet w = new Wallet(), j = new Wallet();
        assertFalse(SignatureUtil.verify("hello", w.sign("hello"), j.getPublicKey()));
    }

    @Test
    void testSignatureAndDiffVerif() {
        Wallet w = new Wallet();
        assertFalse(SignatureUtil.verify("hell0", w.sign("hello"), w.getPublicKey()));
    }

    @Test
    void testSignatureNotNull() {
        Wallet w = new Wallet();
        assertNotNull(w.sign("hello"));
    }

    @Test
    void testSignatureContainsBytes() {
        Wallet w = new Wallet();
        assertTrue(w.sign("hello").length > 0);
    }

    @Test
    void testSREquals() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                Wallet w = new Wallet();
                Transaction t = w.createTransaction(w.getPublicKey(), 10, 0);
            });
    }
}
