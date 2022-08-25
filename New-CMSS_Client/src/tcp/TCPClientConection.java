package tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

import client.ClientHandler;
import client.ClientListener;
import client.ClientView;
import client.WatchingFolder;
import config.Configuration;
import model.TLVMessage;


public class TCPClientConection {

	private TCPClientConectionState state;
	private Socket socket;
	private int portNumber;
	private int reconectRetry;
	private String serverAddress;
	private CheckTask checkTask;
	private ClientListener clientListener = null;
	private Thread clientHandlerThread = null;
	private ClientHandler clientHandler;

	ClientView view;
	
	public TCPClientConection() {
		this.portNumber = Integer.parseInt( Configuration.getProperties("server.portNumber"));
		serverAddress = Configuration.getProperties("server.address");
		this.state = new DisconnectedState(this);
		System.out.println(this.portNumber + this.serverAddress);
		this.connect(serverAddress);
		checkTask = new CheckTask(this);
	}
	
	// method function
	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
		if(this.clientListener != null) {
			this.clientListener.setClientHandler(clientHandler);
		}
	}
	
	public void startListen() {
		System.out.println("start listen");
		if(clientListener == null) {
			this.clientListener = new ClientListener(this, this.clientHandler);
			this.clientHandlerThread = new Thread(this.clientListener);
			this.clientListener.setClientHandler(clientHandler);
			this.clientHandlerThread.start();
		}
		else {
			clientListener.setConn(this);
		}
	}
	

	public void changeState(TCPClientConectionState state) {
		if(!(this.state instanceof ConnectedState) && (state instanceof ConnectedState)) {
			startListen();
			this.state = state;
		}
		else {
			this.state = state;
			if(!(state instanceof ConnectedState)) {
				this.setCheckLoginView(false);
			}
		}
	}
	public void checkConnection() {
		synchronized (this.state) {
			String connectString = state.checkConnection();
			view.setConnectStringView(connectString);
			//System.out.println(connectString);
		}
	}
	public int connect(String serverAddress) {
		return state.connect(serverAddress);
	}
	public int disconnect() {
		return state.disconnect();
	}
	public int sendData(TLVMessage tlv){
		try {
			return state.sendData(tlv);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("send data error");
		return -1;
	}
	public void setCheckLoginView(boolean check) {
		if(check && !WatchingFolder.checkUpdate) {
			clientHandler.submit();
		}
		this.view.setCheckLogin(check);
	}
	// run checkTask cho socket<kiem tra connect giua client va server>
	// chạy sau khi login thành công
	public void runCheckTask() {
		Timer timer = new Timer();
		timer.schedule(checkTask, 0, 1000);
	}
	
	// seter and geter property
	public void setView(ClientView view) {
		this.view = view;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public ClientListener getClientHandler() {
		return clientListener;
	}

	public void setClientListener(ClientListener clientListener) {
		this.clientListener = clientListener;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getReconectRetry() {
		return reconectRetry;
	}
	public void setReconectRetry(int reconectRetry) {
		this.reconectRetry = reconectRetry;
	}
	public TCPClientConectionState getState() {
		return state;
	}
	public void setState(TCPClientConectionState state) {
		this.state = state;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public Socket getSocket() {
		return socket;
	}
	

}
