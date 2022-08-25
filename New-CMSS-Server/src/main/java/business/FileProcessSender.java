package business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;

public class FileProcessSender {
	private static File file;
	private static RandomAccessFile raf;
	private static long currentFilePos;
	private static long fileLength;
	private static TLVMessage tlv;
	private BlockingQueue<TLVMessage> tlvQueue;
//	private Thread thread;
	private Set<String> listAllFile;
	
	private static final String serverFolder = Configuration.getProperties("server.folderDirectory");
	private static final int fragementSize = Integer.parseInt(Configuration.getProperties("fragementSize"));
	
	public FileProcessSender() {
	}

	public void start() {
		System.out.println("FileProcessSender: start synchronize to client");
		listAllFile = new HashSet<String>();
		File folder = new File(serverFolder);
		for (File SFile : folder.listFiles()) {
			listAllFile.add(SFile.getName());
		}
	}
	
	public TLVMessage getNextFileName() {
		if(listAllFile.size() <= 0) {
			TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SYNCHRONIZE_RES);
			System.out.println("FileProcessSender: server NOT File");
			return tlvRequest;
		}
		String fileName = listAllFile.iterator().next();
		System.out.println("FileProcessSender: return fileName: " + fileName + " to client");
		listAllFile.remove(fileName);
		TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SEND_TEXT_REQ);
		tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
		return tlvRequest;
	}
	
	public void startSendFile(String fileName) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				file = new File(serverFolder+"/"+fileName);
				try {
					tlvQueue = new LinkedBlockingQueue<>(); 
					raf = new RandomAccessFile(file, "r");
					currentFilePos = 0;
					fileLength = raf.length();
					System.out.println("----FileProcessSender----- file: " + fileName + " --length: " + fileLength);
					raf.seek(currentFilePos);
					while(fileLength - currentFilePos > 0) {
						int chunkSize = (int)Math.min(fragementSize, fileLength - currentFilePos);
						byte [] chunk = new byte[chunkSize];
						raf.read(chunk);
						tlv = new TLVMessage();
						tlv.addBytes(MessageDefs.FieldTypes.FT_FILE_FRAGMENT, chunk);
						tlv.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
						tlv.addInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS, (int)currentFilePos);
						if(currentFilePos == 0) {
							tlv.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ);;
							tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						}
						else if(fileLength - currentFilePos <= fragementSize) {
							tlv.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
							tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						}
						else {
							tlv.setTag(MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ);
						}
						try {
							tlvQueue.put(tlv);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						currentFilePos = raf.getFilePointer();
					}
					raf.close();
					if(fileLength == 0) {
						TLVMessage tlv1 = new TLVMessage(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ);
						tlv1.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						tlv1.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
						tlv1.addInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS, 0);
						tlvQueue.add(tlv1);
						TLVMessage tlv2 = new TLVMessage(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
						tlv2.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						tlv2.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
						tlv2.addInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS, 0);
						tlvQueue.add(tlv2);
						System.out.println(1);
					}
					else if(fileLength <= currentFilePos) {
						TLVMessage tlv2 = new TLVMessage(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
						tlv2.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						tlv2.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
						tlv2.addInt(MessageDefs.FieldTypes.FT_FILE_CURRENT_POS, (int)fileLength);
						tlvQueue.add(tlv2);
						System.out.println(2);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("FileProcessSender: end thread");
//			}
//		}).start();
	}
	
	public TLVMessage getFragement() {
		try {
			return tlvQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}

