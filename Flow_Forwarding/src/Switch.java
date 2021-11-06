/* A Switch is the same thing as a Router */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Switch extends Node {

	private static final int NUMBER_OF_SENSORS = 3;

	static final int CONTROLLER_PORT = 50001; 
	static final int SWITCH_PORT = 50002;
	
	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header

	static final byte TYPE_UNKNOWN = 0;

	static final int LENGTH_POS = 1;

	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header
	static final byte ACK_ALLOK = 10; // Indicating that everything is ok

	static final int PACKETSIZE = 65536;

	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the ACTUATOR

	Scanner scanner;
	InetSocketAddress dstAddress;

	private static HashMap<String, String> flowTable = new HashMap<String, String>();

	Switch (int port) {
		try {
			socket = new DatagramSocket(port);
			listener.go();
			scanner = new Scanner(System.in);
			dstAddress = new InetSocketAddress(DEFAULT_DST_NODE, CONTROLLER_PORT);
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
				content = sendACK(packet, data);
				break;

			case OFPT_FEATURES_REQUEST:
				System.out.println("Switch received Feature Request from controller");
				replyToFeaturesRequest(srcAddress);
				break;	

			case OFPT_FLOW_MOD:
				System.out.println("Switch received flow mod from controller");
				content = sendACK(packet, data);
				break;	

			default:
				System.out.println("Unexpected packet: " + packet.toString());
			}

		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
	}

	public static void sendHello(SocketAddress dstAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_HELLO;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		try {
			socket.send(packet);
			System.out.println("Hello sent from Controller");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	/* switch eplies with a features reply message that specifies the features and capabilities that are supported by the switch */
	public static void replyToFeaturesRequest(SocketAddress srcAddress) {			
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
		break;
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

	public static void forwardPacket() {
		// check forwardingTable for destination address etc.	


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
			Switch switch_ = new Switch(SWITCH_PORT);
			switch_.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
