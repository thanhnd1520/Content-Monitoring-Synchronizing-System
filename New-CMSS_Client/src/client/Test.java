package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Test {
	public static void main(String[] args) {
		try {
			File file1 = new File("D:\\cmss\\clientFolder\\aaa.txt");
			File file2 = new File("D:\\cmss\\clientFolder\\aaa.txt");
			
			RandomAccessFile raf1 = new RandomAccessFile(file1,"rw");
//			FileChannel fileChanel = raf1.getChannel();
//			FileLock fileLock = fileChanel.lock();
			raf1.setLength(0);
			raf1.write(("123456789").getBytes());
			raf1.seek(3);
			raf1.write(("abcd").getBytes());
			raf1.write(("ABCD").getBytes());
			RandomAccessFile raf2 = new RandomAccessFile(file2,"rw");
//			raf2.setLength(0);
			raf2.write(("aa").getBytes());
			raf2.seek(4);
			raf1.read();
			raf2.close();

//			FileOutputStream fou1 = new FileOutputStream(file1);
//			fou1.write(("aa").getBytes());
//			FileOutputStream fou2 = new FileOutputStream(file2);
////			fou2.write(("bb").getBytes());
////			fou1.close();
////			fou2.close();
////			Thread.sleep(1000000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
