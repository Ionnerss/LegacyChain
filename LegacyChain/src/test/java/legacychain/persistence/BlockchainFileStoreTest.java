package legacychain.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import legacychain.core.Blockchain;
import legacychain.core.Transaction;
import legacychain.core.TransactionType;
import legacychain.persistence.snapshot.BlockchainSnapshot;
import legacychain.persistence.snapshot.TransactionSnapshot;
import legacychain.persistence.snapshot.BlockSnapshot;
import legacychain.wallet.Wallet;

public class BlockchainFileStoreTest {
    @TempDir 
    Path tempDir;

    @Test 
    void testSaveWritesSnapshotJsonToFile() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        Path path = tempDir.resolve("blockchain.json");

        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot bcSnapshot = blockchainMapper.toSnapshot(k);
        assertDoesNotThrow(() -> {BlockchainFileStore.save(bcSnapshot, path);});
        assertTrue(Files.exists(path));

        String contents = assertDoesNotThrow(() -> Files.readString(path));
        assertTrue(!contents.isBlank());
    }

    @Test
    void testSaveAndLoadPreservesSnapshot() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        Path path = tempDir.resolve("blockchain.json");

        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot ogSnapshot = blockchainMapper.toSnapshot(k);
        assertDoesNotThrow(() -> {BlockchainFileStore.save(ogSnapshot, path);});
        assertTrue(Files.exists(path));

        BlockchainSnapshot loadedSnapshot = assertDoesNotThrow(() -> BlockchainFileStore.load(path));
        assertEquals(ogSnapshot, loadedSnapshot);
    }

    @Test 
    void testSavedBlockchainCanBeLoadedAndContinueMining() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        Path path = tempDir.resolve("blockchain.json");

        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot ogSnapshot = blockchainMapper.toSnapshot(k);
        assertDoesNotThrow(() -> {BlockchainFileStore.save(ogSnapshot, path);});
        assertTrue(Files.exists(path));

        BlockchainSnapshot loadedSnapshot = assertDoesNotThrow(() -> BlockchainFileStore.load(path));
        Blockchain loadedBlockchain = blockchainMapper.fromSnapshot(loadedSnapshot);

        assertEquals(k.getDifficulty(), loadedBlockchain.getDifficulty());
        assertEquals(k.size(), loadedBlockchain.size());
        assertEquals(k.getPendingTransactions().size(), loadedBlockchain.getPendingTransactions().size());
        
        for (int i = 0; i < k.size(); i++) {
            assertEquals(k.getBlock(i).getHash(), loadedBlockchain.getBlock(i).getHash());
        }
        for (int i = 0; i < k.getPendingTransactions().size(); i++) {
            assertEquals(
                k.getPendingTransactions().get(i).getTransactionId(), 
                loadedBlockchain.getPendingTransactions().get(i).getTransactionId()
            );
        }
        for (int i = 0; i < k.size(); i++) {
            for (int h = 0; h < k.getBlock(i).getTransactions().size(); h++) {
                if (k.getBlock(i).getTransactions().get(h).getType() != TransactionType.REWARD) {
                    assertEquals(
                        k.getBalance(k.getBlock(i).getTransactions().get(h).getSender()),
                        loadedBlockchain.getBalance(loadedBlockchain.getBlock(i).getTransactions().get(h).getSender())
                    );
                    assertEquals(
                        k.getNextNonce(k.getBlock(i).getTransactions().get(h).getSender()),
                        loadedBlockchain.getNextNonce(loadedBlockchain.getBlock(i).getTransactions().get(h).getSender())
                    );
                }
                assertEquals(
                    k.getBalance(k.getBlock(i).getTransactions().get(h).getRecipient()),
                    loadedBlockchain.getBalance(loadedBlockchain.getBlock(i).getTransactions().get(h).getRecipient())
                );
            }
        }
        assertTrue(loadedBlockchain.isValid());

        Transaction t = loadedBlockchain.getPendingTransactions().getLast();
        loadedBlockchain.minePendingTransactions(new Wallet().getPublicKey());
        assertEquals(0, loadedBlockchain.getPendingTransactions().size());
        assertEquals(k.size() + 1, loadedBlockchain.size());
        assertEquals(k.getNextNonce(w.getPublicKey()) + 1, loadedBlockchain.getNextNonce(w.getPublicKey()));
        assertEquals(t.getTransactionId(), loadedBlockchain.getLatestBlock().getTransactions().getFirst().getTransactionId());
        assertTrue(loadedBlockchain.isValid());
    }

    @Test 
    void testLoadMissingFileThrowsIOException() {
        Path path = tempDir.resolve("missing.json");
        assertTrue(Files.notExists(path));
        assertThrows(IOException.class, () -> {BlockchainFileStore.load(path);});
    }

    @Test 
    void testLoadMalformedJsonRejected() {
        Path path = tempDir.resolve("malformed.json");
        assertDoesNotThrow(() -> Files.writeString(path, "{ definitely not valid json")); 
        assertThrows(IllegalArgumentException.class, () -> {BlockchainFileStore.load(path);});
    }

    @Test 
    void testLoadedSnapshotWithBrokenChainLinkRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        Path path = tempDir.resolve("blockchain.json");

        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 20, 1));

        BlockchainMapper blockchainMapper = new BlockchainMapper();
        BlockchainSnapshot ogSnapshot = blockchainMapper.toSnapshot(k);
        List<BlockSnapshot> chain = ogSnapshot.chain();
        BlockSnapshot ogBlock = ogSnapshot.chain().get(1);
        BlockSnapshot moddedBlock = new BlockSnapshot(
            "corrupted", 
            ogBlock.transactions(), 
            ogBlock.timeStamp(), ogBlock.nonce(), 
            ogBlock.height()
        );
        ArrayList<BlockSnapshot> nModList = new ArrayList<>(chain);
        nModList.set(1, moddedBlock);
        BlockchainSnapshot moddedBlockchainSnapshot = new BlockchainSnapshot(
            ogSnapshot.difficulty(), 
            nModList, 
            ogSnapshot.pendingTransactions()
        );

        assertDoesNotThrow(() -> BlockchainFileStore.save(moddedBlockchainSnapshot, path));
        assertTrue(Files.exists(path));
        BlockchainSnapshot newK = assertDoesNotThrow(() -> BlockchainFileStore.load(path));
        assertThrows(IllegalArgumentException.class, () -> blockchainMapper.fromSnapshot(newK));
    }

    @Test 
    void testLoadedSnapshotWithInvalidPendingTransactionRejected() {
        Blockchain k = new Blockchain(2);
        Wallet w = new Wallet(), j = new Wallet();
        Path path = tempDir.resolve("blockchain.json");

        k.minePendingTransactions(w.getPublicKey());
        k.submitTransaction(w.createTransaction(j.getPublicKey(), 15, 0));
        k.minePendingTransactions(new Wallet().getPublicKey());
        Transaction invalTransaction = w.createTransaction(new Wallet().getPublicKey(), 20, 2);
        TransactionMapper tm = new TransactionMapper();
        TransactionSnapshot ts = tm.toSnapshot(invalTransaction);

        BlockchainMapper bm = new BlockchainMapper();
        List<BlockSnapshot> chain = bm.toSnapshot(k).chain();
        BlockchainSnapshot blockchainSnapshot = new BlockchainSnapshot(
            k.getDifficulty(),
            chain, 
            List.of(ts)
        );
        assertDoesNotThrow(() -> BlockchainFileStore.save(blockchainSnapshot, path));
        assertTrue(Files.exists(path));
        BlockchainSnapshot loadedBlockchainSnapshot = assertDoesNotThrow(() -> BlockchainFileStore.load(path));
        assertThrows(IllegalArgumentException.class, () -> bm.fromSnapshot(loadedBlockchainSnapshot));
    }
}
