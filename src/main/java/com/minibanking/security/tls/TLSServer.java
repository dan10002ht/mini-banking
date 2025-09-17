package com.minibanking.security.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * TLS Server Implementation
 * Provides secure communication for banking system
 */
@Component
public class TLSServer {
    
    private static final Logger logger = LoggerFactory.getLogger(TLSServer.class);
    
    private SSLServerSocket sslServerSocket;
    private SSLServerSocketFactory sslServerSocketFactory;
    private boolean isRunning = false;
    
    /**
     * Initialize TLS Server
     */
    public void initializeTLS() {
        try {
            logger.info("Initializing TLS Server...");
            
            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());
            
            // Create server socket factory
            this.sslServerSocketFactory = sslContext.getServerSocketFactory();
            
            logger.info("TLS Server initialized successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing TLS Server", e);
            throw new RuntimeException("Failed to initialize TLS Server", e);
        }
    }
    
    /**
     * Start TLS Server
     * @param port Port to listen on
     */
    public void startServer(int port) {
        try {
            logger.info("Starting TLS Server on port: {}", port);
            
            // Create SSL server socket
            this.sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            
            // Enable all cipher suites
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());
            
            // Enable client authentication
            sslServerSocket.setNeedClientAuth(true);
            
            this.isRunning = true;
            
            logger.info("TLS Server started successfully on port: {}", port);
            
        } catch (Exception e) {
            logger.error("Error starting TLS Server on port: {}", port, e);
            throw new RuntimeException("Failed to start TLS Server", e);
        }
    }
    
    /**
     * Handle client connection
     * @return Client socket
     */
    public SSLSocket handleClient() {
        try {
            if (!isRunning) {
                throw new IllegalStateException("TLS Server is not running");
            }
            
            logger.debug("Waiting for client connection...");
            SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();
            
            // Start handshake
            clientSocket.startHandshake();
            
            logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
            logger.info("Cipher suite: {}", clientSocket.getSession().getCipherSuite());
            logger.info("Protocol: {}", clientSocket.getSession().getProtocol());
            
            return clientSocket;
            
        } catch (Exception e) {
            logger.error("Error handling client connection", e);
            throw new RuntimeException("Failed to handle client", e);
        }
    }
    
    /**
     * Process client message
     * @param clientSocket Client socket
     * @return Response message
     */
    public String processClientMessage(SSLSocket clientSocket) {
        try {
            // Read message from client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();
            
            logger.info("Received message from client: {}", message);
            
            // Process message (simplified)
            String response = processBankingMessage(message);
            
            // Send response to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(response);
            
            logger.info("Sent response to client: {}", response);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing client message", e);
            throw new RuntimeException("Failed to process client message", e);
        }
    }
    
    /**
     * Process banking message
     * @param message Message from client
     * @return Response message
     */
    private String processBankingMessage(String message) {
        try {
            // Simplified banking message processing
            if (message == null || message.trim().isEmpty()) {
                return "ERROR: Empty message";
            }
            
            // Parse message (simplified)
            String[] parts = message.split(":");
            if (parts.length < 2) {
                return "ERROR: Invalid message format";
            }
            
            String command = parts[0];
            String data = parts[1];
            
            switch (command.toUpperCase()) {
                case "BALANCE":
                    return "BALANCE:1000.00";
                case "TRANSFER":
                    return "TRANSFER:SUCCESS";
                case "DEPOSIT":
                    return "DEPOSIT:SUCCESS";
                case "WITHDRAW":
                    return "WITHDRAW:SUCCESS";
                default:
                    return "ERROR: Unknown command";
            }
            
        } catch (Exception e) {
            logger.error("Error processing banking message", e);
            return "ERROR: Processing failed";
        }
    }
    
    /**
     * Close client connection
     * @param clientSocket Client socket
     */
    public void closeClientConnection(SSLSocket clientSocket) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                logger.debug("Client connection closed");
            }
        } catch (Exception e) {
            logger.error("Error closing client connection", e);
        }
    }
    
    /**
     * Stop TLS Server
     */
    public void stopServer() {
        try {
            if (sslServerSocket != null && !sslServerSocket.isClosed()) {
                sslServerSocket.close();
                this.isRunning = false;
                logger.info("TLS Server stopped");
            }
        } catch (Exception e) {
            logger.error("Error stopping TLS Server", e);
        }
    }
    
    /**
     * Check if server is running
     * @return true if server is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get server port
     * @return Server port
     */
    public int getServerPort() {
        if (sslServerSocket != null && !sslServerSocket.isClosed()) {
            return sslServerSocket.getLocalPort();
        }
        return -1;
    }
}
