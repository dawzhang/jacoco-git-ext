package com.keking.report;

import com.keking.git.ClassDiffEntity;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReportUtil.class);

    /**
     * 扫描代码覆盖率下的文件
     *
     * @param resourcePath
     * @return
     */
    public static Map<String, List<String>> scanJacocoResource(String resourcePath) {
        Map<String, List<String>> codeCoverageReportFiles = new HashMap<String, List<String>>();
        List<File> fileList = (List<File>) FileUtils.listFiles(new File(resourcePath), null, true);
        for (File file : fileList) {
            if (!file.getName().endsWith(".java.html"))
                continue;
            String fileName = file.getName().contains("$") ? file.getName().substring(0, file.getName().indexOf("$")) : file.getName().substring(0, file.getName().indexOf("."));
            if (codeCoverageReportFiles.containsKey(fileName)) {
                codeCoverageReportFiles.get(fileName).add(file.getAbsolutePath());
            } else {
                codeCoverageReportFiles.put(fileName, new ArrayList<String>() {
                    {
                        add(file.getAbsolutePath());
                    }
                });
            }
        }
        return codeCoverageReportFiles;
    }

    /**
     * 标识出Jacoco覆盖率文件中Git变动
     *
     * @param codeCoverageFile
     * @param diffEntity
     * @throws IOException
     */
    public static void markGitDiffFlag(String codeCoverageFile, ClassDiffEntity diffEntity) throws IOException {

        List<String> fileLines = FileUtils.readLines(new File(codeCoverageFile));
        Map<Integer, Integer> changeDetails = diffEntity.getChangeDetails();
        try {
            for (Integer lineNum : changeDetails.keySet()) {
                for (int i = 0; i < changeDetails.get(lineNum); i++) {
                    fileLines.set(lineNum + i, "<span style=\"color:red;font-size:14px;font-weight:bold;\">Gitlab</span>" + fileLines.get(lineNum + i));
                }
            }
        } catch (Exception e) {
            logger.error(diffEntity.getFileName() + " add git diff flag fail", e);
        }
        FileUtils.writeLines(new File(codeCoverageFile), fileLines);

    }

    /**
     * 生成Git变更文件列表
     *
     * @param diffEntities
     * @param filePath
     * @throws IOException
     */
    public static void generateGitDiffIndex(List<ClassDiffEntity> diffEntities, String filePath) throws IOException {
        String indexHtmlHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"/><link rel=\"stylesheet\" href=\"../jacoco-resources/report.css\" type=\"text/css\"/><link rel=\"shortcut icon\" href=\"../jacoco-resources/report.gif\" type=\"image/gif\"/><title>git diff coverage</title><script type=\"text/javascript\" src=\"../jacoco-resources/sort.js\"></script></head><body><h1>git diff coverage</h1><table class=\"coverage\"><thead><td>Element</td><tbody>";
        String indexHtmlBody = "";
        for (ClassDiffEntity diffEntity : diffEntities) {
            indexHtmlBody += "<tr><td><a href=\""
                    + diffEntity.getFilePath().substring(diffEntity.getFilePath().indexOf("src/main/java/") + "src/main/java/".length()).replaceAll("/", ".").replaceAll("." + diffEntity.getFileName(), "/" + diffEntity.getFileName() + ".html")
                    + "\" target=\"_blank\" class=\"el_class\">" + diffEntity.getFileName() + "</a></td></tr>";
        }
        String indexHtmlFoot = "</tbody></table></body></html>";
        String indexHtml = indexHtmlHead + indexHtmlBody + indexHtmlFoot;
        FileUtils.write(new File(filePath), indexHtml);
    }
}
