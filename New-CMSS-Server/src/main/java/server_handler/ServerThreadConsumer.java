package server_handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;

import business.FileProcessReceive;
import business.FileProcessSender;
import config.Configuration;
import model.MessageDefs;
import model.QueueMessage;
import model.TLVMessage;
import server.Server;


public class ServerThreadConsumer implements Runnable { 

	private final BlockingQueue<TLVMessage> clientQueue;
	private final BlockingQueue<QueueMessage> serverQueue;
	private Server server;
	private ServerThreadListener serverThreadListener;
	private OutputStream output;
	private FileProcessReceive fileProcess;
	private FileProcessSender fileProcessSender;
	static Logger log = Logger.getLogger(ServerThreadConsumer.class.getName());
	private static final String serverFolder = Configuration.getProperties("server.folderDirectory");
	
	public ServerThreadConsumer(ServerThreadListener serverThreadListener) {
		this.clientQueue = serverThreadListener.getClientQueue();
		this.server = serverThreadListener.getServer();
		this.serverQueue = this.server.queue;
		this.serverThreadListener = serverThreadListener;
		fileProcess = new FileProcessReceive(this);
		fileProcessSender = new FileProcessSender();
	}
    //
	public void run() {
		try {
			this.output = serverThreadListener.getSocket().getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			try {
				TLVMessage tlv = clientQueue.take();
				int tag = (int)tlv.getTag();
				TLVMessage tlvResponse = new TLVMessage();
				int resultCode;
				String fileName;
				
				switch (tag) {
				case MessageDefs.MessageTypes.MT_LOGIN_REQ:
					server.checkUserLive();
					String username = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_USERID).getData());
					String password = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_USERPASS).getData());
					this.serverThreadListener.setUsername(username);
					server.addClient(serverThreadListener.getSocket(), username);
					System.out.println(username + " -- " + password);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_LOGIN_RES);
					tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, 200);
					log.info("Client " + username + " login to server");
					send(tlvResponse);
					break;
				case MessageDefs.MessageTypes.MT_SEND_TEXT_REQ:
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					System.out.println("ServerThread - MT_SEND_TEXT_REQ: receive code: " + resultCode);
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_SEND_TEXT_RES);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					if(resultCode == MessageDefs.CodeType.SERVER_INIT) {
						File fileEx = new File(serverFolder + "/" + fileName);
						if(fileEx.exists()) {
							System.out.println("ServerThread: File tồn tại");
							tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.FILE_EXIST);
						}
						else {
							System.out.println("ServerThread: File không tồn tại");
							tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.FILE_NOT_EXIST);
						}
					}
					else {
						tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.CLIENT_UPDATE);
						System.out.println("send test RES");
					}
					send(tlvResponse);
					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ:
					fileProcess.createFile(tlv);
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES);
					tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, resultCode);
					System.out.println("ServerThread: create file with code: " + resultCode);
					send(tlvResponse);
					addToQueue(tlv);
					log.info("Client " + this.serverThreadListener.getUsername() + " send file " + fileProcess.getFileName());
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ:
					fileProcess.appendFile(tlv);
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES);
					tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, resultCode);
					send(tlvResponse);
					addToQueue(tlv);
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ:
					fileName = fileProcess.getFileName();
					System.out.println("create file: "  + fileName + " success");
					fileProcess.closeFile(tlv);
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES);
					tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, resultCode);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					send(tlvResponse);
					log.info("Client " + this.serverThreadListener.getUsername() + " send success file " + fileName);
					addToQueue(tlv);
					System.out.println("ServerThread send MT_END_FILE_TRANSFER_REQ result code" + resultCode);
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_REQ:
					fileName = new String(tlv.getString(MessageDefs.FieldTypes.FT_FILE_NAME));
					System.out.println("ServerThread receive delete file request: " + fileName);
					fileProcess.deleteFile(tlv);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_FILE_DELETE_RES);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					send(tlvResponse);
					log.info("Client " + this.serverThreadListener.getUsername() + " delete file " + fileName);
					addToQueue(tlv);
					break;
				case MessageDefs.MessageTypes.MT_SYNCHRONIZE_REQ:
					fileProcessSender.start();
					send(fileProcessSender.getNextFileName());
					log.info("Client " + this.serverThreadListener.getUsername() + " request synchronize ");
					break;
					
					///////////////////////////////
				case MessageDefs.MessageTypes.MT_SEND_TEXT_RES:
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					System.out.println("ServerThread-MT_SEND_TEXT_RES: receive code: " + resultCode);
					if(resultCode == MessageDefs.CodeType.FILE_EXIST) {
						System.out.println("ServerThread: chuyển file khác");
						send(fileProcessSender.getNextFileName());
					}else {
						fileName = tlv.getString(MessageDefs.FieldTypes.FT_FILE_NAME);
						System.out.println("ServerThread: chuyển data file: " + fileName);
						fileProcessSender.startSendFile(fileName);
						send(fileProcessSender.getFragement());
					}
					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES:
					send(fileProcessSender.getFragement());
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_RES:
					send(fileProcessSender.getFragement());
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES:
					send(fileProcessSender.getNextFileName());
					break;
				case MessageDefs.MessageTypes.MT_HEAD_BEAT:
					break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void send(TLVMessage tlvResponse) {
		try {
			Thread.sleep(20);
			this.output.write(tlvResponse.flat());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addToQueue(TLVMessage tlv) {
		String userName = this.serverThreadListener.getUsername();
		QueueMessage message = new QueueMessage();
		message.setUserName(userName);
		message.setTlv(tlv);
		try {
			serverQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
