package huchangchun.sjtu.edu.cn;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

/**
 * System properties
 * 1.	config.retryCount, the retry count when FTP command fails.   
 * 2.	config.retryInterval, the retry Interval when FTP command fails. The unit is millisecond.
 * 3.	config.ftpTimeout, the time of waiting for FTP response. The unit is second.
 */
public class FtpService {

    public static FTPClient createFtpClient(FtpInfo ftpInfo) throws IOException, NoSuchAlgorithmException {
        FTPClient ftpClient = null;
        if (ftpInfo.isSecure()) {
            try {
                ftpClient = new FTPSClient("SSL");
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
        } else {
            ftpClient = new FTPClient();
        }

        int timeout = Integer.parseInt(System.getProperty("config.ftpTimeout", "60")) * 1000;
        // FTP client need get reply when it connecting FTP server, it is a data
        // connection. The
        // timeout will be default timeout. Retry several times, if timeout
        // exception is thrown.
        ftpClient.setDefaultTimeout(timeout);
        ftpClient.setConnectTimeout(timeout);

        connect(ftpInfo, ftpClient);

        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            System.out.println("Connected to " + ftpInfo.getHost());
        } else {
            System.err.println(ftpClient.getReplyString());
            throw new RuntimeException("Failed to connect the FTP server: " + ftpInfo.getHost());
        }

        ftpClient.setDataTimeout(timeout);
        ftpClient.setSoTimeout(timeout);

        String password = null;
        password = ftpInfo.getPassword();

        if (ftpClient.login(ftpInfo.getUserId(), password)) {
            System.out.println("Login successfully.");
        } else {
            System.err.println(ftpClient.getReplyString());
            System.err.println("FTP HOST:" + ftpInfo.getHost() + " FTP User ID:" + ftpInfo.getUserId() + " FTP Port:"
                + ftpInfo.getPort());
            throw new RuntimeException("Login failed.");
        }

        if (!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)) {
            System.err.println(ftpClient.getReplyString());
        }

        ftpClient.enterLocalPassiveMode();

        if (ftpClient instanceof FTPSClient) {
            FTPSClient ftpsClient = (FTPSClient) ftpClient;
            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");
        }

        ftpClient.setBufferSize(1048576);

        System.out.println("New ftp client is created: " + ftpClient);

        return ftpClient;
    }

    private static void connect(FtpInfo ftpInfo, FTPClient ftpClient) throws IOException {
        System.out.println("try to connect FTP. Host : " + ftpInfo.getHost() + ", port : " + ftpInfo.getPort());
        long retryInterval = Long.parseLong(System.getProperty("config.retryInterval", "10000"));
        int retryCount = Integer.parseInt(System.getProperty("config.retryCount", "5"));
        boolean retry = false;
        do {
            boolean needRetry = false;
            try {
                ftpClient.connect(ftpInfo.getHost(), ftpInfo.getPort());
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                needRetry = true;
            } catch (SocketException e) {
                e.printStackTrace();
                needRetry = true;
            }

            retry = needRetry && (retryCount-- > 0);

            if (retry) {
                System.out.println("Will retry to connect ftp server " + ftpInfo.getHost() + ". Retry count left: "
                    + retryCount);
                try {
                    Thread.sleep(retryInterval);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } while (retry);
    }

    public static void cleanExpirationFile(FTPClient ftpClient, String remoteDir, Date expirationDate)
        throws IOException {
        removeDirectory(ftpClient, remoteDir, expirationDate, false);
    }

    public static void removeDirectory(FTPClient ftpClient, String remoteDir) throws IOException {
        removeDirectory(ftpClient, remoteDir, null, true);
    }

    private static void removeDirectory(FTPClient ftpClient, String remoteDir, Date expirationDate, boolean delRootDir)
        throws IOException {
        FTPFile[] files = ftpClient.listFiles(remoteDir);
        if (files != null) {
            for (int i = 0; files != null && i < files.length; i++) {
                FTPFile file = files[i];
                String path = remoteDir + file.getName();
                if (file.isDirectory()) {
                    path += "/";
                    removeDirectory(ftpClient, path, expirationDate, true);
                } else {
                    if (needDelete(file, expirationDate)) {
                        boolean isSuccessful = ftpClient.deleteFile(path);
                        logDeleteResult(path, false, isSuccessful);
                    }
                }
            }

            if (delRootDir) {
                boolean isSuccessful = ftpClient.removeDirectory(remoteDir);
                logDeleteResult(remoteDir, true, isSuccessful);
            }
        }
    }

    private static void logDeleteResult(String path, boolean isDir, boolean isSuccessful) {
        String type = isDir ? "directory" : "file";
        if (isSuccessful) {
            System.out.println(type + ": " + path + " has been deleted successfully.");
        } else {
            System.out.println(type + ": " + path + " hasn't been deleted.");
        }
    }

    private static boolean needDelete(FTPFile file, Date expirationDate) {
        return expirationDate == null || file.getTimestamp().getTime().before(expirationDate);
    }

}
