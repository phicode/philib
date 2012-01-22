package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import ch.bind.philib.net.SocketAddresses;

public class TcpServer2 {
	// TODO: configurable
	private static final int DEFAULT_BACKLOG = 100;

	private ServerSocket serverSocket;

	void open(SocketAddress endpoint) throws IOException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(endpoint, DEFAULT_BACKLOG);
		System.out.println("listening on: " + endpoint);
		this.serverSocket = serverSocket;
	}

	public void run() throws IOException {
		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setReceiveBufferSize(16 * 1024);
			clientSocket.setSendBufferSize(16 * 1024);
			new Thread(new SocketHandler(clientSocket)).start();
		}
	}

	public static void main(String[] args) throws IOException {
		TcpServer2 server = new TcpServer2();
		InetSocketAddress endpoint = SocketAddresses.wildcard(1234);
		server.open(endpoint);
		server.run();
	}

	private static class SocketHandler implements Runnable {
		private final Socket clientSocket;

		public SocketHandler(Socket clientSocket) throws IOException {
			super();
			this.clientSocket = clientSocket;
			// clientChannel.configureBlocking(true);
			// System.out.println("send-buffer-size: " +
			// clientChannel.socket().getSendBufferSize());
			// System.out.println("recv-buffer-size: " +
			// clientChannel.socket().getReceiveBufferSize());
			// System.out.println("tcp-no-delay    : " +
			// clientChannel.socket().getTcpNoDelay());
			clientSocket.setTcpNoDelay(true);
			// clientSocket.setTrafficClass(0x08);
			// clientSocket.set
		}

		@Override
		public void run() {
			byte[] buffer = new byte[16 * 1024];
			long total = 0;
			long lastPrintTime = 0;
			long start = System.currentTimeMillis();
			try {
				InputStream in = clientSocket.getInputStream();
				OutputStream out = clientSocket.getOutputStream();
				while (true) {
					// System.out.println("in: " + in.available());
					int len = in.read(buffer);
					if (len == -1) {
						System.out.println("connection closed");
						return;
					} else {
						out.write(buffer, 0, len);
						out.flush();
						total += len;
					}
					long now = System.currentTimeMillis();
					if (now > (lastPrintTime + 1000)) {
						lastPrintTime = now;
						double mpPerSec = (total / (1024 * 1024)) / ((now - start) / 1000f);
						System.out.println("echoed " + total + " bytes => " + mpPerSec + " mb/sec");
					}
				}
			} catch (IOException e) {
				System.out.println("io-exc: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
