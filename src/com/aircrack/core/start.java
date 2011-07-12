package com.aircrack.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class start {
	private static List<Wlan> Wlans;

	/**
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */

	/**
	 * To RUN : java -jar start.jar
	 * /home/patrick/temp/aircrack-ng/src/aircrack-ng -e PATHOME -Z dumpCpp.txt
	 * pathome*.cap
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException {
		if (new com.sun.security.auth.module.UnixSystem().getGid() != 0) {
			System.err.println("You must be root to run that stuff !!");
			System.exit(-1);
		}
		AirodumpExecutor airo=null;
		airmonExecutor airmon = new airmonExecutor();
		WifiInterface wifi = airmon.getInterfaces().get(0);
		
		try {
			ArrayList<WifiAccessPoint> APS=wifi.GetAccessPoints();
			//airmon.stopAnnoyingProcesses();
			wifi.StartMonitoring();
			WifiAccessPoint AP = APS.get(0);
			
			airo=new AirodumpExecutor(wifi, AP);
			airo.addAirodumpExecutorListener(new listener());
			airo.start();
			for (int n = 0; n < 10*25; n++) {
				try {
					Thread.currentThread().join(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			airo.Stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			// TODO Auto-generated catch block
			if (airo!=null){
				airo.Stop();
			}
			wifi.StopMonitoring();
			//airmon.startAnnoyingProcesses();
		}

		/* Works Perfectly
		airmonExecutor airmon = new airmonExecutor();
		WifiInterface wifi = airmon.getInterface("wlan0");
		wifi.GetAccessPoints();
		for (int n = 0; n < 100; n++) {
			wifi.refreshAccessPoints();
			WifiAccessPoint AP = wifi.GetAccessPoint("ESSID", "WLAN_66");
			if (AP != null) {
				long Q=Math.round(AP.getQuality() * 100);
				String txt = String.format(
						"AccessPoint %s: Quality:%d/100  Noise:%d Signal:%d",
						AP.getESSID(), Q,
						AP.getNoiseLevel(), AP.getSignalLevel());
				System.out.println(txt);
			}
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		*/
		
		/*
		 * Works perfectly 
		 * if (wifi!=null){ 
		 * try {
		 * airmon.stopAnnoyingProcesses(); System.out.println(wifi.getState());
		 * 
		 * wifi.StartMonitoring(); System.out.println(wifi.getState());
		 * 
		 * wifi.StopMonitoring(); System.out.println(wifi.getState()); } finally
		 * { airmon.startAnnoyingProcesses();
		 * System.out.println(wifi.getState()); } }
		 */

		if (args.length > 0) {
			FileOutputStream f = new FileOutputStream("dump.txt");
			FileOutputStream err = new FileOutputStream("error.txt");
			//FileOutputStream out = new FileOutputStream("output.txt");
			SysCommandExecutor exec = new SysCommandExecutor();
			//SysCommandExecutor exec2 = new SysCommandExecutor();
			exec.setOutputLogDevice(new LogDevice(f));
			// exec.setWorkingDirectory("/home/patrick/temp/");
			exec.setErrorLogDevice(new LogDevice(err));
			try {
				String cmd = "";
				if (args.length > 0) {
					for (int n = 0; n < args.length; n++) {
						cmd += args[n] + " ";
					}
				}

				exec.runAsyncCommand(cmd);
				// exec2.runAsyncCommand("airodump-ng -w toto wlan0");

				System.out.println("just started");
				for (int n = 0; n < 160; n++) {
					Thread.currentThread().join(250);
					/*
					 * String s=exec.getCommandOutput(); if (!s.isEmpty()) { try
					 * { out.write(s.getBytes()); out.flush(); } catch
					 * (Exception e) { // TODO: handle exception } }
					 * s=exec.getCommandError(); if (!s.isEmpty())
					 * {System.out.println("err:"+ s); }
					 */
					if (exec.hasFinished()) {
						break;
					}
				}

				System.out.println("20 seconds later...");
				f.close();
				err.close();
				//out.close();
				System.out.println("trying to destroy");
				exec.stop();
				// exec2.stop();
			} catch (Exception e) {
				System.out.println("error:" + e.getMessage());
				e.printStackTrace();
				f.close();
				err.close();
				//out.close();
			}
		}

		/*
		 * Wlans = new ArrayList<Wlan>();
		 * 
		 * File file = new
		 * File("/home/patrick/Documents/test-01.kismet.netxml");
		 * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 * DocumentBuilder db = dbf.newDocumentBuilder(); Document doc =
		 * db.parse(file); doc.getDocumentElement().normalize(); NodeList
		 * nodeLst = doc.getElementsByTagName("wireless-network");
		 * 
		 * for (int s = 0; s < nodeLst.getLength(); s++) {
		 * 
		 * Node fstNode = nodeLst.item(s);
		 * 
		 * if (fstNode.getNodeType() == Node.ELEMENT_NODE) { Wlans.add(new
		 * Wlan(fstNode)); }
		 * 
		 * }
		 */

	}

}

class LogDevice implements ILogDevice {
	private FileOutputStream f;

	public LogDevice(FileOutputStream f) {
		this.f = f;
	}

	public void log(String str) {

		if (f != null) {
			try {
				str += "\n";
				f.write(str.getBytes());
				f.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}

class listener implements AirodumpExecutorListener{

	@Override
	public void onNewData(NewDataEvent e) {
		System.out.println(e.getdata());
		
	}
	
}