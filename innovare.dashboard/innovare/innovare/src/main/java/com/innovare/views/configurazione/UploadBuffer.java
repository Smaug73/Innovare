package com.innovare.views.configurazione;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import com.vaadin.flow.component.upload.receivers.FileBuffer;



public class UploadBuffer extends FileBuffer {
    private File tmpFile;

    @Override
    protected FileOutputStream createFileOutputStream(String fileName) {
        try {
            tmpFile = createFile(fileName); // store reference
            return new FileOutputStream(tmpFile);
        } catch (IOException var3) {
            this.getLogger().log(Level.SEVERE, "Failed to create file output stream for: '" + fileName + "'", var3);
            return null;
        }
    }

    private File createFile(String fileName) throws IOException {
        String tempFileName = "upload_tmpfile_" + fileName;
        return File.createTempFile(tempFileName, (String) null);
    }

    public File getTmpFile() {
        return tmpFile;
    }
}
