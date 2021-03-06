package com.kvark900.client;

import com.kvark900.util.FileManager;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class responsible for managing command and data connections with the server.
 */
public class FTPClient {
    private final String host;
    private final String username;
    private final String password;
    private final int port;
    private volatile ClientCommandChannel cmdChannel;
    private static final Logger LOGGER = Logger.getLogger("CLIENT");

    public FTPClient(String host, String username, String password, int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    /**
     * Connecting to the server - opening command channel and logging in user
     *
     * @throws IOException
     * @throws AuthenticationException
     */
    public void connect() throws IOException, AuthenticationException {
        LOGGER.info(String.format("CLIENT: Connecting to server %s on port %s", host, port));
        cmdChannel = new ClientCommandChannel(host, port);
        cmdChannel.logIn(username, password);
    }

    /**
     * Uploading files - opening data channels on new threads. Maximum number of
     * async transfers is five.
     *
     * @param fileNames
     * @see #openDataChannels(int, List)
     */
    public void uploadFiles(String[] fileNames) {
        FileManager files = new FileManager(fileNames);
        int numOfFiles = files.getNumOfFilesToUpload();
        openDataChannels(numOfFiles, files.getFilesToUpload());
    }

    /**
     * Starting new threads and opening data channels.
     *
     * @param numOfFiles is number of files to transfer
     * @param files list of files to upload
     */
    private void openDataChannels(int numOfFiles, List<File> files) {
        ExecutorService executor = Executors.newFixedThreadPool(numOfFiles);
        for (File file : files) {
            executor.submit(() -> {
                try {
                    putFile(file);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    System.exit(-1);
                }
            });
        }
        shutdownAndAwaitTermination(executor);
    }

    /**
     * Creating new data channel and opening passive data connection with the server.
     * Sending STOR command and calling putFile.
     *
     * @param file to transfer
     * @throws IOException
     * @see #requestPassiveConnection(String)
     */
    private void putFile(File file) throws IOException {
        ClientDataChannel channel = new ClientDataChannel();
        InetSocketAddress ip = requestPassiveConnection(file.getName());
        LOGGER.info(String.format("CLIENT: Establishing data connection with server on IP address: %s:%s", ip.getHostName(), ip.getPort()));
        channel.connect(ip);
        cmdChannel.sendCmd(FTPCmd.STOR, file.getName());
        channel.putFile(file);
        channel.close();
    }

    /**
     * Send PASV command and request passive data connection with the server
     *
     * @param fileName
     * @return {@link InetSocketAddress} extracted server IP and port number - {@link #getSocketIPAndPort(String)}
     * @throws IOException
     */
    private InetSocketAddress requestPassiveConnection(String fileName) throws IOException {
        LOGGER.info("CLIENT: Sending  " + FTPCmd.PASV.toString() + " command");
        cmdChannel.sendCmd(FTPCmd.PASV, fileName);
        String response = cmdChannel.getResponse();
        LOGGER.info("CLIENT: Server response is: " + response);
        return getSocketIPAndPort(response);
    }

    /**
     * Parsing server's response to PASV command. Extracting server's IP and port number
     *
     * @param response
     * @return server's IP and port number
     */
    private InetSocketAddress getSocketIPAndPort(String response) {
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        String ip = "";
        int port = -1;
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
            port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
        }
        return new InetSocketAddress(ip, port);
    }

    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)){
                    LOGGER.log(Level.SEVERE, "Pool did not terminate");
                    System.exit(-1);
                }
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stopping command channel.
     * Each data channel on separate thread is stopped after transaction finish {@link #putFile(File)}
     */
    public void stop() throws IOException {
        cmdChannel.sendCmd(FTPCmd.QUIT, "");
        cmdChannel.close();
    }
}
