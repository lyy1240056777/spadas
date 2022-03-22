package web.Utils;

import edu.nyu.dss.similarity.Framework;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import web.exception.FileException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

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


    /**
     * ????
     * @param fileName ???
     * @return ??
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            //System.out.println(fileName);
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileException("File not found " + fileName, ex);
        }
    }

    /**
     * ????
     * @param fileName ???
     * @param request ??
     * @param response ??
     */
    public void downloadFile(String fileName, HttpServletRequest request, HttpServletResponse response){
//     Load file as Resource
        Resource resource = loadFileAsResource(fileName);

        try {
            //????????ContentType
            String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
            switch(suffix)
            {
                case "csv":
                    response.setContentType("text/csv");break;
                case "xls":
                case "xlsx":
                    response.setContentType("application/vnd.ms-excel");break;
                case "pdf":
                    response.setContentType("application/pdf");break;
                case "docx":
                case "doc":
                    response.setContentType("application/msword");break;
                //????????????????
                default:
                    response.setContentType("application/octet-stream");break;

            }
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(resource.getFile()));
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (IOException e) {
            throw new FileException("??????", e);
        }
    }

    /**
     * argoverse dataset preview func
     * @param filename
     * @return
     * @throws IOException
     */
    public Pair<String[],String[][]> readPreviewDataset(String filename,int max) throws IOException {
        File file = new File(Framework.aString+filename);
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        lineNumber = lineNumber>max? max:lineNumber;
        String[] header = null ;
        String[][] bodies = new String[(int)lineNumber][];
        // display ten rows
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while (((strLine = br.readLine()) != null)&&i<=lineNumber) {
                String[] splitString = strLine.split(",");
                if(splitString.length<3)
                    System.out.println(file.toURI());
                if(i==0){
                    header=splitString;
                    i++;
                    continue;
                }else{
                    bodies[i-1] = splitString;
                    i++;
                }
            }
        }
        return Pair.of(header,bodies);
    }

}
