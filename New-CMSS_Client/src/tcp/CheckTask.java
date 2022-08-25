package tcp;

import java.util.TimerTask;

public class CheckTask extends TimerTask{

	private TCPClientConection context;
	
	public CheckTask(TCPClientConection context) {
		this.context = context;
	}
	
	public void setContext(TCPClientConection context) {
		this.context = context;
	}
	@Override
	public void run() {
		context.checkConnection();
	}
	
}
