package web.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/15:14
 **/
@Service
public class FileUtil {

    private final Path fileStorageLocation;

    private final FileProperties fileProperties;

    @Autowired
    public FileUtil(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
        this.fileStorageLocation = Paths.get(fileProperties.getBaseUri()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * ???????
     *
     * @param file ??
     * @return ??Uri
     */
    public String uploadFile(MultipartFile file) {

        //TODO ??????????
        // ???????
        String originalFilename = file.getOriginalFilename();
        //?????
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String filename ;
        //????????
        if(originalFilename == null) {
            filename = UUID.randomUUID().toString().replace("-", "") + suffix;
        }else{
            filename = originalFilename;
        }

        try {
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // ????????
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + filename + ". Please try again!", ex);
        }
    }


}
