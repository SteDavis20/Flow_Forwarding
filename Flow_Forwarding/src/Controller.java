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

	private static byte[][] flowTables;

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

	public synchronized void start() throws Exception {
		System.out.println("Controller waiting for contact");
		this.wait();
	}

	/**
	 * ACK Sender Method
	 *
	 */
//	public synchronized String sendACK(DatagramPacket packet, byte[] data) throws Exception {
//		try {
//			String content;
//
//			byte[] buffer = new byte[data[LENGTH_POS]];
//			buffer= new byte[data[LENGTH_POS]];
//			System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
//
//			content= new String(buffer);
//
//			data = new byte[HEADER_LENGTH];
//			data[TYPE_POS] = TYPE_ACK;
//			data[ACKCODE_POS] = ACK_ALLOK;
//
//			DatagramPacket response;
//			response = new DatagramPacket(data, data.length);
//			response.setSocketAddress(packet.getSocketAddress());
//			socket.send(response);
//			System.out.println("ACK sent from Controller");
//			return content;
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return "";
//	}

	public static void main(String[] args) {
		try {
			Controller controller = new Controller(CONTROLLER_PORT);
			controller.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}


