package com.buzzcosm.cleanarchitecture.services.library.ftp.client;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface FtpClient {
    void open() throws IOException;
    void close() throws IOException;
    Collection<String> listFiles(String path) throws IOException;
    void downloadFile(String source, String destination) throws IOException;
    void uploadFile(File file, String path) throws IOException;
    void deleteFile(String path) throws IOException;
}
