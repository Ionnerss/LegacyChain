package legacychain.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import legacychain.core.Blockchain;
import legacychain.persistence.snapshot.BlockchainSnapshot;
import legacychain.wallet.Wallet;

public class BlockchainSnapshotJsonCodecTest {
    @Test 
    void testBlockchainSnapshotJsonRoundTrip() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot serialized = blockchainMapper.toSnapshot(k);
        BlockchainSnapshotJsonCodec codec = new BlockchainSnapshotJsonCodec();
        String json = codec.toJson(serialized);
        assertTrue(json != null && !json.isBlank());

        BlockchainSnapshot deserialized = codec.fromJson(json);
        assertEquals(serialized, deserialized);

        Blockchain nBlockchain = blockchainMapper.fromSnapshot(deserialized);
        assertEquals(k.getDifficulty(), nBlockchain.getDifficulty());
        assertEquals(k.size(), nBlockchain.size());
        assertEquals(k.getPendingTransactions().size(), nBlockchain.getPendingTransactions().size());
        assertEquals(k.getLatestBlock().getMerkleRoot(), nBlockchain.getLatestBlock().getMerkleRoot());
        assertEquals(
            k.getPendingTransactions().getLast().getTransactionId(), 
            nBlockchain.getPendingTransactions().getLast().getTransactionId()
        );

    }
}
