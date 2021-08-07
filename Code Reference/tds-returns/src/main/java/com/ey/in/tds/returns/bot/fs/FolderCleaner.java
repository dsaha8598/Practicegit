package com.ey.in.tds.returns.bot.fs;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Utility to delete all files in the given temp folder
 */
@Component
public class FolderCleaner {

    /**
     * Cleans the directory
     *
     * @param tempFolderAbsPath
     * @return true if success, false if there is a failure
     */
    public boolean clean(String tempFolderAbsPath) {
        try {
            FileUtils.cleanDirectory(new File(tempFolderAbsPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteDirectory(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                deleteDirectory(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
        System.out.println("Deleted file/folder: " + file.getAbsolutePath());
    }
}
