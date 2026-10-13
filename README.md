# LegacyChain

> **Status: In Progress** — LegacyChain is an educational Java proof-of-work blockchain built from scratch to explore blockchain internals through explicit, test-driven implementations rather than a production-ready cryptocurrency stack.

LegacyChain began as a small blockchain learning exercise and has grown into a structured single-process ledger with proof of work, signed transactions, deterministic transaction IDs, replay protection, historical state validation, Merkle roots and proofs, a mempool, mining rewards, and JSON file persistence.

The project intentionally keeps the core mechanics visible. Balances are reconstructed from chain history, nonces are replayed rather than stored as authoritative account state, pending transactions are validated in order, blocks commit to transaction IDs through a Merkle root, and persisted state is rebuilt into validated domain objects rather than blindly trusted after deserialization.

The project is still actively evolving. The next feature phase is **wallet addresses**, followed by wallet/key persistence and additional usability, querying, and networking work.

---

## Table of Contents

- [Project Goals](#project-goals)
- [Current Feature Set](#current-feature-set)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Core Ledger Model](#core-ledger-model)
- [Transactions](#transactions)
- [Wallets and Signatures](#wallets-and-signatures)
- [Balances and Nonces](#balances-and-nonces)
- [Mempool and Mining](#mempool-and-mining)
- [Blocks and Proof of Work](#blocks-and-proof-of-work)
- [Merkle Trees and Proofs](#merkle-trees-and-proofs)
- [Blockchain Validation](#blockchain-validation)
- [Persistence](#persistence)
- [API Guide and Important Traps](#api-guide-and-important-traps)
- [Configuration](#configuration)
- [Building and Testing](#building-and-testing)
- [Development and Testing Philosophy](#development-and-testing-philosophy)
- [Current Limitations](#current-limitations)
- [Roadmap](#roadmap)
- [Security and Production Disclaimer](#security-and-production-disclaimer)

---

## Project Goals

LegacyChain is primarily a learning and portfolio project. Its purpose is to make the mechanics of a blockchain explicit enough to reason about, test, and explain.

The current design emphasizes:

- deterministic hashing and transaction identity;
- Ed25519 public/private-key signatures;
- block hash chaining;
- proof-of-work mining;
- transaction replay protection through sender nonces;
- balance reconstruction from confirmed chain history;
- historical validation instead of trusting cached state;
- Merkle commitments and inclusion proofs;
- separation between transaction submission and mining;
- ordered pending-transaction validation;
- persistence through typed snapshots, JSON serialization, and file storage;
- defensive copying and immutable views at important boundaries;
- targeted JUnit regression tests for normal and adversarial cases.

LegacyChain is currently a **single-process, account/balance-replay blockchain**. It is not a UTXO implementation and does not yet implement distributed consensus, peer-to-peer networking, chain reorganization, or production-grade storage.

---

## Current Feature Set

### Implemented

- SHA-256 hashing
- Linked blocks through `previousHash`
- Genesis block creation and validation
- Configurable proof-of-work difficulty
- Block heights
- Multiple transactions per block
- Immutable/defensive transaction and block data exposure
- Ed25519 wallet keypairs
- Ed25519 transaction signing and verification
- `NORMAL` and `REWARD` transaction types
- Deterministic transaction IDs
- Reward-height binding for mining-reward transaction IDs
- Fixed mining reward
- Historical balance replay
- Overspending prevention
- Sender nonces and replay protection
- Historical nonce validation
- Ordered mempool / pending transactions
- Duplicate pending-transaction rejection
- Pending-balance and pending-nonce validation
- Pending dependency handling in insertion order
- Separate transaction submission and mining
- Mining of all currently pending transactions
- Reward-only mining when the mempool is empty
- Merkle roots
- Merkle trees
- Merkle inclusion proof generation and verification
- Persistence snapshots for transactions, blocks, and blockchain state
- JSON snapshot serialization with Jackson
- JSON file save/load
- Blockchain restoration with full validation
- Mempool restoration through normal submission validation
- Regression tests for malformed JSON, missing files, broken chain links, and invalid restored pending state

### In Progress

- **Wallet addresses** derived from public keys

### Planned

- Wallet/private-key persistence
- Transaction and block lookup APIs
- CLI and/or REST interface
- Block explorer
- CI automation and expanded project documentation
- Finite block capacity
- Transaction fees
- Miner transaction-selection policy
- Fee/priority-based pending transaction ordering
- Dynamic difficulty
- Reward schedule / halving-style issuance rules
- Peer-to-peer networking
- Multiple nodes
- Chain synchronization
- Cumulative-work chain selection
- Reorganization logic
- Distributed consensus behavior

---

## Architecture

At a high level, the transaction lifecycle is:

```text
Wallet
  |
  | createTransaction(...)
  v
Signed NORMAL Transaction
  |
  | Blockchain.submitTransaction(...)
  v
Ordered Mempool
  |
  | Blockchain.minePendingTransactions(miner)
  v
Block + automatic REWARD transaction
  |
  v
Confirmed Blockchain
```

Persistence is deliberately separated from the live domain model:

```text
Blockchain
    |
    | BlockchainMapper.toSnapshot(...)
    v
BlockchainSnapshot
    |
    | BlockchainSnapshotJsonCodec.toJson(...)
    v
JSON String
    |
    | BlockchainFileStore.save(...)
    v
Filesystem

Filesystem
    |
    | BlockchainFileStore.load(...)
    v
BlockchainSnapshot
    |
    | BlockchainMapper.fromSnapshot(...)
    v
Validated Blockchain
```

The important design principle is that deserialization does **not** bypass ledger rules. Restored blocks are reconstructed, the confirmed chain is validated, and restored pending transactions are passed back through normal mempool submission rules.

---

## Project Structure

The repository currently contains the Maven project inside the `LegacyChain/` directory:

```text
LegacyChain/
├── pom.xml
└── src/
    ├── main/java/legacychain/
    │   ├── core/
    │   │   ├── Block.java
    │   │   ├── Blockchain.java
    │   │   ├── Transaction.java
    │   │   └── TransactionType.java
    │   ├── crypto/
    │   │   ├── HashUtil.java
    │   │   └── SignatureUtil.java
    │   ├── merkle/
    │   │   └── MerkleUtil.java
    │   ├── persistence/
    │   │   ├── TransactionMapper.java
    │   │   ├── BlockMapper.java
    │   │   ├── BlockchainMapper.java
    │   │   ├── BlockchainSnapshotJsonCodec.java
    │   │   ├── BlockchainFileStore.java
    │   │   └── snapshot/
    │   │       ├── TransactionSnapshot.java
    │   │       ├── BlockSnapshot.java
    │   │       └── BlockchainSnapshot.java
    │   └── wallet/
    │       └── Wallet.java
    └── test/java/legacychain/
        ├── core/
        ├── merkle/
        ├── persistence/
        └── wallet/
```

There is currently no production CLI or REST entry point. The project is primarily exercised through its domain APIs and JUnit tests.

---

## Core Ledger Model

LegacyChain uses a **balance-replay model**, not Bitcoin-style UTXOs.

There is no authoritative mutable balance table stored as blockchain state. Instead, `Blockchain.getBalance(PublicKey)` scans confirmed blocks and reconstructs the requested balance by replaying transactions:

- a `REWARD` transaction credits its recipient;
- a `NORMAL` transaction debits its sender;
- a `NORMAL` transaction credits its recipient.

This approach is intentionally simple and makes the ledger state derivable from history.

Confirmed sender nonces follow the same philosophy. `getNextNonce(PublicKey)` scans confirmed `NORMAL` transactions and counts the sender's committed transactions.

This matters because confirmed state and pending state are deliberately different concepts.

---

## Transactions

LegacyChain supports two transaction types:

```text
NORMAL
REWARD
```

### NORMAL transactions

A normal transaction contains:

- sender `PublicKey`;
- recipient `PublicKey`;
- positive amount;
- Ed25519 signature;
- non-negative sender nonce;
- deterministic transaction ID;
- type `NORMAL`;
- reward height sentinel of `-1` internally.

A wallet signs canonical transaction data containing:

```text
NORMAL
| Base64(sender public key)
| Base64(recipient public key)
| amount
| nonce
```

The transaction ID is then derived from the signing data plus the Base64-encoded signature.

Consequences:

- changing the sender changes the ID;
- changing the recipient changes the ID;
- changing the amount changes the ID;
- changing the nonce changes the ID;
- changing the signature changes the ID;
- identical signed transaction data deterministically reproduces the same ID.

`Transaction.isValid()` recomputes the stored transaction ID and then verifies the Ed25519 signature for `NORMAL` transactions.

### REWARD transactions

Reward transactions are structurally different:

- sender is `null`;
- signature is `null`;
- nonce is not applicable;
- recipient is the miner;
- amount is the fixed block reward;
- `rewardHeight` identifies the block receiving the reward;
- type is `REWARD`.

The reward transaction ID includes the recipient, reward amount, and reward height. The height prevents identical rewards to the same miner at different blocks from collapsing to the same transaction ID.

Reward transactions are created by the mining path. Although restoration support exists through `Transaction.restoreReward(...)`, normal mempool submission explicitly rejects reward transactions.

---

## Wallets and Signatures

Each `Wallet` currently creates a fresh Ed25519 keypair at construction time.

```text
new Wallet()
    -> new private key
    -> new public key
    -> new identity
```

The wallet exposes its public key but keeps the private key internal.

`Wallet.createTransaction(...)`:

1. validates the recipient and amount;
2. builds the canonical signing data;
3. signs it using the wallet's private Ed25519 key;
4. creates a `NORMAL` transaction containing the signature and caller-supplied nonce.

`SignatureUtil.verify(...)` performs the corresponding Ed25519 verification.

### Important current limitation

Blockchain persistence does **not** persist wallet private keys.

A loaded blockchain still contains the historical public keys embedded in transactions, but creating a new `Wallet()` after restart creates a completely new identity. A user cannot recover the ability to spend as an old wallet without the original private key.

Dedicated wallet/key persistence is therefore a separate planned feature.

---

## Balances and Nonces

### Confirmed balance

`Blockchain.getBalance(...)` reflects only confirmed blocks.

Pending transactions do not directly change the result of `getBalance(...)`.

### Confirmed next nonce

`Blockchain.getNextNonce(...)` also reflects only confirmed transactions.

This is intentional.

For example, if a sender has confirmed nonce history ending at `0`, then:

```text
getNextNonce(sender) == 1
```

Even if that sender already has a pending transaction with nonce `1`, `getNextNonce(sender)` remains `1` until the transaction is mined.

Pending nonce adjustment happens inside mempool validation instead of changing the meaning of `getNextNonce(...)`.

---

## Mempool and Mining

Pending transactions are stored in insertion order.

The current architecture is:

```text
create transaction
-> submit transaction
-> pending transaction list
-> mine pending transactions
-> confirmed block
```

### Submission validation

`Blockchain.submitTransaction(...)` rejects:

- `null` transactions;
- reward transactions;
- cryptographically invalid transactions;
- duplicate pending transaction IDs;
- old nonces;
- skipped nonces;
- pending overspending.

It starts from the sender's confirmed balance and confirmed next nonce, then replays the existing pending list in order to derive the sender's effective pending state.

If an earlier pending transaction sends funds **to** the new transaction's sender, those funds can become available to the later transaction.

If an earlier pending transaction spends funds **from** the same sender, the available pending balance decreases and the expected pending nonce advances.

This means ordering matters.

Example:

```text
confirmed: Alice = 50, Bob = 0

pending #1: Alice -> Bob 30
pending #2: Bob   -> Carol 20
```

The second transaction can be valid because Bob's incoming funds appear earlier in the pending order.

The reverse order would not be valid because Bob would try to spend before the incoming pending transfer exists.

### Mining

`minePendingTransactions(miner)` currently:

1. snapshots the current pending list;
2. validates the transactions again while building the block;
3. appends one automatic reward transaction;
4. mines the block;
5. appends it to the chain;
6. clears the mempool only after successful block creation.

All current pending transactions are mined together. Finite block capacity and miner transaction selection are future features.

### Empty mempool behavior

Mining with an empty mempool is allowed. It creates a reward-only block.

This behavior is useful for funding wallets during tests and models a miner producing an otherwise empty block.

---

## Blocks and Proof of Work

A block contains:

- stored hash;
- previous block hash;
- immutable transaction list;
- timestamp;
- nonce;
- height;
- Merkle root.

The block hash is calculated from the current block-header-style state:

```text
previousHash + timestamp + height + merkleRoot + nonce
```

and then SHA-256 hashed.

Transactions are committed indirectly through the Merkle root.

### Proof of work

Mining requires the hexadecimal block hash to begin with a number of zero characters equal to the configured difficulty:

```text
difficulty = 2
hash must start with "00"
```

Mining increments the block nonce until the target is reached.

Because each hexadecimal character has 16 possible values, increasing difficulty by one represents roughly another factor of 16 in expected search work.

Difficulty is validated in the range `1..64`.

### Genesis block

Constructing a new `Blockchain` immediately creates and mines Genesis.

Genesis conventions are:

- chain index `0`;
- height `0`;
- previous hash `"0"`;
- empty transaction list;
- empty-list Merkle root;
- valid proof of work for the configured difficulty.

A newly constructed blockchain is therefore not empty.

---

## Merkle Trees and Proofs

Blocks commit to their ordered transaction IDs through a Merkle root.

Current conventions are:

### Empty transaction list

```text
Merkle root = SHA256("")
```

### One transaction

```text
Merkle root = transaction ID
```

The leaf is not hashed again.

### Two child nodes

```text
parent = SHA256(leftHash + rightHash)
```

### Odd node count

The final node is duplicated before computing its parent.

<img width="2000" height="992" alt="image" src="https://github.com/user-attachments/assets/1fc6f3f6-f3d0-4e35-9b5f-ead978968373" />


LegacyChain also implements Merkle inclusion proof generation and verification using sibling hashes plus left/right position metadata.

At the current API boundary, `calculateMerkleRoot(...)` is public, while proof generation/verification helpers remain package-level implementation APIs used by the Merkle package/tests.

---

## Blockchain Validation

`Blockchain.isValid()` does substantially more than verify that hashes point to each other.

The chain is replayed from Genesis forward.

<img width="3200" height="656" alt="image" src="https://github.com/user-attachments/assets/6fd8a399-ea29-477b-9bfd-d4b85fb61e49" />

### Genesis validation

Validation checks that Genesis:

- has a reproducible stored hash;
- satisfies proof of work;
- contains no transactions;
- points to previous hash `"0"`;
- has height `0`;
- has the expected empty Merkle root.

### Every later block

Validation checks:

- stored block hash equals recalculated block hash;
- `previousHash` equals the actual prior block hash;
- proof-of-work target is satisfied;
- stored block height matches its chain index;
- stored Merkle root matches the block transaction IDs.

### Transaction replay

Validation then replays transaction state historically.

For `NORMAL` transactions it checks:

- transaction validity;
- expected sender nonce;
- sufficient historical sender balance;
- debit/credit state transition.

For `REWARD` transactions it checks:

- the reward is the last transaction in the block;
- reward amount matches the configured fixed reward;
- reward transaction itself is valid;
- reward height matches block height;
- exactly one reward exists in each non-Genesis block.

This replay model is important for persistence: a loaded chain must pass the same historical rules as a chain produced in memory.

---

## Persistence

Persistence is implemented as a layered conversion pipeline rather than serializing mutable domain objects directly.

<img width="2912" height="600" alt="image" src="https://github.com/user-attachments/assets/a6512158-1e7b-40d6-815f-f37871c72afd" />

### Snapshot records

Persistence uses three snapshot records:

- `TransactionSnapshot`
- `BlockSnapshot`
- `BlockchainSnapshot`

Snapshots contain persistence-friendly data such as Base64-encoded public keys/signatures and lists of nested snapshots.

`BlockSnapshot` and `BlockchainSnapshot` defensively copy their list components, so the snapshot graph is not externally mutable through those lists.

### Transaction mapping

`TransactionMapper` converts:

```text
Transaction <-> TransactionSnapshot
```

Public keys are serialized as Base64 strings containing their encoded X.509 key form.

On restore, Ed25519 `PublicKey` objects are rebuilt through `KeyFactory` and `X509EncodedKeySpec`.

Normal transactions are reconstructed and then validated cryptographically. Reward transactions are reconstructed through the reward restoration path and validated structurally.

### Block mapping

`BlockMapper` converts:

```text
Block <-> BlockSnapshot
```

A restored block receives:

- restored transactions;
- persisted previous hash;
- persisted timestamp;
- persisted nonce;
- persisted height.

The live block object recomputes its Merkle root from transaction IDs and recalculates its hash from the restored header state.

The JSON snapshot therefore does not need to trust a persisted block hash or Merkle root as authoritative values.

### Blockchain mapping

`BlockchainMapper` converts:

```text
Blockchain <-> BlockchainSnapshot
```

The snapshot contains:

- difficulty;
- full confirmed chain;
- ordered pending transactions.

During restoration:

1. every snapshot block is converted back to a live block;
2. pending transaction snapshots are converted back to live transactions;
3. `Blockchain.restoreBlockchain(...)` validates the confirmed chain;
4. each pending transaction is re-submitted through `submitTransaction(...)`.

This final step is important: an invalid persisted mempool cannot bypass nonce, balance, duplicate, or transaction-validity rules simply because it came from disk.

### JSON codec

`BlockchainSnapshotJsonCodec` uses Jackson 3 to convert:

```text
BlockchainSnapshot <-> JSON String
```

Malformed or structurally incompatible JSON is translated to `IllegalArgumentException` at the codec boundary while preserving the underlying Jackson exception as the cause.

### File storage

`BlockchainFileStore` provides:

```text
save(BlockchainSnapshot, Path)
load(Path)
```

`save(...)` converts the snapshot to JSON and writes it with `Files.writeString(...)`.

`load(...)` reads the file with `Files.readString(...)` and parses the result through the JSON codec.

Filesystem errors remain `IOException` failures, while invalid JSON/data is treated as invalid input.

### Persistence validation coverage

The persistence tests cover:

- snapshot mapping;
- transaction round trips;
- block round trips;
- blockchain round trips;
- JSON round trips;
- writing JSON to disk;
- save/load preservation;
- loading a blockchain and continuing to mine;
- missing files;
- malformed JSON;
- valid JSON containing a broken chain link;
- valid JSON containing an invalid pending transaction.

---

## API Guide and Important Traps

This section documents behaviors that are easy to misunderstand when using the current APIs.

### 1. `new Blockchain(difficulty)` already creates Genesis

Do not assume a new chain has size `0`.

Immediately after construction:

```text
size() == 1
```

because Genesis is created and mined in the constructor.

### 2. High difficulty becomes expensive very quickly

Difficulty counts leading hexadecimal zeroes, not binary zero bits.

Each additional difficulty level is approximately 16x more expected proof-of-work effort. Test code generally uses a small difficulty such as `2` for this reason.

### 3. `getBalance(...)` is confirmed-only

Pending transactions do not alter `getBalance(...)`.

If you need to reason about whether a new pending transaction is admissible, `submitTransaction(...)` handles the effective pending balance internally.

### 4. `getNextNonce(...)` is confirmed-only

This is one of the most important API semantics in the project.

`getNextNonce(sender)` intentionally ignores the mempool.

Pending nonce advancement is calculated during `submitTransaction(...)` by replaying earlier pending transactions from that sender.

Do not manually redefine `getNextNonce(...)` as "confirmed + pending" unless the surrounding design is intentionally changed.

### 5. Creating a transaction does not submit it

`Wallet.createTransaction(...)` only creates and signs the transaction.

The caller must separately call:

```text
Blockchain.submitTransaction(transaction)
```

Submission also does not mine it.

Mining is a separate operation:

```text
Blockchain.minePendingTransactions(minerPublicKey)
```

### 6. Pending order is semantically meaningful

The mempool is currently an ordered list.

A later pending transaction may spend funds received by an earlier pending transaction, but it cannot spend an incoming transfer that has not yet appeared in the pending order.

Changing the data structure or ordering policy can therefore change validity semantics and must be done carefully.

### 7. `minePendingTransactions(...)` currently mines everything

There is no block-capacity limit or fee-based selection yet.

The method snapshots all current pending transactions, mines them into one block, then clears the mempool after success.

When finite block capacity is added, this behavior will need to change from "clear everything" to selective removal of the transactions actually included.

### 8. Empty mempool mining is valid

Calling `minePendingTransactions(...)` with no pending transactions still creates a block containing the automatic miner reward.

### 9. Do not manually add reward transactions to a block candidate

The internal block-building path rejects supplied reward transactions and appends exactly one reward automatically.

This prevents callers from inserting arbitrary mining rewards through the normal transaction list.

### 10. `addBlock(...)` is an internal/package-level API

The intended external workflow is:

```text
Wallet -> create -> submit -> mempool -> mine -> block
```

Direct block addition is not the normal public workflow.

### 11. `new Wallet()` always means a new identity

The constructor generates a fresh Ed25519 keypair.

Creating another `Wallet` does not restore an existing wallet, even if a blockchain containing the old public key was loaded from disk.

Wallet persistence is a separate future feature.

### 12. Blockchain persistence is not wallet persistence

Saving the blockchain preserves public ledger history and pending transactions.

It does not preserve private signing keys.

This distinction is critical: loading the chain does not automatically restore the ability to authorize new transactions from old wallet identities.

### 13. `TransactionSnapshot` currently contains `transactonNonce`

The current record component is named:

```text
transactonNonce
```

rather than `transactionNonce`.

Because Jackson derives JSON field names from record components, this spelling is part of the current persisted JSON schema. Renaming it later is not just a cosmetic Java refactor; existing JSON files would need compatibility handling or migration.

### 14. Hash/signature canonical formats are effectively protocol rules

Transaction IDs and signatures depend on exact canonical strings. Block hashes depend on exact field ordering and concatenation.

Changing any of the following can invalidate previously created data:

- field order;
- delimiters;
- public-key encoding;
- transaction type text;
- amount/nonce representation;
- block header field ordering;
- Merkle conventions.

Treat these formats as consensus-like compatibility rules even though LegacyChain is currently single-process.

### 15. Merkle conventions are project-specific and must stay consistent

Current rules include:

- empty root = `SHA256("")`;
- one leaf = transaction ID directly;
- parent = `SHA256(left + right)`;
- odd count = duplicate final node.

Changing one side of this convention without changing validation/proof code will invalidate blocks or proofs.

### 16. Returned collections are intentionally protected

Examples include:

- block transaction lists;
- pending transaction getter results;
- snapshot lists;
- Merkle proof lists.

Code should not assume these collections can be modified in place. Make a mutable copy when a test or transformation needs to replace elements.

### 17. `Transaction.getSignature()` returns a defensive copy

Mutating the returned byte array does not mutate the transaction's stored signature.

This prevents callers from changing transaction identity after construction through the getter.

### 18. Snapshot loading and blockchain restoration are different validation layers

A JSON file can be syntactically valid and still represent an invalid blockchain.

For example:

```text
valid JSON
-> valid BlockchainSnapshot
-> broken block previousHash
-> BlockchainMapper.fromSnapshot(...)
-> rejected by blockchain validation
```

Likewise, a valid confirmed chain can still contain an invalid persisted mempool transaction, which is rejected when restoration replays it through `submitTransaction(...)`.

### 19. File save currently overwrites the target file

The file store writes the complete current JSON snapshot to the caller-provided path.

There is currently no:

- append log;
- atomic temp-file swap;
- backup rotation;
- file locking;
- schema version header;
- automatic directory creation.

These are possible future persistence improvements.

### 20. Missing file and invalid file are different failures

A missing/unreadable file is an I/O problem and surfaces as `IOException`.

Malformed or incompatible JSON is an invalid-data problem and surfaces as `IllegalArgumentException` from the codec boundary.

---

## Configuration

LegacyChain currently has deliberately small configuration surface area.

### Java / build configuration

The Maven project currently targets:

- **Java 25**
- **Maven Compiler Plugin 3.14.0**
- **Maven Surefire Plugin 3.5.4**
- **JUnit Jupiter 5.14.2** for tests
- **Jackson Databind 3.2.3** for JSON persistence

Jackson 3 uses the `tools.jackson.*` package/group naming used in the current source.

### Runtime blockchain configuration

#### Difficulty

Difficulty is supplied when constructing a blockchain:

```text
Blockchain(difficulty)
```

Allowed range:

```text
1..64
```

#### Mining reward

The current block reward is fixed internally at:

```text
50
```

It is not externally configurable yet.

#### Persistence path

There is no global persistence-directory configuration. Callers pass a `Path` directly to `BlockchainFileStore.save(...)` and `load(...)`.

### Not configurable yet

The following do not currently have configuration because the associated features are not implemented yet:

- block capacity;
- transaction fees;
- fee-selection policy;
- reward schedule;
- dynamic difficulty adjustment;
- network port / peer configuration;
- node identity;
- chain data directory;
- wallet/key-store path;
- API server settings.

These may become explicit configuration surfaces as the project grows.

---

## Building and Testing

### Requirements

- JDK 25
- Maven
- Git

### Clone

```bash
git clone https://github.com/Ionnerss/LegacyChain.git
cd LegacyChain/LegacyChain
```

The second `LegacyChain` is the current Maven project directory containing `pom.xml`.

### Compile

```bash
mvn compile
```

### Run the full test suite

```bash
mvn test
```

### Typical development configuration

Use a low proof-of-work difficulty for tests and local experimentation. Difficulty `2` is common in the current tests because it exercises the mining logic without making the test suite unnecessarily expensive.

---

## Example Lifecycle

A typical conceptual flow is:

```java
Blockchain chain = new Blockchain(2);

Wallet alice = new Wallet();
Wallet bob = new Wallet();
Wallet miner = new Wallet();

// Reward-only block funds Alice.
chain.minePendingTransactions(alice.getPublicKey());

int nonce = chain.getNextNonce(alice.getPublicKey());
Transaction tx = alice.createTransaction(bob.getPublicKey(), 10, nonce);

chain.submitTransaction(tx);
chain.minePendingTransactions(miner.getPublicKey());
```

After mining:

- Alice's confirmed balance is reduced by the transfer;
- Bob's confirmed balance is increased;
- the miner receives the block reward;
- Alice's confirmed next nonce advances;
- the pending list is cleared;
- the new block commits to its transaction list through its Merkle root;
- `isValid()` can replay and validate the resulting chain.

A persistence lifecycle is:

```java
BlockchainMapper mapper = new BlockchainMapper();
BlockchainSnapshot snapshot = mapper.toSnapshot(chain);

BlockchainFileStore.save(snapshot, path);

BlockchainSnapshot loadedSnapshot = BlockchainFileStore.load(path);
Blockchain loadedChain = mapper.fromSnapshot(loadedSnapshot);
```

The restored chain can continue accepting and mining transactions if the persisted state is valid.

---

## Development and Testing Philosophy

LegacyChain has been developed incrementally with regression tests around each feature.

The usual workflow is:

```text
identify behavior
-> write targeted JUnit test
-> implement/refactor
-> run mvn test
-> inspect failure
-> correct one issue at a time
-> keep regression test
```

Test coverage currently spans:

- blockchain construction and Genesis behavior;
- proof of work;
- linked hashes;
- block heights;
- transaction validity;
- deterministic transaction IDs;
- defensive signature handling;
- balances;
- overspending;
- nonce sequencing;
- reward correctness;
- Merkle roots;
- Merkle proofs;
- mempool admission;
- pending dependencies;
- mining lifecycle;
- mapper round trips;
- persistence round trips;
- restored-chain continuation;
- invalid persisted state.

The intent is not only to prove successful cases, but to preserve invariants against regressions as the architecture evolves.

---

## Current Limitations

LegacyChain is intentionally not production-ready.

Current limitations include:

- single Java process only;
- no peer-to-peer network;
- no node discovery;
- no distributed consensus;
- no cumulative-work competing-chain selection;
- no chain reorganization support;
- no UTXO model;
- no transaction fees;
- no finite block size/capacity;
- no miner selection policy beyond current list order;
- fixed mining reward;
- fixed per-chain difficulty with no adjustment algorithm;
- no wallet/private-key persistence;
- no human-friendly wallet addresses yet;
- no CLI or REST interface;
- no transaction/block explorer;
- no schema-versioned persistence format;
- no atomic/transactional file persistence;
- no database-backed storage;
- no concurrency/thread-safety guarantees;
- no production key-management protections;
- no adversarial network/security model.

These limitations are deliberate boundaries of the current learning phase, not claims that the system is secure for real assets.

---

## Roadmap

### Completed milestones

- [x] Maven/JUnit project foundation
- [x] SHA-256 hashing
- [x] Block hash chaining
- [x] Genesis block
- [x] Proof of work / difficulty
- [x] Multiple transactions per block
- [x] Defensive/immutable data exposure
- [x] Ed25519 wallets
- [x] Digital signatures
- [x] NORMAL / REWARD transaction model
- [x] Mining rewards
- [x] Balance replay
- [x] Overspending prevention
- [x] Historical state replay
- [x] Transaction nonces / replay protection
- [x] Block heights
- [x] Deterministic transaction IDs
- [x] Reward-height binding
- [x] Merkle tree and Merkle root
- [x] Merkle inclusion proofs
- [x] Ordered mempool
- [x] Pending nonce/balance validation
- [x] Submission/mining separation
- [x] Pending transaction mining
- [x] Transaction/block/blockchain snapshot mapping
- [x] JSON snapshot serialization
- [x] Filesystem save/load
- [x] Validated blockchain restoration
- [x] Persistence corruption/error regression tests

### Active milestone

- [ ] **Wallet addresses — IN PROGRESS**
  - deterministic address derivation from public keys;
  - stable textual representation;
  - validation rules;
  - integration with wallet-facing APIs without weakening signature verification.

### Near-term planned work

- [ ] Wallet/private-key persistence
- [ ] Transaction lookup by ID
- [ ] Block lookup by index/hash/identifier
- [ ] CLI and/or REST interface
- [ ] Block explorer
- [ ] GitHub Actions CI
- [ ] Expanded architecture/user documentation

### Deferred realism improvements

- [ ] Finite block capacity
- [ ] Transaction fees
- [ ] Miner transaction-selection policy
- [ ] Priority/fee-based mempool structure
- [ ] Dynamic difficulty adjustment
- [ ] Reward reduction / issuance schedule

### Long-term / stretch work

- [ ] P2P networking
- [ ] Multiple nodes
- [ ] Chain synchronization
- [ ] Cumulative-work chain selection
- [ ] Reorganization logic
- [ ] Distributed consensus behavior

---

## Security and Production Disclaimer

LegacyChain is an educational implementation.

It should **not** be treated as a secure cryptocurrency, production blockchain, wallet, custody system, or financial network.

Among other things, production blockchain software would require substantially more work around:

- consensus and network adversaries;
- durable/transactional storage;
- private-key encryption and secure key management;
- protocol versioning;
- canonical serialization specifications;
- denial-of-service resistance;
- concurrency;
- network-level validation;
- chain selection and reorganization;
- economic rules;
- fee markets;
- resource limits;
- auditing and formal security review.

LegacyChain deliberately exposes and tests the underlying mechanics first so these later concerns can be introduced from a well-understood base.

---

## Project Status

**LegacyChain is in active development.**

The current milestone has completed the local JSON persistence pipeline. The next active feature is wallet-address derivation, followed by wallet/key persistence and higher-level usability APIs.
