package business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;
import server_handler.ServerThreadConsumer;

public class FileProcessReceive {
	private File file;
	private FileOutputStream fou;
	private static final String serverFolder = Configuration.getProperties("server.folderDirectory");
	
	protected ServerThreadConsumer serverHandler;
	
	public FileProcessReceive(ServerThreadConsumer serverHandler) {
		super();
		this.serverHandler = serverHandler;
	}

	public int createFile(TLVMessage tlv) {
		try {
			String fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
			file = new File(serverFolder + "/" + fileName);
			fou = new FileOutputStream(file);
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			fou.write(data);
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
			fou.write(data);
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public int closeFile(TLVMessage tlv) {
		try {
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			fou.write(data);
			fou.close();
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			try {
				fou.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return 1;
		}
		return 0;
	}
	
	public void deleteFile(TLVMessage tlv) {
		String fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
		File fileDelete = new File(serverFolder + "/"  + fileName);
		System.out.println("delete file: " + fileDelete.getAbsolutePath());
		if(fileDelete.exists()) {
			fileDelete.delete();
		}
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
	
//	public void synchronize(OutputStream os) {
//		File folder = new File(serverFolder);
//		System.out.println(folder);
//		System.out.println(folder.list().length);
//		for ( File fileEntry : folder.listFiles()) {
//			System.out.println("send file: " + fileEntry.getName());
//			File file = new File(folder+"/"+fileEntry.getName());
//			try {
//				RandomAccessFile raf = new RandomAccessFile(file, "r");
//				long currentFilePos = 0;
//				long fileLength = raf.length();
//				raf.seek(currentFilePos);
//				while(fileLength - currentFilePos > 0) {
//					int chunkSize = (int)Math.min(fragementSize, fileLength - currentFilePos);
//	                byte [] chunk = new byte[chunkSize];
//	                raf.read(chunk);
//	                
//	                TLVMessage tlv = new TLVMessage();
//	                tlv.addBytes(MessageDefs.FieldTypes.FT_FILE_FRAGMENT, chunk);
//	                if(currentFilePos == 0) {
//	                	tlv.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ);;
//	                	tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileEntry.getName());
//	                }
//	                else if(fileLength - currentFilePos <= fragementSize) {
//	                	tlv.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
//	                	tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileEntry.getName());
//	                }
//	                else {
//	                	tlv.setTag(MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ);
//	                }
//	                serverHandler.send(tlv);
//	                currentFilePos = raf.getFilePointer();
//				}
//				raf.close();
//				if(fileLength == 0) {
//					TLVMessage tlv1 = new TLVMessage(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ);
//					tlv1.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileEntry.getName());
//					serverHandler.send(tlv1);
//					TLVMessage tlv2 = new TLVMessage(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
//					tlv2.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileEntry.getName());
//					serverHandler.send(tlv2);
//				}
//				else if(fileLength <= currentFilePos) {
//					TLVMessage tlv2 = new TLVMessage(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
//					tlv2.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileEntry.getName());
//					serverHandler.send(tlv2);
//				}
//				Thread.sleep(1500);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//	    }
//		
//		TLVMessage tlvEndSynchronize = new TLVMessage(MessageDefs.MessageTypes.MT_SYNCHRONIZE_RES);
//		serverHandler.send(tlvEndSynchronize);
//		
//	}
}
