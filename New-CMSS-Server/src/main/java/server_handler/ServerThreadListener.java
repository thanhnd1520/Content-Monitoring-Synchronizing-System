package server_handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import model.TLVMessage;
import server.Server;

public class ServerThreadListener implements Runnable {

	private Server server;
	private Socket socket;
	private InputStream input;
	private String username;
	private BlockingQueue<TLVMessage> clientQueue;
	private ServerThreadConsumer clientThreadConsumer;

	public ServerThreadListener(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		clientQueue = new LinkedBlockingQueue<>();
		clientThreadConsumer = new ServerThreadConsumer(this);
		Thread clientComsumerThread = new Thread(clientThreadConsumer);
		clientComsumerThread.start();
	}
	public void run() {
		try {
			System.out.println("client accept");
			input = this.socket.getInputStream();
			ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
			while (true) {
				if (input.available() <= 0 && byteBuffer.position() < 5) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				int min = Math.min(byteBuffer.remaining(), input.available());
				byte[] b = new byte[min];
				input.read(b);
				byteBuffer.put(b);
				if(byteBuffer.capacity() < 5) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				byte[] lengthByte = new byte[4];
				System.arraycopy(byteBuffer.array(), 0, lengthByte, 0, 4);
				int length = ByteBuffer.wrap(lengthByte).getInt();
				if(byteBuffer.position() < 4 + length) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				byteBuffer.flip();
				length = byteBuffer.getInt();
				byte[] dataByte = new byte[length];
				byteBuffer.get(dataByte);
				TLVMessage tlv = new TLVMessage();
				tlv.parse(length, dataByte);
				try {
					clientQueue.put(tlv);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				byteBuffer.compact();
			}
			
		} catch (EOFException e) { // TH socket đóng
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			server.removeClient(username);
		}
	}
	
	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.input = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public BlockingQueue<TLVMessage> getClientQueue() {
		return clientQueue;
	}
}
