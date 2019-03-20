package com.keking.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGitUtil {

    private static final Logger logger = LoggerFactory.getLogger(JGitUtil.class);

    private static final String GIT_DIFF_FLAG = "diff --git";
    private static final String GIT_DIFF_LINE_PATTERN = "\\+(.*?) @@";

    static {
        JschConfigSessionFactory jschConfigSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        };
        SshSessionFactory.setInstance(jschConfigSessionFactory);
    }

    /**
     * clone代码
     *
     * @param remoteURI
     * @param tmpPath
     * @throws Exception
     */
    public static void cloneGit(String remoteURI, String tmpPath, String gitUsername, String gitPassword) throws Exception {
        File localPath = new File(tmpPath);
        if (localPath.exists()) {
            FileUtils.deleteDirectory(localPath);
        }
        CloneCommand gitCommand = Git.cloneRepository().setURI(remoteURI).setCloneAllBranches(true).setDirectory(localPath);
        if (gitUsername != null && gitPassword != null) {
            gitCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword));
        }
        try (Git git = gitCommand.call()) {
            for (Ref b : git.branchList().setListMode(ListMode.ALL).call())
                logger.debug("cloned branch:" + b.getName());
        }
    }

    /**
     * 更新代码
     *
     * @param tmpPath
     * @throws Exception
     */
    public static void pullGit(String tmpPath, String gitUsername, String gitPassword) throws Exception {
        UsernamePasswordCredentialsProvider credential = null;
        if (gitUsername != null && gitPassword != null) {
            credential = new UsernamePasswordCredentialsProvider(gitUsername, gitPassword);
        }
        try (Git git = Git.open(new File(tmpPath))) {
            PullCommand pullCommand = git.pull();
            if (credential != null) {
                pullCommand.setCredentialsProvider(credential);
            }
            pullCommand.call();
        }
    }

    /**
     * diff git branch
     *
     * @param tmpPath
     * @param newRefName
     * @param oldRefName
     * @return
     * @throws Exception
     */
    public static List<ClassDiffEntity> diffBranch(String tmpPath, String newRefName, String oldRefName) throws Exception {
        try (Git git = Git.open(new File(tmpPath)); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Repository repository = git.getRepository();
            List<ClassDiffEntity> changeDetails = new ArrayList<ClassDiffEntity>();
            AbstractTreeIterator oldTreeParser = prepareTreeParserWithRef(repository, oldRefName);
            AbstractTreeIterator newTreeParser = prepareTreeParserWithRef(repository, newRefName);
            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();

            DiffFormatter diffLineFormat = new DiffFormatter(out);
            diffLineFormat.setRepository(repository);
            diffLineFormat.format(diffs);
            RawText rawText = new RawText(out.toByteArray());
            rawText.getLineDelimiter();
            String changeDiffLog = out.toString();

            if (StringUtils.isEmptyOrNull(changeDiffLog)) {
                return changeDetails;
            }

            if (changeDiffLog.startsWith(GIT_DIFF_FLAG))
                changeDiffLog = changeDiffLog.replaceFirst(GIT_DIFF_FLAG, "");
            String[] diffFileLogs = changeDiffLog.split(GIT_DIFF_FLAG);

            if (diffFileLogs.length != diffs.size()) {
                throw new Exception("git diff branch analysis appear unknow exception");
            }

            for (int diffIndex = 0; diffIndex < diffs.size(); diffIndex++) {
                DiffEntry entry = diffs.get(diffIndex);
                String fileName = entry.getNewPath().substring(entry.getNewPath().lastIndexOf("/") + 1);
                if (fileName.endsWith("java"))
                    changeDetails.add(new ClassDiffEntity(fileName, entry.getNewPath(), entry.getChangeType().name(), analysisDiffLog(diffFileLogs[diffIndex])));
            }
            return changeDetails;
        }
    }

    private static AbstractTreeIterator prepareTreeParserWithRef(Repository repository, String refName) throws IOException {
        Ref head = repository.exactRef(refName);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }

    /**
     * 分析git diff log
     *
     * @param diffLog
     * @return
     */
    private static Map<Integer, Integer> analysisDiffLog(String diffLog) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        Pattern pattern = Pattern.compile(GIT_DIFF_LINE_PATTERN);
        Matcher matcher = pattern.matcher(diffLog);
        while (matcher.find()) {
            String[] lineDiff = matcher.group(0).replaceAll("@@", "").replaceAll("\\+", "").trim().split(",");
            if (lineDiff.length != 2) {
                logger.error("analysis diff details appear error");
            } else {
                map.put(Integer.parseInt(lineDiff[0]), Integer.parseInt(lineDiff[1]));
            }
        }
        return map;
    }
}
