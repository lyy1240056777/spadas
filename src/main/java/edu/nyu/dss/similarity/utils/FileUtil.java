package edu.nyu.dss.similarity.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtil {

    // write the information into files
    public static void write(String fileName, String content) {
        RandomAccessFile randomFile = null;
        try {
            File file = new File(fileName);
            String parentDir = file.getParent();
            File dir  = new File(parentDir);
            dir.mkdirs();
            randomFile = new RandomAccessFile(fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
