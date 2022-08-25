package client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import business.FileProcessReceive;
import business.FileProcessSender;
import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;
import tcp.TCPClientConection;

public class ClientConsumer implements Runnable {
	private BlockingQueue<TLVMessage> queue;
	private TCPClientConection conn;
	private ClientHandler clientHandler;
	private FileProcessReceive fileProcessReceive;
	private FileProcessSender fileProcessSender;
	private Set<String> listFile;
	static Logger log = Logger.getLogger(ClientConsumer.class.getName());

	private final String clientFolder = Configuration.getProperties("client.folderDirectory");

	public ClientConsumer(BlockingQueue<TLVMessage> queue, TCPClientConection conn, ClientHandler clientHandler) {
		this.queue = queue;
		this.conn = conn;
		this.clientHandler = clientHandler;
		fileProcessReceive = new FileProcessReceive();
		fileProcessSender = new FileProcessSender();
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	@Override
	public void run() {
		log.info("Connect to server");
		while (true) {
			try {
				TLVMessage tlv = queue.take();
				int tag = tlv.getTag();
				String fileName;
				String even;
				int resultCode;
				TLVMessage tlvRequest = new TLVMessage();
				switch (tag) {
				case MessageDefs.MessageTypes.MT_SEND_TEXT_RES:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					if (resultCode == MessageDefs.CodeType.CLIENT_UPDATE) {
						even = clientHandler.getEven(fileName);
						System.out.println("MT_SEND_TEXT_RES: " + fileName);
						if (even.equals("d")) {
							tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_FILE_DELETE_REQ);
							tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
							try {
								this.conn.getSocket().getOutputStream().write(tlvRequest.flat());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							fileProcessSender.startSendFile(fileName);
							tlvRequest = fileProcessSender.getFragement();
							tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE,
									MessageDefs.CodeType.CLIENT_UPDATE);
							sendTlv(tlvRequest);
							System.out.println("ClientConsumer: send thành công");
						}
					} else if (resultCode == MessageDefs.CodeType.FILE_NOT_EXIST) {
						fileProcessSender.startSendFile(fileName);
						tlvRequest = fileProcessSender.getFragement();
						tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
						sendTlv(tlvRequest);
						System.out.println("ClientConsumer: send thành công");
					} else if (resultCode == MessageDefs.CodeType.FILE_EXIST) {
						tlvRequest = getClientFileName();
						sendTlv(tlvRequest);
					}

					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES:
					tlvRequest = fileProcessSender.getFragement();
					tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE,
							tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE));
					sendTlv(tlvRequest);
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_RES:
					tlvRequest = fileProcessSender.getFragement();
					tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE,
							tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE));
					sendTlv(tlvRequest);
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES:
					resultCode = tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE);
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					clientHandler.deleteEvenLogChange(fileName);
					System.out.println("ClientConsumer: MT_END_FILE_TRANSFER_RES result code: " + resultCode);
					if (resultCode == MessageDefs.CodeType.CLIENT_UPDATE) {
						System.out.println("server send end file: " + fileName);
						Thread.sleep(1000);
						clientHandler.submit();
					} else {
						System.out.println("ClientConsumer: update INIT Server");
						tlvRequest = getClientFileName();
						if (tlvRequest != null) {
							sendTlv(tlvRequest);
						} else {
							tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SYNCHRONIZE_REQ);
							sendTlv(tlvRequest);
						}
					}
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_RES:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					clientHandler.deleteEvenLogChange(fileName);
					Thread.sleep(100);
					clientHandler.submit();
					break;
				case MessageDefs.MessageTypes.MT_LOGIN_RES:
					System.out.println("ClientConsumer: seceive login result");
					conn.setCheckLoginView(true);
					listFile = clientHandler.getListFile();
					tlvRequest = getClientFileName();
					WatchingFolder.checkUpdate = false;
					if (tlvRequest != null) {
						System.out.println("ClientConsumer: send INIT to server");
						sendTlv(tlvRequest);
					} else {
						System.out.println("ClientConsumer: start synchronize");
						tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SYNCHRONIZE_REQ);
						sendTlv(tlvRequest);
					}
					break;

				/// server tranfer from other client
				case MessageDefs.MessageTypes.MT_SEND_TEXT_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					File file = new File(clientFolder + "\\" + fileName);
					System.out.println("ClientConsumer: receive fileName: " + fileName);
					tlvRequest.setTag(MessageDefs.MessageTypes.MT_SEND_TEXT_RES);
					tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					if (file.exists()) {
						System.out.println("ClientConsumer-MT_SEND_TEXT_REQ: filename " + fileName + " exist");
						tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.FILE_EXIST);
					} else {
						System.out.println("ClientConsumer-MT_SEND_TEXT_REQ: fileName: " + fileName + " not exist");
						tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.FILE_NOT_EXIST);
					}
					sendTlv(tlvRequest);
					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					clientHandler.addServerEvenChange(fileName);
					fileProcessReceive.createFile(tlv);
					if (tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE) == MessageDefs.CodeType.SERVER_INIT) {
						tlvRequest.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES);
						tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						sendTlv(tlvRequest);
					}
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ:
					fileProcessReceive.appendFile(tlv);
					if (tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE) == MessageDefs.CodeType.SERVER_INIT) {
						tlvRequest.setTag(MessageDefs.MessageTypes.MT_FILE_FRAGMENT_RES);
						sendTlv(tlvRequest);
					}
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					fileProcessReceive.closeFile(tlv);
					clientHandler.deleteEvenLogChange(fileName);
					System.out.println("server send and file: " + fileName);
					if (tlv.getInt(MessageDefs.FieldTypes.FT_RESULT_CODE) == MessageDefs.CodeType.SERVER_INIT) {
						tlvRequest.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES);
						tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						sendTlv(tlvRequest);
					}
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					System.out.println("ClientConsumer delete file: " + fileName);
					deleteFile(fileName);
					break;
				case MessageDefs.MessageTypes.MT_SYNCHRONIZE_RES:
					WatchingFolder.checkUpdate = true;
					System.out.println("ClientConsumer-MT_SYNCHRONIZE_RES: checkUpdate = true");
				default:
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private void deleteFile(String fileName) {
		WatchingFolder.checkUpdate = false;
		String folder = Configuration.getProperties("client.folderDirectory");
		System.out.println("folder: " + folder);
		System.out.println("fileName: " + fileName);
		java.io.File file = new java.io.File(folder + "\\" + fileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WatchingFolder.checkUpdate = true;
	}

	private void sendTlv(TLVMessage tlv) {
		try {
			conn.getSocket().getOutputStream().write(tlv.flat());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private TLVMessage getClientFileName() {
		if (listFile.size() > 0) {
			TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SEND_TEXT_REQ);
			String fileName = listFile.iterator().next();
			tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
			tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.SERVER_INIT);
			listFile.remove(fileName);
			return tlvRequest;
		}
		TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SYNCHRONIZE_REQ);
		return tlvRequest;
	}
}
