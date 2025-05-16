package com.buzzcosm.cleanarchitecture.services.library.ftp;

import com.buzzcosm.cleanarchitecture.services.library.ftp.client.impl.FtpClientSimpleImpl;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class FtpClientSimpleImplIntegrationTest {

    private static FakeFtpServer fakeFtpServer;
    private static FtpClientSimpleImpl ftpClientSimpleImpl;

    @BeforeEach
    void setUpEachTest() throws IOException {
        // Initialize the fake FTP server and add a user account and files
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "walletId: 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);

        // Set a random port
        fakeFtpServer.setServerControlPort(0); // Random available port
        fakeFtpServer.start();

        // Verify server has started and print the port
        log.info("FTP server started on port {}", fakeFtpServer.getServerControlPort());

        // Initialize the FTP client
        ftpClientSimpleImpl = new FtpClientSimpleImpl("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
        ftpClientSimpleImpl.open();
    }

    @AfterEach
    void tearDownEachTest() throws IOException {
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
    void givenRemoteFile_whenStreaming_thenItIsOnTheLocalFilesystem() throws IOException {
        String filePath = "/data/foobar.txt"; // Ensure that the file exists
        Collection<String> filesBeforeDelete = ftpClientSimpleImpl.listFiles("/data");

        // Check that the file exists on the server
        assertThat(filesBeforeDelete).contains("foobar.txt");

        // Stream file from server to ByteArrayOutputStream
        ByteArrayOutputStream os = ftpClientSimpleImpl.streamFile(filePath);

        // Ensure that the content of the ByteArrayOutputStream is correct
        String fileContent = os.toString();
        assertThat(fileContent).isNotEmpty(); // Check that the contents are not empty
        assertThat(fileContent).contains("walletId: 1234567890"); // Expected content of the file
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