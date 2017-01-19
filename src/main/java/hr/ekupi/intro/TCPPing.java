package hr.ekupi.intro;

import java.io.IOException;
import java.util.ArrayList;

public class TCPPing {
		
	static Pitcher pitcher;
	static Catcher catcher;
	
	public static void main(String[] args) {
		
		String input = "";
		ArrayList<String> inputCommand = new ArrayList<String>();
		
		for(String str : args) {
			input += str;
			input += " ";
			inputCommand.add(str);
		}

		try {
			if(input.contains("-p ")) {
				
				String port;
				String size;
				String hostName;
				String speed;
				
				if(input.contains("-port")) {
					port = inputCommand.get(inputCommand.indexOf("-port")+1);
					hostName = inputCommand.get(inputCommand.size()-1);
					if(input.contains("-mps")) {
						speed = inputCommand.get(inputCommand.indexOf("-mps")+1);	
					}
					else {
						speed = "1";
					}
					if(input.contains("-size")) {
						size = inputCommand.get(inputCommand.indexOf("-size")+1);
					}
					else {
						size = "300";
					}
				}
				else {
					System.out.println(help());
					return;
				}
				new Pitcher(speed, port, size, hostName);
				
			}
			else if(input.contains("-c ")) {
				String port;
				String address;
				
				if(input.contains("-bind")) {
					if(input.contains("-port")) {
						port = inputCommand.get(inputCommand.indexOf("-port")+1);
						address = inputCommand.get(inputCommand.indexOf("-bind")+1);
	
					}
					else {
						System.out.println(help());
						return;
					}
				}
				else {
					System.out.println("help()");
					return;
				}
				try {
					new Catcher(address, port).start();
					
				} catch (IOException e) {
					System.out.println(help());
					e.printStackTrace();
					return;
				}
			}
			else if(input.contains("help")) {
				System.out.println(help());
				return;
			}
		} catch (Exception e) {
			System.out.println(help());
			System.out.println(e.getStackTrace());
		}
	}
	
	private static String help() {
		return "Please follow the command syntax";
	}
}
