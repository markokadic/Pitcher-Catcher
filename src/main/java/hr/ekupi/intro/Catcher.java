package hr.ekupi.intro;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Date;

public class Catcher extends Thread {
	
	private static DatagramSocket socket = null; 

	public Catcher(String address, String portNum) throws IOException {
		socket = new DatagramSocket(Integer.parseInt(portNum), InetAddress.getByName(address));
	}
	
	@Override
	public void run()  {
		// otvori port i primaj 
		while(true) {
			try {
				byte[] buf = new byte[3000];
				DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
				socket.receive(receivedPacket);   	
				
				ByteBuffer packetData = ByteBuffer.wrap(buf);
				
				int pckgNum = packetData.getInt();
				
				ByteBuffer response = ByteBuffer.allocate(receivedPacket.getLength()); // natrag šaljem paket iste veličine
				response.putInt(pckgNum);
				
				Integer length = packetData.getInt(); //timestamp od pitchera
				byte[] ts1 = new byte[length];
				packetData.get(ts1, 0, length);	
				
				response.putInt(length);
				response.put(ts1);
				
				Timestamp ts2 = new Timestamp(new Date().getTime()); //novi timestamp za statistiku
				byte[] timestamp = ts2.toString().getBytes();
				response.putInt(timestamp.length);
				response.put(timestamp);
				
				buf = response.array();
				
				InetAddress address = receivedPacket.getAddress(); //šaljem paket natrag
				int port = receivedPacket.getPort();
				DatagramPacket responsePacket = new DatagramPacket(buf, buf.length, address, port);
				socket.send(responsePacket);
			
			} catch (IOException e) {
				
			}
		}
		
	}
	
}
