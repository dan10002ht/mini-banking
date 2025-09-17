# 📚 Week 12: Blockchain Networking

## 🎯 Mục tiêu

Học về Gossip protocol, P2P networking trong blockchain.

## 📖 Nội dung học tập

### 1. Gossip Protocol

- **Message Broadcasting**: Lan truyền message qua network
- **Efficient Distribution**: Phân phối hiệu quả
- **Fault Tolerance**: Chống lỗi node

### 2. P2P Networking

- **Peer Discovery**: Tìm và kết nối peers
- **Message Routing**: Định tuyến message
- **Network Topology**: Cấu trúc mạng

### 3. Message Types

- **Transaction Messages**: Gửi transaction
- **Block Messages**: Gửi block mới
- **Sync Messages**: Đồng bộ blockchain

### 4. Network Security

- **Message Validation**: Validate message
- **Peer Authentication**: Xác thực peer
- **DDoS Protection**: Chống tấn công

## 🏗️ Implementation trong Mini Banking

### 1. Current: Redis Streams (Centralized)

```java
// Hiện tại dùng Redis Streams (centralized)
streamProducer.sendTransactionEvent(transactionEvent);
```

### 2. Cần implement: P2P Network

```java
@Service
public class P2PNetworkService {

    private List<Peer> peers = new ArrayList<>();

    public void broadcastTransaction(Transaction transaction) {
        // Broadcast transaction to all peers
        for (Peer peer : peers) {
            peer.sendTransaction(transaction);
        }
    }

    public void broadcastBlock(Block block) {
        // Broadcast block to all peers
        for (Peer peer : peers) {
            peer.sendBlock(block);
        }
    }
}
```

### 3. Cần implement: Gossip Protocol

```java
@Service
public class GossipService {

    public void gossipTransaction(Transaction transaction) {
        // Select random peers to gossip
        List<Peer> randomPeers = selectRandomPeers(3);
        for (Peer peer : randomPeers) {
            peer.sendTransaction(transaction);
        }
    }
}
```

## ✅ Đã hoàn thành

- [x] Redis Streams cho transaction processing
- [x] Event-driven architecture
- [x] Asynchronous processing

## 🎯 Tuần 12: Cần làm gì?

### 1. P2P Network

- [ ] Peer discovery
- [ ] Peer communication
- [ ] Message routing

### 2. Gossip Protocol

- [ ] Message broadcasting
- [ ] Random peer selection
- [ ] Message propagation

### 3. Network Security

- [ ] Message validation
- [ ] Peer authentication
- [ ] DDoS protection

### 4. Network Testing

- [ ] Unit tests
- [ ] Integration tests
- [ ] Network simulation

## 📝 Tasks cho Tuần 12

1. **P2P Network Service**

   - Peer discovery
   - Peer communication
   - Message routing

2. **Gossip Protocol Service**

   - Message broadcasting
   - Random peer selection
   - Message propagation

3. **Network Security Service**

   - Message validation
   - Peer authentication
   - DDoS protection

4. **Network Testing**
   - Unit tests
   - Integration tests
   - Network simulation
