package edu.nyu.dss.similarity.datasetReader;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import web.SpadasWebApplication;

import java.io.File;
import java.io.FileNotFoundException;

@Slf4j
public class DirectoryRenamer {
    private final String baseDir = "/Users/haoxingxiao/repos/spadas/dataset-mini";

    @Test
    public void replaceComma() throws FileNotFoundException {
        File base = new File(baseDir);
        rename(base);
    }

    /**
     * rename directory and file, replace ":" with "_"
     *
     * @param file
     */
    private void rename(File file) {
        if (file.getName().contains(":")) {
            File newFile = new File(file.getAbsolutePath().replace(":", "_"));
            log.info("Renaming file from {} to {}", file.getName(), newFile.getName());
            boolean success = file.renameTo(newFile);
            if (!success) {
                log.error("Failed to rename file {}", file.getAbsolutePath());
                return;
            }
            file = newFile;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                rename(f);
            }
        }
    }
}
