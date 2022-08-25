package tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import model.TLVMessage;

public class ConnectingState implements TCPClientConectionState {

	private TCPClientConection tcpClientConnect;

	public ConnectingState(TCPClientConection tcpClientConection) {
		this.tcpClientConnect = tcpClientConection;
		this.tcpClientConnect.setReconectRetry(0);
	}

	@Override
	public String checkConnection() {
		try {
			if (tcpClientConnect.getReconectRetry() < 5) {
				Socket socket = new Socket(tcpClientConnect.getServerAddress(), this.tcpClientConnect.getPortNumber());
				this.tcpClientConnect.setSocket(socket);
				if(tcpClientConnect.getSocket()!= null && tcpClientConnect.getSocket().isConnected()) {
					tcpClientConnect.changeState(new ConnectedState(tcpClientConnect));
					System.out.println("change from connecting to connected");
					return "connected";
				}
				else {
					this.connect(tcpClientConnect.getServerAddress());
					tcpClientConnect.setReconectRetry(tcpClientConnect.getReconectRetry() + 1);
					return "connecting";
				}
			} else {
				tcpClientConnect.changeState(new DisconnectedState(tcpClientConnect));
				System.out.println("Connect fail. Client disconneted to server.");
				System.out.println("change from connecting to disconnect");
				return "disconnected";
			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		return "disconnect";
	}

	@Override
	public int connect(String serverAddress) {
		try {
			Socket socket = new Socket(serverAddress, this.tcpClientConnect.getPortNumber());
			this.tcpClientConnect.setSocket(socket);
			if (this.tcpClientConnect.getSocket().isConnected()) {
				this.tcpClientConnect.changeState(new ConnectedState(tcpClientConnect));
				System.out.println("change from Connecting to Connected");
			} else {
				this.tcpClientConnect.changeState(new ConnectingState(tcpClientConnect));
			}
		} catch (UnknownHostException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		}
		return 1;
	}

	@Override
	public int disconnect() {
		this.tcpClientConnect.changeState(new DisconnectedState(tcpClientConnect));
		return 0;
	}

	@Override
	public int sendData(TLVMessage tlv) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
