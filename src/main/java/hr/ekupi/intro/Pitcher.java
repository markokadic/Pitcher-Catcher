package hr.ekupi.intro;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public class Pitcher {
		
	private int speed;
	private int port;
	private int size;
	private String hostName;
	private int counter;
	private Timer packetTimer;
	private Timer writeTimer;
	private Thread recieve;
	private static DatagramSocket clientSocket;
	private static ConcurrentHashMap<Integer, Long> ABTimes = new ConcurrentHashMap<Integer, Long>();
	private static ConcurrentHashMap<Integer, Long> BATimes = new ConcurrentHashMap<Integer, Long>();
	private static ConcurrentHashMap<Integer, Long> ABTimesAVG = new ConcurrentHashMap<Integer, Long>();
	private static ConcurrentHashMap<Integer, Long> BATimesAVG = new ConcurrentHashMap<Integer, Long>();
	
	public Pitcher(String speed, String port, String size, String hostName) {
		this.speed = Integer.parseInt(speed);
		this.port = Integer.parseInt(port);
		this.size = Integer.parseInt(size);
		this.hostName = hostName;
		this.counter = 1;
		try {
			clientSocket = new DatagramSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		recieve = new Thread(new PitcherRecieve());
		recieve.start();
		packetTimer = new Timer();
		writeTimer = new Timer();

		
		packetTimer.schedule(new PitcherSend(), 0, 1000/Integer.parseInt(speed));
		writeTimer.schedule(new PitcherWrite(), 1000, 1000);
	}
	
	class PitcherSend extends TimerTask {
		@Override
		public void run() {
			try {
				ByteBuffer packetData = ByteBuffer.allocate(size);
				
				packetData.putInt(counter);
				Timestamp stamp =  new Timestamp(new Date().getTime());
				byte[] timestamp = stamp.toString().getBytes();
				packetData.putInt(timestamp.length);
				packetData.put(timestamp);
				
				byte[] buf = packetData.array();
				
				DatagramPacket p  = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostName), port);
				clientSocket.send(p);
				
			} catch (IOException e) {
					
			}
			
			counter++;
		}
	}
	
	class PitcherRecieve extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					byte[] buf = new byte[3000];
					
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					
					clientSocket.receive(packet);
					
					ByteBuffer packetData = ByteBuffer.wrap(buf);
					
					Integer pckgNum = packetData.getInt();
					
					Integer length1 = packetData.getInt();		//vrijeme slanja pitchera
					byte[] ts1 = new byte[length1];
					packetData.get(ts1, 0, length1);	
					
					Integer length2 = packetData.getInt();		//vrijeme slanja catchera
					byte[] ts2 = new byte[length2];
					packetData.get(ts2, 0, length2);
					
					SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");		///dio za statistiku
					try {
						long timestamp1 = s.parse(new String(ts2)).getTime() -
																	s.parse(new String(ts1)).getTime();
						long timestamp2 = new Date().getTime() - s.parse(new String(ts2)).getTime();
						ABTimes.put(pckgNum, Math.abs(timestamp1));
						BATimes.put(pckgNum, Math.abs(timestamp2));
						ABTimesAVG.put(pckgNum, Math.abs(timestamp1));
						BATimesAVG.put(pckgNum, Math.abs(timestamp2));
					} catch (ParseException e) {
						
					}		
				} catch(IOException e) {
						
				}
			}
		}
	}
		
		
	class PitcherWrite extends TimerTask {
			
		@Override
		public void run() {
			LocalDateTime now = LocalDateTime.now();
			System.out.println(now.getHour()+":"+now.getMinute()+":"+now.getSecond());
			System.out.println(speed);
			System.out.println(counter);
			System.out.println("avg AB: "+avgAB());
			System.out.println("avgBA: "+avgBA());
			System.out.println("avgABA: "+avgABA());
			System.out.println("maxAB: "+maxAB());
			System.out.println("maxBA: "+maxBA());
			System.out.println("maxABA: "+maxABA());
				
			ABTimesAVG.clear(); //GLEDAMO SAMO POSLJEDNJU SEKUNDU
			BATimesAVG.clear(); 
		}
			
			private String maxAB() {
				long max  = 0;
				for(long s2 : ABTimes.values()) {
					if(s2 > max) {
						max = s2;
					}
				}
				return Long.toString(max);
			}
			
			private String maxBA() {
				long max  = 0;
				for(long s2 : BATimes.values()) {
					if(s2 > max) {
						max = s2;
					}
				}
				return Long.toString(max);
			}
			
			private String avgAB() {
				if(ABTimesAVG.size() == 0) {
					return "0";
				}
				long sum  = 0;
				for(long s2 : ABTimesAVG.values()) {
					sum += s2; 
				}
				return Long.toString(sum/ABTimesAVG.size());
			}
			
			private String avgBA() {
				if(BATimesAVG.size() == 0) {
					return "0";
				}
				long sum  = 0;
				for(long s2 : BATimesAVG.values()) {
					sum += s2; 
				}
				return Long.toString(sum/BATimesAVG.size());
			}
			
			private String maxABA() {
				long max1  = 0;
				for(long s2 : ABTimes.values()) {
					if(s2 > max1) {
						max1 = s2;
					}
				}
				long max2  = 0;
				for(long s2 : BATimes.values()) {
					if(s2 > max2) {
						max2 = s2;
					}
				}
				return Long.toString(max1+max2);
			}
			
			private String avgABA() {
				if(ABTimesAVG.size() == 0 && BATimesAVG.size() == 0 ) {
					return "0";
				}
				long sum  = 0;
				for(long s2 : ABTimesAVG.values()) {
					sum += s2; 
				}
				for(long s2 : BATimesAVG.values()) {
					sum += s2; 
				}
				return Long.toString(sum/(BATimesAVG.size()+ABTimesAVG.size())*2);
			}
			
	}
		
	
}
