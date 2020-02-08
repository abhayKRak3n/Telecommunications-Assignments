import java.io.*;
import java.net.*;

public class Router extends Node implements Runnable {

	private Terminal terminal;
	private int port;
	private String message;
	private int destination;

	Router(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			this.port = port;
			socket = new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		String content;
		byte[] data;
		byte[] buffer;

		data = packet.getData();

		buffer = new byte[data[CONSTANTS.LENGTH_POS]];
		System.arraycopy(data, CONSTANTS.HEADER_LENGTH, buffer, 0, buffer.length);
		content = new String(buffer);
		int packetPort = packet.getPort();
		
		//check if its destination 
		if (packetPort == CONSTANTS.CONTROLLERPORT || packetPort >= CONSTANTS.STARTROUTER && packetPort < CONSTANTS.STARTENDUSER) {
			if (content.equals(CONSTANTS.HELLO)) {
				terminal.println("The Controller sent hello");
			} 
			
			else {
				sendMessage(message, Integer.parseInt(Controller.next(port, destination)));
			}
			
			//else forward the message along 
		} else if (packetPort >= CONSTANTS.STARTENDUSER) {
			if (packetPort == CONSTANTS.STARTENDUSER)
				destination = CONSTANTS.STARTENDUSER + 1;
			else 
				destination = CONSTANTS.STARTENDUSER;

			message = content;
			sendMessage(CONSTANTS.NEXT + destination, CONSTANTS.CONTROLLERPORT);
		}
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

	private synchronized void sayHello() {
			sendMessage(CONSTANTS.HELLO, CONSTANTS.CONTROLLERPORT);
	}

	public synchronized void run() {
		sayHello();

		try { this.wait(); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}


}