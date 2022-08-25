package server;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import model.QueueMessage;
import model.TLVMessage;

public class ServerConsumer implements Runnable {

	private final BlockingQueue<QueueMessage> queue;
	private Server server;

	public ServerConsumer(BlockingQueue<QueueMessage> queue, Server server) {
		
		this.queue = queue;
		this.server = server;
	}
    //
	public void run() {
		while (true) {
			try {
				QueueMessage message = queue.take();
				String userName = message.getUserName();
				TLVMessage tlv = message.getTlv();
				sendAll(tlv, userName);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendAll(TLVMessage tlv, String userName) {
		this.server.clientsOutputStream.forEach((k,v) ->{
			if(!k.equals(userName)) {
				System.out.println("send to client: " + k);
				try {
					v.getOutputStream().write(tlv.flat());
				} catch (IOException e) {
					server.removeClient(userName);
					e.printStackTrace();
				}
			}
		});
	}
	
//	private void sendFileToAllClient(String fileName, String userName) {
//		server.checkUserLive();
//		File file = new File(folder + "/" + fileName);
//		try {
//			RandomAccessFile raf = new RandomAccessFile(file, "r");
//			long currentFilePos = 0;
//			long fileLength = raf.length();
//			raf.seek(currentFilePos);
//			while(fileLength - currentFilePos > 0) {
//				int chunkSize = (int)Math.min(fragementSize, fileLength - currentFilePos);
//                byte [] chunk = new byte[chunkSize];
//                raf.read(chunk);
//                
//                TLVMessage tlv = new TLVMessage();
//                tlv.addBytes(MessageDefs.FieldTypes.FT_FILE_FRAGMENT, chunk);
//                if(currentFilePos == 0) {
//                	tlv.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ);;
//                	tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
//                }
//                else if(fileLength - currentFilePos <= fragementSize) {
//                	tlv.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ);
//                	tlv.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
//                }
//                else {
//                	tlv.setTag(MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ);
//                }
//                currentFilePos = raf.getFilePointer();
//                sendAll(tlv, userName);
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	private void deleteFileToAllClient(String fileName, String userName) {
//		server.checkUserLive();
//		TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_FILE_DELETE_REQ);
//		System.out.println(" ServerConsumer send delete: " + fileName);
//		tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
//		sendAll(tlvRequest, userName);	
//	}

}
