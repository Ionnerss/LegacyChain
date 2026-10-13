package legacychain.persistence;

import legacychain.persistence.snapshot.BlockchainSnapshot;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class BlockchainSnapshotJsonCodec {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(BlockchainSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Invalid snapshot.");
        return objectMapper.writeValueAsString(snapshot);
    }

    public BlockchainSnapshot fromJson(String json) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Invalid JSON.");

        try {
            return objectMapper.readValue(json, BlockchainSnapshot.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

}
