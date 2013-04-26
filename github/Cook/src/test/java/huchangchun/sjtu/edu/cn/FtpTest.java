package huchangchun.sjtu.edu.cn;

import huchangchun.sjtu.edu.cn.FtpInfo;
import huchangchun.sjtu.edu.cn.FtpService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.testng.annotations.Test;

public class FtpTest {

    @Test
    public void listFtpsFiles() throws NoSuchAlgorithmException, IOException {
        FtpInfo ftp = new FtpInfo("ftps", "172.17.254.207", 22, "jon", "jon");
        FTPClient ftpClient = FtpService.createFtpClient(ftp);
        FTPFile[] files = ftpClient.listFiles("/");
        for (FTPFile file : files) {
            System.out.println(file.toString());
        }
    }

}
