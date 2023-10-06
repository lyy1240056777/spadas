package web.controller;

import edu.nyu.dss.similarity.SSSData;
import edu.nyu.dss.similarity.SSSOperate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import web.Utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class SssController {

    @Autowired
    private FileUtil fileService;

    @GetMapping("spadas/api/sss/load")
    public Map<Integer, Double[]> loadSSS() throws IOException {
        if (SSSOperate.placeIDMap.isEmpty()) {
            SSSOperate.initSSS();
        }
        return SSSOperate.placeIDMap;
    }

    @GetMapping("spadas/api/sss/get/{param}")
    public List<SSSData> getSSSData(@PathVariable int param) {
        return SSSOperate.selectSSSData(param);
    }

    @GetMapping("spadas/api/sss/file/{id}")
    public void downloadSSSFile(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        SSSData sss = SSSOperate.sssDataList.get(id);
        String fileName = sss.getName();
        File downloadFile = new File(SSSOperate.rootDir, fileName);
        fileService.downloadFile(downloadFile, request, response);
    }
}
