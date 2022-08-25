package client;

import org.apache.log4j.PropertyConfigurator;

import config.Configuration;
import tcp.TCPClientConection;

public class ClientRuner {
	private ClientView viewer;
	private WatchingFolder watcher;
	private TCPClientConection conn;
	private ClientHandler clientHandler;
	
	public ClientRuner() {
		PropertyConfigurator.configure(Configuration.getProperties("client.log4jDirectory"));
		WatchingFolder.checkUpdate = true;
		this.conn = new TCPClientConection();
		this.clientHandler = new ClientHandler(conn);
		this.conn.setClientHandler(clientHandler);
		this.viewer = new ClientView();
		viewer.setClientHandler(clientHandler);
		conn.setView(viewer);
		this.watcher = new WatchingFolder(clientHandler);
		conn.runCheckTask();
		Thread watchingFolderThread = new Thread(watcher);
		watchingFolderThread.start();
	}
	
	public static void main(String[] args) {
		ClientRuner client = new ClientRuner();
	}
	
}
