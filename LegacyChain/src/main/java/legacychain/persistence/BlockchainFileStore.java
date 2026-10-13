package legacychain.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import legacychain.persistence.snapshot.BlockchainSnapshot;

public class BlockchainFileStore {
    private static final BlockchainSnapshotJsonCodec codec = new BlockchainSnapshotJsonCodec();

    public static void save(BlockchainSnapshot snapshot, Path path) throws IOException {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");
        if (path == null) throw new IllegalArgumentException("Invalid path.");

        String json = codec.toJson(snapshot);
        Files.writeString(path, json);
    }

    public static BlockchainSnapshot load(Path path) throws IOException {
        if (path == null) throw new IllegalArgumentException("Invalid path.");

        String json = Files.readString(path);
        return codec.fromJson(json);
    }
}
