package server;


public class ServerRuner {
	public static void main(String[] args) {
		Server server= new Server(8888);
		server.waitingForClient();
	}
}
