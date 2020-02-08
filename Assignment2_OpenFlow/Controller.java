import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.*;
import java.lang.System;

public class Controller extends Node implements Runnable {

    private Terminal terminal;
    private ArrayList<Integer> routers = new ArrayList<>();		//ROUTER INFO 
    private static HashMap<Integer, int[]> flowTable = new HashMap<>();		//HASHTABLE TO STORE ROUTERS

    Controller(Terminal terminal, int port) {
        try {
            this.terminal = terminal;
            socket = new DatagramSocket(port);
            listener.go();
        } catch (java.lang.Exception e) { e.printStackTrace(); }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        byte[] data;
        String content;
        byte[] buffer;

        data = packet.getData();

        buffer = new byte[data[CONSTANTS.LENGTH_POS]];
        System.arraycopy(data, CONSTANTS.HEADER_LENGTH, buffer, 0, buffer.length);
        content = new String(buffer);
        
        int packetPort = packet.getPort();
        if (packetPort >= CONSTANTS.STARTROUTER) {
        	
            if (content.equals(CONSTANTS.HELLO)) {
                routers.add(packetPort);
                sendMessage(CONSTANTS.HELLO, packetPort);
            } 
            
            else if (content.contains(CONSTANTS.NEXT)) {
                sendMessage(next(packetPort, Integer.parseInt(content.replaceAll("[^0-9]", ""))), packetPort);
            }
        }
    }

    static String next(int source, int destination) {
        int[] route = flowTable.get(destination);

        for (int i = 0; i < route.length; i++) {
        	
            if (i + 1 == route.length)
                return Integer.toString(destination);
            
            if (source == route[i])
                return Integer.toString(route[i+1]);
        }
        return "";
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

    private synchronized void generateFlowTable() {
        for (int i = 0; i < CONSTANTS.ENDUSERS; i++) {
        	
            int[] routerPorts = new int[routers.size()];
            
            for (int j = 0; j < routers.size(); j++) {
                routerPorts[j] = routers.get(j);
            }
            flowTable.put(CONSTANTS.STARTENDUSER + i, routerPorts);
        }
    }

    public synchronized void run() {
        try { 
        	this.wait(2500);
        }
        
        catch (InterruptedException e) { 
        	e.printStackTrace();
        }
        
        generateFlowTable();

        try { 
        	this.wait();
        }
        catch (InterruptedException e) { 
        	e.printStackTrace(); 
        }
    }
}