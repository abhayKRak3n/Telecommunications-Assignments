import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class User extends Node implements Runnable {

	private Terminal terminal;

	User(Terminal terminal, int port) {
		try {
			this.terminal = terminal;
			socket = new DatagramSocket(port);
			listener.go();
		} catch (SocketException e) { e.printStackTrace(); }
	}

	public void onReceipt(DatagramPacket packet) {
		byte[] data;
		String content;
		byte[] buffer;

		data = packet.getData();

		buffer = new byte[data[CONSTANTS.LENGTH_POS]];
		System.arraycopy(data, CONSTANTS.HEADER_LENGTH, buffer, 0, buffer.length);
		content = new String(buffer);

		terminal.println("Message : " + content);
	}

	private synchronized void sendMessage(String input, int port) {
		byte[] buffer;
		byte[] data;
		DatagramPacket packet;

		buffer = input.getBytes();
		data = new byte[CONSTANTS.HEADER_LENGTH + buffer.length];
		data[CONSTANTS.TYPE_POS] = CONSTANTS.STRING_TYPE;
		data[CONSTANTS.LENGTH_POS] = (byte)buffer.length;

		System.arraycopy(buffer, 0, data, CONSTANTS.HEADER_LENGTH, buffer.length);

		packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(new InetSocketAddress(CONSTANTS.DSTHOST, port));

		try {
			socket.send(packet);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		while (true) {
			terminal.println("Enter message to be sent ");
			String input = terminal.read("-->");

			sendMessage(input, CONSTANTS.STARTROUTER);
		}
	}
}