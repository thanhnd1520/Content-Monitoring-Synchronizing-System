package tcp;

import java.io.IOException;

import model.TLVMessage;

public interface TCPClientConectionState {
	public String checkConnection();
	public int connect(String serverAddress);
	public int disconnect();
	public int sendData(TLVMessage tlv) throws IOException;
}
