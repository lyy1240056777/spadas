package edu.nyu.dss.similarity.utils;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtil {

    // write the information into files
    public static void write(String fileName, String content) {
        RandomAccessFile randomFile = null;
        try {
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
