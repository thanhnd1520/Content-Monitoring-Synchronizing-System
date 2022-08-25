package tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import model.TLVMessage;

public class DisconnectedState implements TCPClientConectionState {
	private TCPClientConection tcpClientConnect;

	public DisconnectedState(TCPClientConection tcpClientConection) {
		this.tcpClientConnect = tcpClientConection;
		this.tcpClientConnect.setReconectRetry(0);
	}

	@Override
	public String checkConnection() {
		this.tcpClientConnect.changeState(new ConnectingState(tcpClientConnect));
		return "disconnected";
	}

	@Override
	public int connect(String serverAddress) {
		try {
			Socket socket = new Socket(serverAddress, this.tcpClientConnect.getPortNumber());
			this.tcpClientConnect.setSocket(socket);
			if (this.tcpClientConnect.getSocket()!= null && this.tcpClientConnect.getSocket().isConnected()) {
				this.tcpClientConnect.changeState(new ConnectedState(tcpClientConnect));
				System.out.println("change from disconnect to connected");
			} else {
				this.tcpClientConnect.changeState(new ConnectingState(tcpClientConnect));
				System.out.println("change from disconnect to connecting");
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
		System.out.println("Client is disconnect.....");
		return -1;
	}

	@Override
	public int sendData(TLVMessage tlv) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
