package legacychain.persistence;

import legacychain.persistence.snapshot.BlockchainSnapshot;
import tools.jackson.databind.ObjectMapper;

public class BlockchainSnapshotJsonCodec {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(BlockchainSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");
        return objectMapper.writeValueAsString(snapshot);
    }

    public BlockchainSnapshot fromJson(String json) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Invalid JSON.");
        return objectMapper.readValue(json, BlockchainSnapshot.class);
    }

}
