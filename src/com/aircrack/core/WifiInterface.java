package com.aircrack.core;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.*;
import java.text.Format;
import java.util.ArrayList;
import java.util.Enumeration;


public class WifiInterface {
	private String Driver;
	private String HWaddr;
	private String Chipset;
	private String Name;
	private String MonitorName;
	private String State;
	private ArrayList<WifiAccessPoint> fAccessPoints;

	private SysCommandExecutor exec;

	public WifiInterface(String sequence) throws Exception {
		exec = new SysCommandExecutor();
		parseifconfig(sequence);
	}

	private void parseifconfig(String sequence) throws Exception {
		String[] tokens = sequence.split("\t");
		String name = tokens[0];
		HWaddr=getMAC(name);
		Name = name;
		/*
		
		try {
			String cmd = "ifconfig " + name;

			exec.runSyncCommand(cmd,5);
			String output = exec.getCommandOutput();
			if (!exec.getCommandError().isEmpty()) {
				System.err.println(exec.getCommandError());
			}
			if (output.contains("Device not found")) {
				throw new Exception("Unable to create Object", new Throwable(
						"Interface '" + name + "' doesn't exist"));
			}
			Name = name;
			Chipset = tokens[2];
			Driver = tokens[4];
			int start = output.indexOf("HWaddr") + 7;
			int end = output.indexOf("\n", start);
			HWaddr = output.substring(start, end).trim();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
	}
	
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
	
	private String getMAC(String interfaceName){
		StringBuffer res=new StringBuffer();
		try {
			NetworkInterface wlan=NetworkInterface.getByName(interfaceName);
			if (wlan!=null){
				byte[] b=wlan.getHardwareAddress();
				String r=toHex(b);
				
				for (int i = 0; i < b.length; i++) {
					String b2=String.format("%02X", b[i]);
					//String b2=Integer.toHexString(0xFF & b[i]);
					res.append(b2).append(":");				
		        }
				res.deleteCharAt(res.length()-1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		/*
		Enumeration<NetworkInterface> nets=null;
		
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			//NetworkInterface wlan=NetworkInterface.getByName("wlan0");
			//String mac=new String(wlan.getHardwareAddress());
			//System.out.println(mac);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (nets.hasMoreElements()){
			
			NetworkInterface net=nets.nextElement();
				System.out.println(net.getName());
			if (net.getName().equals(interfaceName)){
				try {
					res=new String(net.getHardwareAddress());
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		*/
		return res.toString();
	}

	public String getDriver() {
		return Driver;
	}

	public String getHWaddr() {
		return HWaddr;
	}

	public String getChipset() {
		return Chipset;
	}

	public String getName() {
		return Name;
	}

