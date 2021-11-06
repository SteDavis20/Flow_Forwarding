/* @author: 	Stephen Davis (code extended from provided code on blackboard by lecturer Stefan Weber) 
 * student id: 	18324401
 */

/* Control the contents of each Switch/Router's forwarding table 
 * 
 * Tell switch where to go if switch has no entry for the received packet in its forwarding table
 * 
 * 
 * 
 * 
 * */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Controller extends Node {

	static final int CONTROLLER_PORT = 50001; // Port of the broker (destination)
	
	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header

	static final byte TYPE_UNKNOWN = 0;

	static final int LENGTH_POS = 1;

	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header
	static final byte ACK_ALLOK = 10; // Indicating that everything is ok

	static final byte TYPE_ACK = 7;   // Indicating an acknowledgement

	static final int PACKETSIZE = 65536;

	Scanner scanner;

	private static HashMap<String, String> flowTables = new HashMap<String, String>();

	Controller (int port) {
		try {
			socket = new DatagramSocket(port);
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
				break;	
				
			default:
				System.out.println("Unexpected packet: " + packet.toString());
			}

		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
	}

	public static void sendHello(SocketAddress srcAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_HELLO;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(srcAddress);
		try {
			socket.send(packet);
			System.out.println("Hello sent from Controller");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/* Ask switch what features they want to receive */
	public static void sendFeaturesRequest(SocketAddress srcAddress) {
		byte[] data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = OFPT_FEATURES_REQUEST;
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(srcAddress);
		try {
			socket.send(packet);
			System.out.println("Feature Request sent from Controller");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void start() throws Exception {
		System.out.println("Controller waiting for contact");
		this.wait();
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
			System.out.println("ACK sent from Controller");
			return content;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
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


