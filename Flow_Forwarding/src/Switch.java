/* A Switch is the same thing as a Router */

/* 
 * Switch's functionality:
 *  Send hello message to controller
 *  Receive hello from controller
 *  Lookup forwarding table and send packet along the chain
 *  Change it's forwarding table by directions from controller
 */

/* 1st Submission:
	Routers' forwarding table are hard coded and know where to forward the packet.
 */

/* 2nd Submission:
	Routers' forwarding tables are blank and the controllers forwarding table is hard coded. 
	The routers then contact the controller for the next hop.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Switch extends Node {

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header

	static final byte TYPE_UNKNOWN = 0;

	static final int LENGTH_POS = 1;

	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header
	static final byte ACK_ALLOK = 10; // Indicating that everything is ok

	static final int PACKETSIZE = 65536;

	Scanner scanner;
	InetSocketAddress dstAddress;

	private static String[][] flowTable;
	private static String[][] preconfiguredInformation = 
			/* 	Dest, 		Src, 	Router, 	In, 	Out	*/
		{ 
				{TRINITY, 	E1_HOST_NAME, 	R1_HOST_NAME, 		E1_HOST_NAME, 	R2_HOST_NAME},
				{TRINITY, 	E1_HOST_NAME, 	R2_HOST_NAME, 		R1_HOST_NAME, 	R4_HOST_NAME},
				{TRINITY, 	E1_HOST_NAME, 	R4_HOST_NAME, 		R2_HOST_NAME, 	E4_HOST_NAME},
		};

	Switch (int port) {
		try {
			socket = new DatagramSocket(port);
			listener.go();
			scanner = new Scanner(System.in);
			dstAddress = new InetSocketAddress("Controller", CONTROLLER_PORT);
			flowTable = preconfiguredInformation;				// 1 row, 3 columns
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			String content;
			byte[] data;
			data = packet.getData();			

			SocketAddress srcAddress = packet.getSocketAddress();

			switch(data[TYPE_POS]) {

			case OFPT_HELLO:
				System.out.println("Switch received hello from controller");
				break;

			case OFPT_FEATURES_REQUEST:
				System.out.println("Switch received Feature Request from controller");
				replyToFeaturesRequest(srcAddress);
				break;	

			case OFPT_FLOW_MOD:
				System.out.println("Switch received flow mod from controller");
				content = sendACK(packet, data);
				break;	

			case END_NODE_SEND_MESSAGE:
				forwardPacket(packet);
				break;

			default:
				System.out.println("Unexpected packet: " + packet.toString());
			}

		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
	}

	public synchronized void sendHello(SocketAddress dstAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_HELLO;
		data[LENGTH_POS] = 0;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		try {
			socket.send(packet);
			System.out.println("Hello sent from Switch");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized InetSocketAddress lookUpTable(DatagramPacket packet) {
		String ipAddress = packet.getAddress().getHostName();
		System.out.println("Container src name: "+ipAddress);
		String src_host_name="";
		if(ipAddress.equalsIgnoreCase(E1_HOST_NAME)) {
			src_host_name=E1_HOST_NAME;
		}
		else if(ipAddress.equalsIgnoreCase(R1_HOST_NAME)) {
			src_host_name=R1_HOST_NAME;
		}
		else if(ipAddress.equalsIgnoreCase(R2_HOST_NAME)) {
			src_host_name=R2_HOST_NAME;
		}
		else if(ipAddress.equalsIgnoreCase(R4_HOST_NAME)) {
			src_host_name=R4_HOST_NAME;
		}
		else if(ipAddress.equalsIgnoreCase(E4_HOST_NAME)) {
			src_host_name=E4_HOST_NAME;
		}
		String destNode = "";
		for(int i=0; i<preconfiguredInformation.length; i++) {
			if(preconfiguredInformation[i][3].equalsIgnoreCase(src_host_name)) {
				destNode=preconfiguredInformation[i][4];
				break;
			}
		}
		int port = SWITCH_PORT;
		if(destNode.equalsIgnoreCase(E4_HOST_NAME)) {
			port = END_NODE_PORT;
		}
		System.out.println("Container destination name: "+destNode);
		InetSocketAddress destAddress = new InetSocketAddress(destNode, port);
		return destAddress;
	}

	public synchronized void forwardPacket(DatagramPacket packet) {
		// check forwardingTable for destination address etc.	
		InetSocketAddress destAddress = lookUpTable(packet);
		packet.setSocketAddress(destAddress);
		try {
			socket.send(packet);
			System.out.println("Packet forwarded from Switch");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	/* switch eplies with a features reply message that specifies the features and capabilities that are supported by the switch */
	public synchronized void replyToFeaturesRequest(SocketAddress srcAddress) {			
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_FEATURES_REPLY;
		data[FEATURES_POS] = BASIC_FEATURES;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(srcAddress);
		try {
			socket.send(packet);
			System.out.println("Switch sent a features reply to controller.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * ACK Sender Method
	 *
	 */
	public synchronized String sendACK(DatagramPacket packet, byte[] data) throws Exception {
		try {
			String content;

			byte[] buffer = new byte[data[LENGTH_POS]];
			buffer= new byte[data[LENGTH_POS]];
			System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);

			content= new String(buffer);

			data = new byte[HEADER_LENGTH];
			data[TYPE_POS] = TYPE_ACK;
			data[ACKCODE_POS] = ACK_ALLOK;

			DatagramPacket response;
			response = new DatagramPacket(data, data.length);
			response.setSocketAddress(packet.getSocketAddress());
			socket.send(response);
			System.out.println("ACK sent from Switch");
			return content;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void requestNextHop() {


	}

	public synchronized void start() throws Exception {
		System.out.println("Switch waiting for contact");
		sendHello(dstAddress);
		this.wait();
	}

	/* "switch" is a keyword in java so cannot use this name as Object's name! */
	public static void main(String[] args) {
		try {
			Switch s = new Switch(SWITCH_PORT);
			s.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
