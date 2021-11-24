/* @author: 	Stephen Davis (code extended from provided code on blackboard by lecturer Stefan Weber) 
 * student id: 	18324401
 */

/* Sends hello to switches
 * receives hello from switches
 * Look up directions in hard-coded routing table (based on topology)
 * Tell switches to change their forwarding table  
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Controller extends Node {

	Scanner scanner;

//	private static byte[][] flowTables;
	private static String[][] preconfiguredInformation = 
			/* 	Dest, 		Src, 			Router, 			In, 			Out	*/
		{ 
				{E4_HOST_NAME, 	E1_HOST_NAME, 	R1_HOST_NAME, 		E1_HOST_NAME, 	R2_HOST_NAME},
				{E4_HOST_NAME, 	E1_HOST_NAME, 	R2_HOST_NAME, 		R1_HOST_NAME, 	R4_HOST_NAME},
				{E4_HOST_NAME, 	E1_HOST_NAME, 	R4_HOST_NAME, 		R2_HOST_NAME, 	E4_HOST_NAME},
				{E1_HOST_NAME, 	E4_HOST_NAME, 	R4_HOST_NAME, 		E4_HOST_NAME, 	R2_HOST_NAME},
				{E1_HOST_NAME, 	E4_HOST_NAME, 	R2_HOST_NAME, 		R4_HOST_NAME, 	R1_HOST_NAME},
				{E1_HOST_NAME, 	E4_HOST_NAME, 	R1_HOST_NAME, 		R2_HOST_NAME, 	E1_HOST_NAME},
		};
	
	Controller (int port) {
		try {
			this.socket = new DatagramSocket(port);
			listener.go();
			scanner = new Scanner(System.in);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void onReceipt(DatagramPacket packet) {
		try {
//			String content;
			byte[] data;
			data = packet.getData();			

			SocketAddress srcAddress = packet.getSocketAddress();
			
			switch(data[TYPE_POS]) {

			case OFPT_HELLO:
				System.out.println("Controller received hello from switch");
				sendHello(srcAddress);
				sendFeaturesRequest(srcAddress);
				break;

			case OFPT_FEATURES_REPLY:
				System.out.println("Controller received Features Reply from switch");
				if(data[FEATURES_POS]==BASIC_FEATURES) {
					System.out.println("Features are: Basic Features");
				}
				else {
					System.out.println("Features are: Not basic Features");	
				}
				sendFlowMod(packet);
				break;	
				
			case TYPE_ACK:
				System.out.println("Controller received ACK from Switch for flow mod message");
				break;
				
			case OFPT_PACKET_IN:
				System.out.println("Controller was contacted by Switch for unrecognised packet");
				System.out.println("Controller tells switch to drop packet, there is no path");
				break;	
				
			default:
				System.out.println("Unexpected packet: " + packet.toString());
			}

		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
	}

	public synchronized void sendHello(SocketAddress srcAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_HELLO;
		data[LENGTH_POS] = 0;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(srcAddress);
		try {
			socket.send(packet);
			System.out.println("Hello sent from Controller");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/* Ask switch what features they want to receive */
	public synchronized void sendFeaturesRequest(SocketAddress srcAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_FEATURES_REQUEST;
		data[LENGTH_POS] = 0;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(srcAddress);
		try {
			socket.send(packet);
			System.out.println("Feature Request sent from Controller");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void sendFlowMod(DatagramPacket packet) {
		String flowTableModificationMessage = getFlowTableModificationMessage(packet);
		byte[] buffer = flowTableModificationMessage.getBytes();
		byte[] data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = OFPT_FLOW_MOD;
		data[LENGTH_POS] = (byte) buffer.length;
		/* copy buffer content into data content */
		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);
		DatagramPacket flowModPacket = new DatagramPacket(data, data.length);
		SocketAddress srcAddress = packet.getSocketAddress();
		flowModPacket.setSocketAddress(srcAddress);
		try {
			socket.send(flowModPacket);
			System.out.println("Flow mod sent from Controller");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}	
	
	public synchronized String getFlowTableModificationMessage(DatagramPacket packet) {
		StringBuilder builder = new StringBuilder();
		String entry = "";
		int columnIndex = 2;
		String ipAddress = packet.getAddress().getHostName();
		for(int i=0; i<preconfiguredInformation.length; i++) {
			entry = preconfiguredInformation[i][columnIndex];
			if(ipAddress.equalsIgnoreCase(entry)) {
				if(builder.length()!=0) {
					builder.append(",");
				}
				for(int j=0; j<preconfiguredInformation[i].length-1; j++) {
					String flowTableElement = preconfiguredInformation[i][j];
					builder.append(flowTableElement);
					builder.append(",");
				}
				builder.append(preconfiguredInformation[i][preconfiguredInformation[i].length-1]);
			}
		}
		String modificationMessage = builder.toString();
		return modificationMessage;
	}
	
	public synchronized void start() throws Exception {
		System.out.println("Controller waiting for contact");
		this.wait();
	}

	public static void main(String[] args) {
		try {
			Controller controller = new Controller(CONTROLLER_PORT);
			controller.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}


