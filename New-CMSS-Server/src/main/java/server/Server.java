package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import config.Configuration;
import model.MessageDefs;
import model.QueueMessage;
import model.TLVMessage;
import server_handler.ServerThreadListener;

public class Server {
	private ServerSocket serverSocket;
	private Socket socket;
	public Hashtable<String, Socket> clientsOutputStream; 
	public final BlockingQueue<QueueMessage> queue = new LinkedBlockingQueue<>();
	private ServerConsumer consumer; // xử lý tất cả các message gửi lên từ client
	static Logger log = Logger.getLogger(Server.class.getName());
	
	public Server(int port)  {
		try {
			PropertyConfigurator.configure(Configuration.getProperties("server.log4jDirectory"));
			clientsOutputStream = new Hashtable<String, Socket>();
			serverSocket = new ServerSocket(port);
			consumer = new ServerConsumer(this.queue, this);
			Thread serverListenerThread = new Thread(consumer);
			serverListenerThread.start();
			System.out.println(serverSocket.getLocalPort());
			log.info("SERVER START");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void waitingForClient() {
		while(true){
			try {
				socket = serverSocket.accept();
				ServerThreadListener client =  new ServerThreadListener(this, socket);
				System.out.println(1);
				Thread clientThread = new Thread(client);
				clientThread.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	//send file to all client
	public void newFileUpdate(String fileName, String clientName) {
		
	}
	// add client to hash table
	public void addClient(Socket socket, String username) {
		synchronized (clientsOutputStream){
			clientsOutputStream.put(username, socket);
			System.out.println("List client");
			clientsOutputStream.forEach((k,v) ->{
				System.out.println(k);
			});
		}
	}
	
	//Removing the client from the client hash table
	public void removeClient(String username) {
		synchronized (clientsOutputStream){
			try {
				clientsOutputStream.remove(username);
			}catch(NullPointerException ex) {
				
			}
		}
	}
	public void checkUserLive() {
		List<String> list = new LinkedList<String>();
		synchronized (clientsOutputStream){
			clientsOutputStream.forEach((k,v) ->{
				TLVMessage tlvPing = new TLVMessage(MessageDefs.MessageTypes.MT_HEAD_BEAT);
				try {
					v.getOutputStream().write(tlvPing.flat());
				} catch (IOException e) {
					list.add(k);
				}
			});
			for(String user : list) {
				removeClient(user);
			}
		}
	}
}
