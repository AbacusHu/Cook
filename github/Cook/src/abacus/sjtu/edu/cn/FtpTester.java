package abacus.sjtu.edu.cn;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FtpTester {

	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException {
		FtpInfo ftp = new FtpInfo("ftps", "127.0.0.1", "userId", "password");
		FTPClient ftpClient = FtpService.createFtpClient(ftp);
		FTPFile[] files = ftpClient.listFiles("/");
		for (FTPFile file : files) {
			System.out.println(file.toString());
		}

	}

}
