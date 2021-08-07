package com.ey.in.tds.returns.bot.fs;

import java.io.File;
import java.io.IOException;

public class TempFolderCreator {

    public String createNewTempFolderAndReturnPath() throws IOException {
        final File temp;

        temp = File.createTempFile("itr_web_bot_temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp).getAbsolutePath();
    }
}
