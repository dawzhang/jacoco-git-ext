package com.keking;

import com.keking.git.ClassDiffEntity;
import com.keking.git.JGitUtil;
import com.keking.report.ReportUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DiffClover {
    private static final Logger logger = LoggerFactory.getLogger(DiffClover.class);

    public static void main(String[] args) {

        // step1:获取必要的配置信息
        String gitUrl, gitUsername, gitPassword, newVersion, oldVersion, diffFolder, jacocoReport = "";

        try {
            gitUrl = System.getProperty("GitURI").trim();
            gitUsername = System.getProperty("GitUsername") == null ? null : System.getProperty("GitUsername").trim();
            gitPassword = System.getProperty("GitPassword") == null ? null : System.getProperty("GitPassword").trim();
            diffFolder = System.getProperty("DiffFolder").trim();
            newVersion = System.getProperty("NewVersion").trim();
            oldVersion = System.getProperty("OldVersion").trim();
            jacocoReport = System.getProperty("JacocoReport").trim();
            jacocoReport = jacocoReport.endsWith("/") ? jacocoReport.substring(0, jacocoReport.lastIndexOf("/")) : jacocoReport;
        } catch (Exception e) {
            logger.error("GitURI || NewVersion || OldVersion || DiffFolder || JacocoReport is empty");
            return;
        }

        if (StringUtils.isEmptyOrNull(gitUrl) || StringUtils.isEmptyOrNull(newVersion)
                || StringUtils.isEmptyOrNull(oldVersion) || StringUtils.isEmptyOrNull(diffFolder)
                || StringUtils.isEmptyOrNull(jacocoReport)) {
            logger.error("GitURI || NewVersion || OldVersion || DiffFolder || JacocoReport is empty");
            return;
        }

        try {
            // step2:获取最新的源代码
            List<File> fileList = (List<File>) FileUtils.listFiles(new File(diffFolder), null, true);
            if (fileList.size() < 10) {
                JGitUtil.cloneGit(gitUrl, diffFolder, gitUsername, gitPassword);
            } else {
                JGitUtil.pullGit(diffFolder, gitUsername, gitPassword);
            }
            // step3:针对传入分支比较
            List<ClassDiffEntity> diffs = JGitUtil.diffBranch(diffFolder, newVersion, oldVersion);
            Map<String, List<String>> jacocoFiles = ReportUtil.scanJacocoResource(jacocoReport);

            // step4:生成Diff Coverage Report
            ReportUtil.generateGitDiffIndex(diffs, jacocoReport.substring(0, jacocoReport.lastIndexOf("/")) + "/Git_Diff/index.html");
            for (ClassDiffEntity diff : diffs) {
                List<String> jacocoClassFiles = jacocoFiles.get(diff.getFileName().replace(".java", ""));
                if (jacocoClassFiles == null || jacocoClassFiles.size() == 0) continue;
                for (String jacocoFile : jacocoClassFiles) {
                    String diffJacocoFile = jacocoFile.replaceAll(jacocoReport.substring(jacocoReport.lastIndexOf("/") + 1), "Git_Diff");
                    FileUtils.copyFile(FileUtils.getFile(jacocoFile), new File(diffJacocoFile));
                    ReportUtil.markGitDiffFlag(diffJacocoFile, diff);
                }
            }
            logger.info("git diff report generate success");
        } catch (Exception e) {
            logger.error("git diff report generate fail", e);
        }
    }
}
