package com.kvark900;

import com.kvark900.client.FTPClient;
import com.kvark900.util.FileTransferStats;
import com.kvark900.util.ArgumentsParser;
import com.kvark900.server.FTPServer;
import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FTP {
    private static final Logger LOGGER = Logger.getLogger("FTP");
    private static volatile boolean uploadFinished = false;

    public static void main(String[] args) {
        try {
            server().start();
            client(args).start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Exception occurred: %s", e.getMessage()), e);
            System.exit(-1);
        }
    }

    private static Thread server() {
        FTPServer server = new FTPServer();
        return new Thread(() -> {
            server.start(21);
            while (!uploadFinished) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            server.stop();
        });
    }

    private static Thread client(String[] args) {
        return new Thread(() -> {
            ArgumentsParser parser = new ArgumentsParser(args);
            FTPClient client = new FTPClient(parser.getServer(), parser.getUsername(), parser.getPassword(), 21);
            try {
                client.connect();
                client.uploadFiles(parser.getFileNames());
                client.stop();
                uploadFinished = true;
                FileTransferStats.displaySummaryStats();
            } catch (IOException | AuthenticationException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                uploadFinished = true;
                System.exit(-1);
            }
        });
    }
}
