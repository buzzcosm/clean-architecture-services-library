package com.buzzcosm.cleanarchitecture.services.library.ftp;

import com.buzzcosm.cleanarchitecture.services.library.ftp.client.impl.FtpClientSimpleImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class FtpClientSimpleImplIntegrationTest {

    private static FakeFtpServer fakeFtpServer;
    private static FtpClientSimpleImpl ftpClientSimpleImpl;

    @BeforeAll
    static void setUp() throws IOException {
        // Initialize the fake FTP server and add a user account and files
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "walletId: 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);
        fakeFtpServer.start();

        // Initialize the FTP client
        ftpClientSimpleImpl = new FtpClientSimpleImpl("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
        ftpClientSimpleImpl.open();
    }

    @AfterAll
    static void tearDown() throws IOException {
        // Clean up after tests
        ftpClientSimpleImpl.close();
        fakeFtpServer.stop();
    }

    @Test
    void givenRemoteFile_whenListingRemoteFiles_thenItIsContainedInList() throws IOException {
        // Call the method to test
        Collection<String> files = ftpClientSimpleImpl.listFiles("/data");

        // Assert the expected result
        assertThat(files).contains("foobar.txt");
    }

    @Test
    void givenRemoteFile_whenDownloading_thenItIsOnTheLocalFilesystem() throws IOException {
        ftpClientSimpleImpl.downloadFile("/buz.txt", "downloaded_buz.txt");
        assertThat(new File("downloaded_buz.txt")).exists();
        new File("downloaded_buz.txt").delete(); // cleanup
    }

    @Test
    public void givenLocalFile_whenUploadingIt_thenItExistsOnRemoteLocation() throws IOException {
        // Create a temporary file in memory
        String content = "This is a virtual file content!";
        byte[] contentBytes = content.getBytes();

        // Write the content to the temporary file
        File tempFile = File.createTempFile("tempFile", ".txt");

        // Write the content to the temporary file
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(contentBytes);
        }

        // Use the FTP client to upload the temporary file
        ftpClientSimpleImpl.uploadFile(tempFile, "/buz.txt");

        // Check whether the file exists in the fake FTP server
        assertThat(fakeFtpServer.getFileSystem().exists("/buz.txt")).isTrue();

        // Clean up the temporary file after the test
        tempFile.delete();
    }

    @Rollback
    @Transactional
    @Test
    void givenRemoteFile_whenDeleting_thenItIsDeletedOnRemoteServer() throws IOException {
        String filePath = "/data/foobar.txt"; // Ensure that the file exists
        Collection<String> filesBeforeDelete = ftpClientSimpleImpl.listFiles("/data");
        assertThat(filesBeforeDelete).contains("foobar.txt");

        // Delete the file on the FTP server
        ftpClientSimpleImpl.deleteFile(filePath);

        // Check that the file no longer exists on the server after deletion
        Collection<String> filesAfterDelete = ftpClientSimpleImpl.listFiles("/data");
        assertThat(filesAfterDelete).doesNotContain("foobar.txt");
    }
}