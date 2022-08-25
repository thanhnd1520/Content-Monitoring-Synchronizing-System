package tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import model.MessageDefs;
import model.TLVMessage;

public class ConnectedState implements TCPClientConectionState {

	private TCPClientConection tcpClientConnect;

	public ConnectedState(TCPClientConection tcpClientConection) {
		this.tcpClientConnect = tcpClientConection;
		this.tcpClientConnect.setReconectRetry(0);
	}

	@Override
	public String checkConnection() {
			try {
				TLVMessage tlv = new TLVMessage(MessageDefs.MessageTypes.MT_HEAD_BEAT);
				sendData(tlv);
				return "connected";
			} catch (IOException e) {
				e.printStackTrace();
				this.tcpClientConnect.changeState(new DisconnectedState(tcpClientConnect));
				this.tcpClientConnect.setSocket(null);
				return "Disconnected";
			}
	}

	@Override
	public int connect(String serverAddress) {
		try {
			// disconnect server cũ
			this.disconnect();
			this.tcpClientConnect.setSocket(new Socket(serverAddress, tcpClientConnect.getPortNumber()));

			if (this.tcpClientConnect.getSocket().isConnected()) {
				this.tcpClientConnect.changeState(new ConnectedState(tcpClientConnect));
				System.out.println("change from Connected to Connected");
			} else {
				this.tcpClientConnect.changeState(new ConnectingState(tcpClientConnect));
				System.out.println("change from Connected to Connecting");
				System.out.println("Client connect fail.");
			}
		} catch (UnknownHostException e) {
			System.err.println("Client connect fail.");
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	@Override
	public int disconnect() {
		try {
			tcpClientConnect.getSocket().close();
			tcpClientConnect.changeState(new DisconnectedState(tcpClientConnect));
		} catch (IOException e) {
			System.err.println("Error closing socket");
			return -1;
		}
		return 1;
	}

	@Override
	public int sendData(TLVMessage tlv) throws IOException {
		byte[] dataBytes = tlv.flat();
		this.tcpClientConnect.getSocket().getOutputStream().write(dataBytes);
		return 1;
	}
}
