package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;
import tcp.TCPClientConection;

public class ClientHandler {
	private static final String clientChangeFile = "clientChange.txt";
	private static final String clientDateFile = "clientDate.txt";
	private static final String path = Configuration.getProperties("client.folderDirectory");
	private ConcurrentHashMap<String, String> logChange; 
	private ConcurrentHashMap<String, String> logDate;
	private TCPClientConection conn;
	private File changeFile ;
	private File dateFile;
	
	public ClientHandler(TCPClientConection conn) {
		this.conn = conn;
		logChange = new ConcurrentHashMap<String, String>();
		logDate = new ConcurrentHashMap<String, String>();
		changeFile = new File(clientChangeFile);
		dateFile = new File(clientDateFile);
		readFromFile(changeFile, logChange);
		readFromFile(dateFile, logDate);
		compareChange();
		System.out.println("Start ClientConsumer");
		printLogChange();
	}

	public void submit() {
		try {
			synchronized (logChange) {
				if(WatchingFolder.checkUpdate) {
					WatchingFolder.checkUpdate = false;
				}
				Map.Entry<String,String> entry = logChange.entrySet().iterator().next();
				String fileName = entry.getKey();
				TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_SEND_TEXT_REQ);
				tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
				tlvRequest.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, MessageDefs.CodeType.CLIENT_UPDATE);
				try {
					this.conn.getSocket().getOutputStream().write(tlvRequest.flat());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}catch(NoSuchElementException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			WatchingFolder.checkUpdate = true;
			System.out.println("change logChange: True");
			return;
		}
	}
	
	public void addFileDelete(String fileName) {
		try {
			if(logChange.get(fileName) != null && logChange.get(fileName).equals("c")) {
				logChange.remove(fileName);
			}
			else if(logChange.get(fileName) != null && logChange.get(fileName).equals("s")){
				logChange.remove(fileName);
			}
			else {
				logChange.put(fileName, "d");
			}
			writeToFile(changeFile,logChange);
			getFileInFolder(logDate); 
			writeToFile(dateFile, logDate);
			printLogChange();
		}catch(Throwable e) {
			e.printStackTrace();
		}
		printLogChange();
	}
	public void addFileCreate(String fileName) {
		System.out.println("create file: " + fileName);
		if(logChange.containsKey(fileName) && logChange.get(fileName).equals("s")) {
			if(fileName.endsWith(".txt")) {
				logChange.remove(fileName);
			}
		}else {
			logChange.put(fileName, "c");
		}
		writeToFile(changeFile,logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
		printLogChange();
	}
	public void addFileRename(String oldName, String newName) {
		if(logChange.containsKey(oldName) && logChange.get(oldName).equals("c")) { // mới tạo và đổi tên luôn
			logChange.remove(oldName);
			logChange.put(newName, "c");
		}else {
			logChange.put(oldName, "d");
			logChange.put(newName, "c");
		}
		writeToFile(changeFile,logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
		printLogChange();
	}
	public void addFileUpdate(String fileName) {
		System.out.println("ClientHandler: update file: " + fileName);
		if(logChange.get(fileName) == null) {
			logChange.put(fileName, "u");
			writeToFile(changeFile,logChange);
		}
		else if(logChange.get(fileName).equals("s")) { 
			logChange.remove(fileName);
		}
		writeToFile(changeFile,logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
		printLogChange();
	}
	public void addServerEvenChange(String fileName) {
		logChange.remove(fileName);
		logChange.put(fileName, "s");
		writeToFile(changeFile,logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public void deleteServerEvenChange(String fileName) {
		logChange.remove(fileName);
	}
	public void sendLogin(String userName, String password) {
		TLVMessage tlv = new TLVMessage();
		tlv.setTag(MessageDefs.MessageTypes.MT_LOGIN_REQ);
		tlv.addString(MessageDefs.FieldTypes.FT_USERID, userName);
		tlv.addString(MessageDefs.FieldTypes.FT_USERPASS, password);
		conn.sendData(tlv);
		System.out.println("ClientConsumer: send login");
	}
	// xóa even log của client khi dã update file lên server
	public void deleteEvenLogChange(String fileName) {
		try {
			System.out.println("Log change trc khi xoa:");
			printLogChange();
			synchronized (logChange) {
				System.out.println("consumer: delete file name: " + fileName);
				logChange.remove(fileName);
			}
		}catch(NullPointerException e) {
			// xóa mà không có gì
		}
		writeToFile(changeFile,logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public String getEven(String fileName) {
		return logChange.get(fileName);
	}
	private void printLogChange() {
		System.out.println("PRINT LOGCHANGE ");
		logChange.forEach((k,v) ->{
			System.out.println(k + " -- " + v);
		});
		System.out.println("");
	}
	private void readFromFile(File file, ConcurrentHashMap<String, String> list) {
		try {
			if(!file.exists()) {
				file.createNewFile();
			}else if(file.length() != 0){
				FileInputStream fileIn = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				list = (ConcurrentHashMap<String, String>) in.readObject();
				in.close();
				fileIn.close();
				if(file.getName().equals(clientChangeFile)) {
					this.logChange = list;
				}
				else {
					this.logDate = list;
				}
				System.out.println("read file success");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void writeToFile(File file, ConcurrentHashMap<String, String> list) {
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(list);
			out.close();
			fileOut.close();
		}catch(FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void getFileInFolder(ConcurrentHashMap<String, String> list) {
		list.clear();
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
		}
		for ( File fileEntry : folder.listFiles()) {
	        String fileName = fileEntry.getName();
	        String time ="" + fileEntry.lastModified();
	        list.put(fileName, time);
	    }
	}
	
	//  detect file change offline
	private void compareChange() {
		ConcurrentHashMap<String, String> currentLogDate = new ConcurrentHashMap<String, String>();
		getFileInFolder(currentLogDate); // lấy cây thư mục hiên tại
		writeToFile(dateFile, currentLogDate); // cập nhật cây thư mục hiện giờ vào logDate
		if(currentLogDate != null && currentLogDate.size() > 0) {
			currentLogDate.forEach((k,v) ->{ /// so sánh với cây thư mục trc lúc cmss-client tắt đi
				if(logDate.containsKey(k)) {
					if(logDate.get(k).equals(v)) {
						
					}else {
						if(logChange.get(k) != null && !logChange.get(k).equals("c")) {
							logChange.put(k, "u");
						}
					}
					System.out.println("LOGDATE constans CurrentLogDate");
					logDate.remove(k);
				}else {
					System.out.println("LOGDATE not constans CurrentLogDate");
					logChange.put(k, "c");
				}
			});
		}
		try {
			if(logDate.size() > 0) {
				List<String> list = new LinkedList<String>();
				logDate.forEach((k,v) ->{
					list.add(k);
				});
				if(list.size() > 0) {
					for(String fileName : list) {
						if(logChange.get(fileName) != null && logChange.get(fileName).equals("c")) {
							System.out.println(fileName);
							logChange.remove(fileName);
						}else {
							logChange.put(fileName, "d");
						}
					}
				}
			}
			writeToFile(changeFile, logChange);
		}catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public Set<String> getListFile() {
		Set<String> set = new HashSet<String>();
		File folder = new File(path);
		for ( File fileEntry : folder.listFiles()) {
			String fileName = fileEntry.getName();
			set.add(fileName);
		}
		return set;
	}

	
}	
