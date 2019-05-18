package com.kvark900.util;

import java.util.Collections;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Responsible for parsing command line arguments
 */
public class ArgumentsParser {
    private String username, password, server, files;

    public ArgumentsParser(String... args) {
        parse(args);
    }

    /**
     * Parsing command line arguments
     *
     * @param args
     */
    private void parse(String... args) {
        int i = 0;
        String arg;
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-u") && (i < args.length)) {
                username = args[i++];
            } else if (arg.equals("-p") && (i < args.length)) {
                password = args[i++];
            } else if (arg.equals("-server") && (i < args.length)) {
                server = args[i++];
            } else if (arg.equals("-files")) {
                if (i < args.length) {
                    files = args[i++];
                }
            }
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("File arguments empty\n"
                    + "Use: –u ftpuser –p ftppass – server 127.0.0.1 –files filepath\n");
        }
    }

    public String getUsername() {
        if (username == null)
            return "unixmen";
        return username;
    }

    public String getPassword() {
        if (password == null)
            return "unixmen";
        return password;
    }

    public String getServer() {
        if (server == null)
            return "127.0.0.1";
        return server;
    }

    /**
     * Extracting file paths from CL arguments and removing duplicates.
     *
     * @return string array containing file paths.
     */
    public String[] getFileNames() {
        return Collections.list(new StringTokenizer(files, ";"))
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet())
                .toArray(String[]::new);
    }

}
