package LegacyChain;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class SignatureUtil {
    public static boolean verify(String data, byte[] signature, PublicKey publicKey) {
        try {
            Signature sign = Signature.getInstance("Ed25519");
            sign.initVerify(publicKey);

            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            sign.update(bytes);

            return sign.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch(InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
    }
}
