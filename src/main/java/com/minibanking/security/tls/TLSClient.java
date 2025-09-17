package com.minibanking.security.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * TLS Client Implementation
 * Provides secure client communication for banking system
 */
@Component
public class TLSClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TLSClient.class);
    
    private SSLSocket sslSocket;
    private SSLSocketFactory sslSocketFactory;
    private boolean isConnected = false;
    
    /**
     * Initialize TLS Client
     */
    public void initializeTLS() {
        try {
            logger.info("Initializing TLS Client...");
            
            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());
            
            // Create socket factory
            this.sslSocketFactory = sslContext.getSocketFactory();
            
            logger.info("TLS Client initialized successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing TLS Client", e);
            throw new RuntimeException("Failed to initialize TLS Client", e);
        }
    }
    
    /**
     * Connect to TLS Server
     * @param host Server host
     * @param port Server port
     */
    public void connectToServer(String host, int port) {
        try {
            logger.info("Connecting to TLS Server: {}:{}", host, port);
            
            // Create SSL socket
            this.sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            
            // Enable all cipher suites
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            
            // Start handshake
            sslSocket.startHandshake();
            
            this.isConnected = true;
            
            logger.info("TLS connection established successfully");
            logger.info("Cipher suite: {}", sslSocket.getSession().getCipherSuite());
            logger.info("Protocol: {}", sslSocket.getSession().getProtocol());
            
        } catch (Exception e) {
            logger.error("Error connecting to TLS Server", e);
            throw new RuntimeException("Failed to connect to TLS Server", e);
        }
    }
    
    /**
     * Send secure message to server
     * @param message Message to send
     * @return Response from server
     */
    public String sendSecureMessage(String message) {
        try {
            if (!isConnected) {
                throw new IllegalStateException("TLS Client is not connected");
            }
            
            logger.info("Sending secure message: {}", message);
            
            // Send message
            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            out.println(message);
            
            // Receive response
            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String response = in.readLine();
            
            logger.info("Received response: {}", response);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error sending secure message", e);
            throw new RuntimeException("Failed to send secure message", e);
        }
    }
    
    /**
     * Send banking command
     * @param command Banking command
     * @param data Command data
     * @return Response from server
     */
    public String sendBankingCommand(String command, String data) {
        try {
            String message = command + ":" + data;
            return sendSecureMessage(message);
        } catch (Exception e) {
            logger.error("Error sending banking command: {}", command, e);
            throw new RuntimeException("Failed to send banking command", e);
        }
    }
    
    /**
     * Get account balance
     * @param accountId Account ID
     * @return Balance response
     */
    public String getAccountBalance(String accountId) {
        try {
            logger.info("Getting account balance for: {}", accountId);
            return sendBankingCommand("BALANCE", accountId);
        } catch (Exception e) {
            logger.error("Error getting account balance", e);
            throw new RuntimeException("Failed to get account balance", e);
        }
    }
    
    /**
     * Transfer money
     * @param fromAccount From account
     * @param toAccount To account
     * @param amount Amount
     * @return Transfer response
     */
    public String transferMoney(String fromAccount, String toAccount, String amount) {
        try {
            logger.info("Transferring money: {} from {} to {}", amount, fromAccount, toAccount);
            String data = fromAccount + "," + toAccount + "," + amount;
            return sendBankingCommand("TRANSFER", data);
        } catch (Exception e) {
            logger.error("Error transferring money", e);
            throw new RuntimeException("Failed to transfer money", e);
        }
    }
    
    /**
     * Deposit money
     * @param accountId Account ID
     * @param amount Amount
     * @return Deposit response
     */
    public String depositMoney(String accountId, String amount) {
        try {
            logger.info("Depositing money: {} to account {}", amount, accountId);
            String data = accountId + "," + amount;
            return sendBankingCommand("DEPOSIT", data);
        } catch (Exception e) {
            logger.error("Error depositing money", e);
            throw new RuntimeException("Failed to deposit money", e);
        }
    }
    
    /**
     * Withdraw money
     * @param accountId Account ID
     * @param amount Amount
     * @return Withdrawal response
     */
    public String withdrawMoney(String accountId, String amount) {
        try {
            logger.info("Withdrawing money: {} from account {}", amount, accountId);
            String data = accountId + "," + amount;
            return sendBankingCommand("WITHDRAW", data);
        } catch (Exception e) {
            logger.error("Error withdrawing money", e);
            throw new RuntimeException("Failed to withdraw money", e);
        }
    }
    
    /**
     * Close connection
     */
    public void closeConnection() {
        try {
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
                this.isConnected = false;
                logger.info("TLS connection closed");
            }
        } catch (Exception e) {
            logger.error("Error closing TLS connection", e);
        }
    }
    
    /**
     * Check if client is connected
     * @return true if connected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Get connection info
     * @return Connection info
     */
    public String getConnectionInfo() {
        if (sslSocket != null && !sslSocket.isClosed()) {
            return String.format("Connected to %s:%d, Cipher: %s, Protocol: %s",
                sslSocket.getInetAddress().getHostName(),
                sslSocket.getPort(),
                sslSocket.getSession().getCipherSuite(),
                sslSocket.getSession().getProtocol());
        }
        return "Not connected";
    }
}
