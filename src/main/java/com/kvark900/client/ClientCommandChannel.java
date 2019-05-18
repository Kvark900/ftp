package com.kvark900.client;

import com.kvark900.server.FTPReturnCode;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Command channel created on client's main thread.
 * Responsible for communicating with the server
 */
public class ClientCommandChannel {
    private String server;
    private int port;
    private Socket cmdSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private FTPReturnCode returnCode;
    private String response;
    private static final Logger LOGGER = Logger.getLogger("CLIENT");

    public ClientCommandChannel(String server, int port) throws IOException {
        this.server = server;
        this.port = port;
        cmdSocket = new Socket(server, port);
        reader = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(cmdSocket.getOutputStream()));
    }

    /**
     * Logging in with the username and password provided in CL arguments
     *
     * @param username
     * @param password
     * @throws IOException
     * @throws AuthenticationException if username or passwords are incorrect
     */
    public void logIn(String username, String password) throws IOException, AuthenticationException {
        returnCode = sendCmd(FTPCmd.USER, username);
        if (returnCode != FTPReturnCode.NEED_PASSWORD)
            throw new AuthenticationException("Wrong username!");
        returnCode = sendCmd(FTPCmd.PASS, password);
        if (returnCode != FTPReturnCode.LOGGED_IN)
            throw new AuthenticationException("Wrong password!");
        LOGGER.info("Logged in successfully...");
    }

    /**
     * Sending command to the server.
     *
     * @param cmd - client command
     * @return {@link FTPReturnCode}
     * @throws IOException
     * @see FTPCmd
     */
    public FTPReturnCode sendCmd(FTPCmd cmd, String args) throws IOException {
        writer.write(createMessage(cmd.toString(), args));
        writer.flush();
        LOGGER.info(String.format("CLIENT: %s %s --> Server[%s:%S]", cmd, args, server, port));
        return readResponse();
    }

    /**
     * Creating client's message consisting of FTP command and additional arguments
     *
     * @param cmd command
     * @param args arguments
     * @return string representing client's message
     */
    private String createMessage(String cmd, String args) {
        StringBuilder message = new StringBuilder();
        message.append(cmd);
        if (args != null) message.append(' ').append(args);
        message.append("\r\n");
        return message.toString();
    }

    /**
     * Reading server's response.
     *
     * @return server's FTP response code
     * @throws IOException
     *
     * @see FTPReturnCode
     */
    private FTPReturnCode readResponse() throws IOException {
        response = reader.readLine();
        int code = -1;
        try {
            code = Integer.parseInt(response, 0, 3, 10);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {}
        return code == -1 ? FTPReturnCode.UNKNOWN_ERROR : FTPReturnCode.fromId(code);
    }

    public FTPReturnCode getReturnCode() {
        return returnCode;
    }

    public String getResponse() {
        return response;
    }

    /**
     * Closing socket and IO streams
     *
     * @throws IOException
     */
    public void close() throws IOException {
        cmdSocket.close();
        reader.close();
        writer.close();
    }
}
