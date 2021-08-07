package com.ey.in.tds.dividend.common;

import com.ey.in.tds.returns.bot.fs.FolderCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FileUtil {

   // @Value("${report.io.dir.path}")
    private String basePath;

    //@Value("${app.temp.file.delete}")
    private boolean shouldDeleteTempFolder;

    @Autowired
    private FolderCleaner folderCleaner;

    public File createJobDirectory(String jobId) throws IOException {
        File baseDir = getBaseDirectory();
        return createDirectory(baseDir.getAbsolutePath() + File.separator + jobId);
    }

    public File getBaseDirectory() throws IOException {
        return createDirectory(basePath);
    }

    private File createDirectory(String path) throws IOException {
        File directory = new File(path);
        if (directory.exists()) {
            return directory;
        } else {
            return Files.createDirectory(Paths.get(path)).toFile();
        }
    }

    public List<File> getFilesForJobId(String jobId) {
        File baseDir = new File(basePath + File.separator + jobId);
        File[] files = baseDir.listFiles();
        if (files != null && files.length > 0)
            return Arrays.asList(files);
        else
            return new ArrayList<>();
    }

    public File createFile(String fileName) throws IOException {
        File file = new File(getBaseDirectory().getAbsolutePath() + File.separator + fileName);
        boolean fileCreated = file.createNewFile();
        if (!fileCreated) {
            throw new IOException("Unable to create file " + fileName);
        }
        return file;
    }

    public boolean isFileExists(String fileName) {
        File f = null;
        try {
            f = new File(getBaseDirectory().getAbsolutePath() + File.separator + fileName);
        } catch (IOException e) {
            return false;
        }
        return f.exists();
    }

    public boolean deleteFile(String fileName) {
        File f = null;
        try {
            f = new File(getBaseDirectory().getAbsolutePath() + File.separator + fileName);
            return f.delete();
        } catch (IOException e) {
            return false;
        }
    }

    public void deleteDirectory(String jobId) {
        File f = null;
        try {
            f = new File(getBaseDirectory().getAbsolutePath() + File.separator + jobId);
            if (shouldDeleteTempFolder) {
                folderCleaner.deleteDirectory(f);
            }
        } catch (IOException ignored) {

        }
    }

    public boolean ifAnyProcessing() throws IOException {
        File folder = getBaseDirectory();
        File[] listOfFiles = folder.listFiles();
        boolean flag = false;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String[] filename = file.getName().split("\\.(?=[^\\.]+$)"); //split filename from it's extension
                if (filename[1].equalsIgnoreCase(".p"))
                    flag = true;
            }
        }
        return flag;
    }
}
