package web.Utils;

import edu.nyu.dss.similarity.Framework;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import web.exception.FileException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/15:14
 **/
@Service
public class FileUtil {

    @Value("${spadas.file.baseUri}")
    private String basePath;

    private final Path fileStorageLocation;

    private final FileProperties fileProperties;

    //    @Autowired
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
    public String uploadFile(MultipartFile file) throws IOException {
        String oriFileName = file.getOriginalFilename();
//        String directoryName = oriFileName.substring(0, oriFileName.indexOf('/'));
//        String oriFilePath = fileProperties.getBaseUri() + oriFileName;
        String oriFilePath = "./dataset/" + oriFileName;
        File outputFile = new File(oriFilePath);
//        build parent directory
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdir();
        }
        Path outputPath = Paths.get(oriFilePath).toAbsolutePath().normalize();
        Files.copy(file.getInputStream(), outputPath, StandardCopyOption.REPLACE_EXISTING);
        return oriFileName;

//        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
//        String filename ;
//        //????????
//        if(originalFilename == null) {
//            filename = UUID.randomUUID().toString().replace("-", "") + suffix;
//        }else{
//            filename = originalFilename;
//        }

//        try {
//            // Copy file to the target location (Replacing existing file with the same name)
////            InputStream stream = file.getInputStream();
////            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
////            String line;
////            while((line = br.readLine()) != null) {
////                System.out.println(line);
////            }
//            Path targetLocation = fileStorageLocation.resolve(filename);
//            File newFile = new File(targetLocation);
//            Files.createDirectory(targetLocation);
////            need debug
//            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
//            // ????????
//            return filename;
//        } catch (IOException ex) {
//            throw new RuntimeException("Could not store file " + filename + ". Please try again!", ex);
//        }
    }


    public Resource loadFileAsResource(File file) {
        try {
            //System.out.println(fileName);
//             filePath = file.getAbsolutePath();
            Path filePath = Paths.get(file.getAbsolutePath());
//            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileException("File not found ");
            }
        } catch (MalformedURLException ex) {
            throw new FileException("File not found ", ex);
        }
    }


    public void downloadFile(File file, HttpServletRequest request, HttpServletResponse response) {
//     Load file as Resource
        Resource resource = loadFileAsResource(file);
        String fileName = file.getName();
        try {
            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            response.setContentType(mimeType);
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
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public Pair<String[], String[][]> readPreviewDataset(String filename, int max, double[][] data) throws IOException {
//        File file = findFiles(Framework.aString, filename);
        File file = new File(basePath + "\\" + filename);
        if (file.exists()) {
            System.out.println();
        }
        long lineNumber = 0;
//        try (Stream<String> lines = Files.lines(file.toPath())) {
//            lineNumber = lines.count();
//        }
        FileReader fr = new FileReader(file);
        LineNumberReader lnr = new LineNumberReader(fr);
        lnr.skip(Long.MAX_VALUE);
        lineNumber = lnr.getLineNumber() - 1;
        lnr.close();
        lineNumber = Math.min(lineNumber, max);
        String[] header = null;
//        String[][] bodies = new String[(int)lineNumber][];
        List<String[]> bodiesList = new ArrayList<>();
        // display ten rows
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), "gbk"))) {
            String strLine;
            int i = 0, j = 0;
            while (((strLine = br.readLine()) != null) && i <= lineNumber && j < data.length) {
                String[] rawSplitString = strLine.split(",");
                if (rawSplitString.length < 3)
                    System.out.println(file.toURI());
                String[] splitString = new String[9];
                System.arraycopy(rawSplitString, 1, splitString, 0, 7);
                System.arraycopy(rawSplitString, 11, splitString, 7, 2);
                if (i == 0) {
                    header = splitString;
                } else {
                    double temp;
                    try {
                        temp = Double.parseDouble(rawSplitString[12]);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        continue;
                    }
                    if (temp != data[j][0]) {
                        continue;
                    }
//                    bodies[i-1] = splitString;
                    bodiesList.add(splitString);
                    j++;
                }
                i++;
            }
        }
        System.out.println("bodies:" + bodiesList.size());
        String[][] bodies = new String[bodiesList.size()][];
        for (int i = 0; i < bodiesList.size(); i++) {
            bodies[i] = bodiesList.get(i);
        }
        return Pair.of(header, bodies);
    }

    public static File findFiles(String baseDirName, String targetFileName) {
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            System.out.println("Base Directory Error!");
        }
        File tempFile;
        File[] files = baseDir.listFiles();
        for (File file : files) {
            tempFile = file;
            if (tempFile.isDirectory()) {
                File resFile = findFiles(tempFile.getAbsolutePath(), targetFileName);
                return resFile;
            } else if (tempFile.isFile()) {
                String tempName = tempFile.getName();
                if (tempName.equals(targetFileName)) {
                    return tempFile;
                }
            }
        }
        System.out.println("Cannot Find Required File!");
//        should not access this step!
        return baseDir;
    }

}
