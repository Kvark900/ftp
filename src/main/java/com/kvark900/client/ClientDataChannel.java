package com.kvark900.client;

import com.kvark900.util.FileTransferStats;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Data channel created on separate threads.
 * Responsible for sending bytes to the server
 */
public class ClientDataChannel {
    private Socket dataSocket;
    private static final Logger LOGGER = Logger.getLogger("CLIENT");

    /**
     * Connecting to the server
     *
     * @param serverIP server's IP and port number provided in response message to PASV  cmd
     * @throws IOException
     */
    public void connect(InetSocketAddress serverIP) throws IOException {
        LOGGER.info(String.format("CLIENT: Connecting to server data channel: %s:%s", serverIP.getHostName(), serverIP.getPort()));
        dataSocket = new Socket(serverIP.getHostName(), serverIP.getPort());
        LOGGER.info(String.format("CLIENT: Connected to server data channel: %s:%s", serverIP.getHostName(), serverIP.getPort()));
    }

    /**
     * Transferring bytes from file's input stream to data socket's output stream.
     * Saving transfer statistics
     *
     * @param file
     * @throws IOException
     */
    public void  putFile(File file) throws IOException {
        try (InputStream inStream = new FileInputStream(file);
            BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream()))
        {
            long startTime = System.currentTimeMillis();
            long bytesTransferred = inStream.transferTo(output);
            LOGGER.info(String.format("Uploaded %d bytes to server %d ", bytesTransferred, dataSocket.getPort()));
            long endTime = System.currentTimeMillis();
            double timeTook = (double) (endTime - startTime) / 1000;
            double kBTransferred = bytesTransferred / timeTook;
            FileTransferStats.displaySingleFileStats(file.getName(), kBTransferred / 1024, timeTook);
        }
    }

    /**
     * Closing data socket
     *
     * @throws IOException
     */
    public void close() throws IOException {
        dataSocket.close();
    }
}
