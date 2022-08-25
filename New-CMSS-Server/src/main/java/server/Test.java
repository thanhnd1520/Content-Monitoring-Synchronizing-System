package server;


import java.nio.ByteBuffer;

public class Test {
	public static void main(String[] argr) {
		ByteBuffer buffer = ByteBuffer.allocate(0);
		buffer = ByteBuffer.allocate(8);
		buffer.putInt(10);
		buffer.putInt(4);
		buffer.position(4);
		buffer.reset();
		
		System.out.println(buffer.getInt());
		System.out.println("--" + buffer.array().length);
	}
}
