/* End nodes send messages to each other, where messages travel through different switches 
 * 
 * End node sends payload to a switch, this switch forwards this packet to another switch etc. until eventually
 * the last switch on the path forwards the payload to the target end node (receiver).
 * 
 * Therefore, the end node has 2 functionalities:
 * 
 * 		send payload 		(1st end node sends message)
 * 		receive payload		(target end node receives)
 * 	

 /*
	Applications name data and data names will directly be used in network packet forwarding; 
	Consumer applications request desired data by its name, so communications in NDN are consumer-driven (Wikipedia).
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.Scanner;

public class EndNode extends Node {	

	private String function;
	static Scanner scanner; 

	EndNode (int port) {
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
			byte[] data;
			data = packet.getData();			
			String content;

			switch(data[TYPE_POS]) {

			case END_NODE_SEND_MESSAGE:
				content = getPacketMessage(packet);
				System.out.println("Target end node received message from start end node.\nMessage was: "+content);
				this.notify();
				break;	

			default:
				System.out.println("Unexpected packet: " + packet.toString());
			}

		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
	}

	public synchronized String getPacketMessage(DatagramPacket packet) {
		byte[] data = packet.getData();
		byte[] buffer = new byte[data[LENGTH_POS]];
		System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
		String content= new String(buffer);
		return content;
	}

	/* Function to send a message to another end node. Asks the user which end node to send the
	 * message to as well as the content of the message. Sends the message into the network.
	 */
	public synchronized void sendMessage(SocketAddress dstAddress) {
		System.out.println("Enter message to send: ");
		String messageAsString = scanner.nextLine();

		byte[] buffer = messageAsString.getBytes();
		byte[] data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = END_NODE_SEND_MESSAGE;
		data[LENGTH_POS] = (byte) buffer.length;

		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);

		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		try {
			socket.send(packet);
			System.out.println("Message sent from starting EndNode");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public synchronized void start() throws Exception {
		while(true) {
			if(this.function.equalsIgnoreCase("send")) {
				sendMessage(R1_ADDRESS);
				System.out.println("EndNode waiting for contact");
				this.wait();
			}
			else if(this.function.equalsIgnoreCase("wait")) {
				System.out.println("EndNode waiting for contact");
				this.wait();
				sendMessage(R4_ADDRESS);
			}
		}
	}

	public static void main(String[] args) {
		try {
			EndNode e1 = new EndNode(END_NODE_PORT);		// port of original sender as per diagram
			Scanner scanner = new Scanner(System.in);
			System.out.println("Will this end node send or wait? ");
			String input = scanner.nextLine();
			e1.function=input;
			e1.start();
			scanner.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		//		try {
		//			EndNode endNode4 = new EndNode(E4_PORT);		// port of target end node (receiver) as per diagram
		//			endNode4.start(E4_PORT);
		//		} catch(Exception e) {
		//			e.printStackTrace();
		//		}		
	}
}
