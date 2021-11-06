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
 * */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public class EndNode extends Node {	
	
	
	public synchronized void onReceipt(DatagramPacket packet) {
			
//		System.out.println("End Node received the following packet: ");
		
	}
	
	public static void sendMessage() {
			
		
	}
	
}
