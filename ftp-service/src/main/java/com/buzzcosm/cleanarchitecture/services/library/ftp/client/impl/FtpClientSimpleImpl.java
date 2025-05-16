package com.buzzcosm.cleanarchitecture.services.library.ftp.client.impl;

import com.buzzcosm.cleanarchitecture.services.library.ftp.client.FtpClient;
import com.buzzcosm.cleanarchitecture.services.library.ftp.client.BufferSize;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class FtpClientSimpleImpl implements FtpClient {
    private String server;
    private int port;
    private String user;
    private String password;
    private FTPClient ftp;

    public FtpClientSimpleImpl(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    @Override
    public void open() throws IOException {
        ftp = new FTPClient();

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(user, password);
    }

    @Override
    public void close() throws IOException {
        ftp.disconnect();
    }

    @Override
    public Collection<String> listFiles(String path) throws IOException {
        FTPFile[] files = ftp.listFiles(path);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void downloadFile(String source, String destination) throws IOException {
        // try-with-resources
        try (FileOutputStream out = new FileOutputStream(destination)) {
            ftp.retrieveFile(source, out);
        }
    }

    @Override
    public ByteArrayOutputStream streamFile(String path) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BufferSize.BUFFER_8KB.getSize()];
        try (InputStream in = ftp.retrieveFileStream(path)) {
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return outputStream;
    }

    @Override
    public void uploadFile(File file, String path) throws IOException {
        ftp.storeFile(path, new FileInputStream(file));
    }

    @Override
    public void deleteFile(String path) throws IOException {
        ftp.deleteFile(path);
    }
}