	public String getState() {
		State = "Unknown";

		try {
			SysCommandExecutor run = new SysCommandExecutor();
			String cmd = "iwconfig " + Name;
			run.runSyncCommand(cmd, 5);
			String output = run.getCommandOutput();

			if (!run.getCommandError().isEmpty()) {
				System.err.println("error:\n" + run.getCommandError());
			}

			int start = output.indexOf("Mode:") + 5;
			int end = output.indexOf(" ", start);
			State = output.substring(start, end).trim();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return State;
	}

	public boolean StartMonitoring() {
		boolean res = false;
		try {
			SysCommandExecutor run = new SysCommandExecutor();
			String cmd = String.format("ifconfig %s up", Name);
			run.runSyncCommand(cmd, 20);
			cmd = "airmon-ng start " + Name;
			run.runSyncCommand(cmd, 20);
			String output = run.getCommandOutput();

			if (!run.getCommandError().isEmpty()) {
				System.err.println("error:\n" + run.getCommandError());
			}

			int start = output.indexOf(Driver) + Driver.length();
			if (start > -1) {
				String result = output.substring(start, output.length()).trim();
				if (result.contains("enabled")) {
					res = true;
					start = result.indexOf("enabled on ") + 11;
					if (start > 10) {
						int end = result.indexOf(")", start);
						MonitorName = result.substring(start, end);
					} else {
						MonitorName = Name;
					}
					if (!getState().equalsIgnoreCase("Monitor")) {
						run.runSyncCommand("iwconfig " + MonitorName
								+ " mode Monitor");
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return res;
	}

	public boolean StopMonitoring() {
		boolean res = false;
		SysCommandExecutor run = new SysCommandExecutor();
		try {

			String cmd = "airmon-ng stop " + MonitorName;
			run.runSyncCommand(cmd, 5);
			String output = run.getCommandOutput();

			if (!run.getCommandError().isEmpty()) {
				System.err.println("error:\n" + run.getCommandError());
			}

			int start = output.indexOf(MonitorName) + MonitorName.length();
			if (start > -1) {
				String result = output.substring(start).trim();
				if (MonitorName == Name) {
					if (result.contains("disabled")) {
						res = true;
					}
				} else {
					if (result.contains("removed")) {
						res = true;
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (res) {
				try {
					run.runSyncCommand("ifconfig " + Name + " up", 5);
					run.runSyncCommand("iwconfig " + Name + " mode Managed", 5);
					run.runSyncCommand("stop network-manager", 5);
					run.runSyncCommand("start network-manager", 5);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return res;
	}

	public ArrayList<WifiAccessPoint> GetAccessPoints() throws Exception {
		String cmd = String.format("iwlist %s scan", Name);
		try {
			exec.runSyncCommand(cmd, 20);
			String output = exec.getCommandOutput();
			System.out.println(output);
			if (!exec.getCommandError().isEmpty()) {
				System.err.println("error:" + exec.getCommandError());
				throw new Exception("General Failure", new Throwable(exec.getCommandError()));
			}

			if (output.split("\n")[0].contains("completed")) {
				output=output.substring(output.indexOf("\n")+1).trim();
				if (fAccessPoints==null){
					fAccessPoints = new ArrayList<WifiAccessPoint>();
				}else {
					fAccessPoints.clear();
				}
				int start = -1;
				int end = 0;
				start = output.indexOf("Cell");
				String tmp;
				while (start > -1) {
					end = output.indexOf("Cell", start + 1);
					if (end > -1) {
						tmp = output.substring(start, end);
						output = output.substring(end);
					} else {
						tmp = output;
						output = "";
					}
					fAccessPoints.add(new WifiAccessPoint(tmp));
					start = output.indexOf("Cell");

				}
			}

		} catch (Exception e) {
			if (fAccessPoints != null) {
				fAccessPoints.clear();
				System.err.println(exec.getCommandOutput());
			}
			throw new Exception("GetAccessPoints Failure", new Throwable(e.getMessage()));
		}

		return fAccessPoints;
	}

	public WifiAccessPoint GetAccessPoint(String SSIDType, String SSID) {
		WifiAccessPoint res = null;
		if (fAccessPoints != null) {
			for (int n = 0; n < fAccessPoints.size(); n++) {
				if (SSIDType.equalsIgnoreCase("ESSID")) {
					if (fAccessPoints.get(n).getESSID().equals(SSID)) {
						res = fAccessPoints.get(n);
						break;
					}
				} else {
					if (fAccessPoints.get(n).getHWAddr().equals(SSID)) {
						res = fAccessPoints.get(n);
						break;
					}
				}
			}
		}

		return res;
	}

	public void refreshAccessPoints() {
		String cmd = String.format("iwlist %s ap", Name);
		try {
			exec.runSyncCommand(cmd,5);
			String[] output = exec.getCommandOutput().split("\n");
			if (!exec.getCommandError().isEmpty()) {
				System.err.println("error:" + exec.getCommandError());
			}

			if (output.length > 1) {
				int start = -1;
				int end = 0;
				for (int n = 1; n < output.length; n++) {
					output[n] = output[n].trim();
					start = 0;
					end = output[n].indexOf(" :");
					String BSSID=output[n].substring(start,end);
					WifiAccessPoint wifi=GetAccessPoint("BSSID", BSSID);
					if (wifi==null){
						GetAccessPoints();
						break;
					}else {
						wifi.Refresh(output[n].substring(end+3));
					}
				}
			}

		} catch (Exception e) {
			if (fAccessPoints != null) {
				fAccessPoints.clear();
			}
			e.printStackTrace();
		}

	}

	public String getMonitorName() {
		return MonitorName;
	}
}
