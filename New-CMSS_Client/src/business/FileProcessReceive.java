package business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;

public class FileProcessReceive {
	private File file;
	private FileOutputStream fou;
	private RandomAccessFile raf;
	private int currentPointer;
	private static final String clientFolder = Configuration.getProperties("client.folderDirectory");
	
	public int createFile(TLVMessage tlv) {
		try {
			String fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
			file = new File(clientFolder + "/" + fileName);
			raf = new RandomAccessFile(file, "rw");
			raf.setLength(0);
			raf.seek(0);
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			raf.write(data);
			System.out.println("FileProcessReceive: create file: " + fileName);
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			return 1; 
		}
		return 0;
	}
	
	public int appendFile(TLVMessage tlv) {
		try {
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			currentPointer = tlv.getInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS);
			raf.seek(currentPointer);
			raf.write(data);
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NullPointerException e) {
			return 1; 
		}
		return 0;
	}
	public int closeFile(TLVMessage tlv) {
		try {
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			currentPointer = tlv.getInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS);
			raf.seek(currentPointer);
			raf.write(data);
			raf.close();
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			try {
				raf.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return 1;
		}
		return 0;
	}
	
	public String getFileName() {
		return file.getName();
	}
	
	
	public FileProcessReceive() {
		super();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public FileOutputStream getFou() {
		return fou;
	}

	public void setFou(FileOutputStream fou) {
		this.fou = fou;
	}
}
