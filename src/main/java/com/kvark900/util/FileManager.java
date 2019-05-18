package com.kvark900.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FileManager {
    private static final int MAX_FILES = 5;
    private File[] files;
    private List<File> filesToUpload;
    private static final Logger LOGGER = Logger.getLogger("CLIENT");

    public FileManager(String[] fileNames) {
        int filesNum = fileNames.length;
        if (filesNum > MAX_FILES )
            throw new IllegalArgumentException
                    (String.format("Number of maximum files exceeded! You can upload maximum %s files", MAX_FILES));

        files = new File[fileNames.length];
        initFiles(fileNames);
        filesToUpload = new ArrayList<>();
        validateFiles();
    }

    private void initFiles(String[] fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = new File(fileNames[i]);
        }
    }

    private void validateFiles() {
        StringBuilder filesToUpload = new StringBuilder("Files to be uploaded: ");
        String delimiter = "";
        for (File file : files) {
            if (!isFile(file)) throw new IllegalArgumentException(file.getName() + " is not file.");
            this.filesToUpload.add(file);
            filesToUpload.append(delimiter).append(file.getName());
            delimiter = ";";
        }
        LOGGER.info(filesToUpload.toString());
    }

    private boolean isFile(File fileName) {
        return fileName.canRead() && fileName.isFile();
    }

    public List<File> getFilesToUpload() {
        return filesToUpload;
    }

    public int getNumOfFilesToUpload() {
        return filesToUpload.size();
    }
}
