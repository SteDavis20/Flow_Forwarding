/* @author: 	Stefan Weber (code provided on blackboard)
 * student id: 	18324401
 */

/* An application that intends to receive traffic will send a Datagram to
 * the forwarding service on its local host, indicating that it intends to
 * receive traffic for a given string. 
 */



import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public abstract class Node {

	/* Below values are provided in assignment2 powerpoint */
	
	/* OFP = Open Flow Packet */

	/* Immutable messages. */
	static final byte OFPT_HELLO = 0; 
	static final byte OFPT_ERROR = 1; 
	static final byte OFPT_ECHO_REQUEST = 2; 
	static final byte OFPT_ECHO_REPLY = 3;
	static final byte OFPT_EXPERIMENTER = 4; 

	/* Switch configuration messages. */
	static final byte OFPT_FEATURES_REQUEST = 5; 
	static final byte OFPT_FEATURES_REPLY = 6; 
	static final byte OFPT_GET_CONFIG_REQUEST = 7; 
	static final byte OFPT_GET_CONFIG_REPLY = 8; 
	static final byte OFPT_SET_CONFIG = 9; 

	/* Asynchronous (one-way) messages. */
	static final byte OFPT_PACKET_IN = 10; 
	static final byte OFPT_FLOW_REMOVED = 11; 
	static final byte OFPT_PORT_STATUS = 12; 
	static final byte OFPT_PACKET_OUT = 13; 
	static final byte OFPT_FLOW_MOD = 14; 
	static final byte OFPT_GROUP_MOD = 15; 
	static final byte OFPT_PORT_MOD = 16; 
	static final byte OFPT_TABLE_MOD = 17; 

	/* Multipart messages. */
	static final byte OFPT_MULTIPART_REQUEST = 18; 
	static final byte OFPT_MULTIPART_REPLY = 19; 

	/* Barrier messages. */
	static final byte OFPT_BARRIER_REQUEST = 20; 
	static final byte OFPT_BARRIER_REPLY = 21; 

	/* Controller role change request messages. */
	static final byte OFPT_ROLE_REQUEST = 24; 
	static final byte OFPT_ROLE_REPLY = 25; 

	/* Asynchronous message configuration. */
	static final byte OFPT_GET_ASYNC_REQUEST = 26; 
	static final byte OFPT_GET_ASYNC_REPLY = 27; 
	static final byte OFPT_SET_ASYNC = 28; 

	/* Meters and rate limiters configuration messages. */
	static final byte OFPT_METER_MOD = 29; 

	/* Controller role change event messages. */
	static final byte OFPT_ROLE_STATUS = 30; 

	/* Asynchronous messages. */
	static final byte OFPT_TABLE_STATUS = 31; 

	/* Request forwarding by the switch. */
	static final byte OFPT_REQUESTFORWARD = 32; 

	/* Bundle operations. */
	static final byte OFPT_BUNDLE_CONTROL = 33;
	static final byte OFPT_BUNDLE_ADD_MESSAGE = 34; 

	/* Controller Status async message. */
	static final byte OFPT_CONTROLLER_STATUS = 35;
	
	static final byte BASIC_FEATURES = 36;

	static final byte END_NODE_SEND_MESSAGE = 37;

	static final byte FEATURES_POS = 1;
	
	static final int PACKETSIZE = 65536;

	static final int CONTROLLER_PORT = 50001;
	static final int SWITCH_PORT = 50002;
	static final int END_NODE_PORT = 50005;
	
	DatagramSocket socket;				
	
	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header

	static final byte TYPE_UNKNOWN = 0;

	static final int LENGTH_POS = 1;
	
	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header
	static final byte ACK_ALLOK = 10; // Indicating that everything is ok

	static final byte TYPE_ACK = 7;   // Indicating an acknowledgement
	
	static final byte E1 = 1;
	static final byte R1 = 2;		// switch 1
	static final byte R2 = 3;		// switch 2
	static final byte R4 = 4;		// switch 3
	static final byte E4 = 5;
	
	static final String E1_HOST_NAME = "E1.cs2031";
	static final String R1_HOST_NAME = "R1.cs2031";		// switch 1
	static final String R2_HOST_NAME = "R2.cs2031";		// switch 2
	static final String R4_HOST_NAME = "R4.cs2031";		// switch 3
	static final String E4_HOST_NAME = "E4.cs2031";
	static final String TRINITY = "trinity";
	
//	static final int NUMBER_OF_SWITCHES = 3;
		
	InetSocketAddress dstAddress;
	
	Listener listener;
	CountDownLatch latch;

	Node() {
		latch= new CountDownLatch(1);
		listener= new Listener();
		listener.setDaemon(true);
		listener.start();
	}

	public abstract void onReceipt(DatagramPacket packet);

	/**
	 *
	 * Listener thread
	 * 
	 * Listens for incoming packets on a datagram socket and informs registered receivers about incoming packets.
	 */
	class Listener extends Thread {

		/*
		 *  Telling the listener that the socket has been initialized 
		 */
		public void go() {
			latch.countDown();
		}

		/*
		 * Listen for incoming packets and inform receivers
		 */
		public void run() {
			try {
				latch.await();
				// Endless loop: attempt to receive packet, notify receivers, etc
				while(true) {
					DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
					socket.receive(packet);

					onReceipt(packet);
				}
			} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
		}
	}
}
