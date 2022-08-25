package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import config.Configuration;
import model.TLVMessage;
import tcp.TCPClientConection;

public class ClientListener implements Runnable{
	
	private TCPClientConection conn;
	private ClientHandler clientHandler;
	private ClientConsumer consumer;
	private InputStream input;
	private final BlockingQueue<TLVMessage> queue = new LinkedBlockingQueue<>();
	
	public ClientListener(TCPClientConection conn, ClientHandler clientHandler) {
		this.conn = conn;
		this.clientHandler = clientHandler;
		this.consumer = new ClientConsumer(queue, conn, clientHandler);
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();
	}

	public void setConn(TCPClientConection conn) {
		this.conn = conn;
		try {
			this.input = conn.getSocket().getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
		this.consumer.setClientHandler(clientHandler);
	}

	@Override
	public void run() {
		try {
			System.out.println("start listenning");
			input = this.conn.getSocket().getInputStream();
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
					queue.put(tlv);
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
		} 
	}
	
	
}
