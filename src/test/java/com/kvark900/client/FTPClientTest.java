package com.kvark900.client;

import com.kvark900.server.FTPServer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class FTPClientTest {

    private static final java.util.logging.Logger LOGGER = Logger.getLogger("FTP");

    @Test
    FTPClient connectSuccessfully() {
        startServer(21);

        FTPClient ftpClient = new FTPClient("127.0.0.1", null, null, 21);
        assertDoesNotThrow(ftpClient::connect, "Connected successfully");
        return ftpClient;
    }

    @Test
    void connectOnWrongPort() {
        startServer(21);

        FTPClient ftpClient = new FTPClient("127.0.0.1", null, null, 22);
        assertThrows(ConnectException.class, ftpClient::connect);
    }

    @Test
    void uploadFiles() {
        startServer(21);
        assertDoesNotThrow(() -> {
            FTPClient ftpClient = new FTPClient("127.0.0.1", null, null, 21);
            ftpClient.connect();
            long start = System.currentTimeMillis();
            ftpClient.uploadFiles(new String[]{
                    "./src/main/resources/client/1.pdf",
                    "./src/main/resources/client/2.pdf",
                    "./src/main/resources/client/3.pdf",
                    "./src/main/resources/client/4.pdf"
            });

            LOGGER.log(Level.INFO, "Total upload time: " + (double) (System.currentTimeMillis() - start ) / 1000);
        });
    }

    @Test
    void testFileDiscovery() {
        File file = new File("./src/main/resources/client/1.pdf");
        assertTrue(file.length() != 0L);
    }


    private void startServer(int port) {
        new Thread(() -> {
            try {
                new FTPServer().start(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}