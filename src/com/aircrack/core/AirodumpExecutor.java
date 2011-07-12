package com.aircrack.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Timer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AirodumpExecutor extends Thread {
	private WifiInterface wifi;
	private WifiAccessPoint AP;
	private Timer timer1;
	private SysCommandExecutor exec = new SysCommandExecutor();
	private Vector AirodumpExecutorListeners = new Vector();

	public AirodumpExecutor(WifiInterface wifi, WifiAccessPoint AP) {
		this.wifi = wifi;
		this.AP = AP;
		
	}
	
	public synchronized void addAirodumpExecutorListener(
			AirodumpExecutorListener l) {
		AirodumpExecutorListeners.addElement(l);
	}

	public synchronized void removeAirodumpExecutorListener(AirodumpExecutorListener l) {
		AirodumpExecutorListeners.removeElement(l);
	}

	public void run() {
		exec.setErrorLogDevice(new AirodumpLogDevice(AP.getHWAddr(),AirodumpExecutorListeners));
		String cmd = String.format("airodump-ng --bssid %s -c %d -w %s %s",
				AP.getHWAddr(),AP.getChannel(), AP.getHWAddr(), wifi.getMonitorName());
		System.out.println(cmd);
		try {
			exec.runAsyncCommand(cmd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Stop() {
		exec.stop();
		this.interrupt();
	}

}

class NewDataEvent extends EventObject {
	private int data;

	public NewDataEvent(Object source, Integer data) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public long getdata() {
		return data;
	}

	// Type-safe access to source
	public AirodumpExecutor getStock() {
		return (AirodumpExecutor) getSource();
	}

}

// Define new listener interface
interface AirodumpExecutorListener extends EventListener {
	public abstract void onNewData(NewDataEvent e);
}

class AirodumpLogDevice implements ILogDevice {
	private Vector AirodumpExecutorListeners;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private File file;
	public Integer lastData;
	private String BSSID;

	//private Wlan wlan;

	public AirodumpLogDevice(String FileName,Vector AirodumpExecutorListeners ) {
		this.AirodumpExecutorListeners=AirodumpExecutorListeners;
		BSSID=FileName;
		try {
			file = new File(FileName + "-01.kismet.netxml");
			System.out.println(file.getAbsolutePath());
			// file.deleteOnExit();
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			db = dbf.newDocumentBuilder();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Wlan readXMLNode(File file) {
		Wlan res = null;
		try {
			
			if (file.exists()) {
				Document doc = db.parse(file);
				doc.getDocumentElement().normalize();
				NodeList nodeLst = doc.getElementsByTagName("wireless-network");
				if (nodeLst.getLength() > 0) {
					Node node = nodeLst.item(0);
					res = new Wlan(node);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return res;
	}

	public void log(String str) {
		System.out.println(str);
		String[] output=str.split("\n");
		String theOne="";
		int start=-1;
		int end=-1;
		
		for (int n=0;n<output.length;n++){
			if (output[n].trim().startsWith(BSSID)){
				theOne=output[n];
				break;
			}
		}
		if (!theOne.equals("")){
			try {
			theOne=theOne.trim().replace(BSSID,"");
			start=2;
			end=theOne.indexOf(" ",start);
			String Station=theOne.substring(start,end);
			theOne=theOne.substring(end+1).trim();
			
			String PWR=theOne.substring(0,end);
			theOne=theOne.substring(end).trim();
			
			end=theOne.indexOf(" ");
			String RXQ=theOne.substring(0,end);
			theOne=theOne.substring(end+1).trim();
			
			end=theOne.indexOf(" ");
			String Beacon=theOne.substring(0,end);
			theOne=theOne.substring(end+1).trim();
			
			end=theOne.indexOf(" ");
			String Data=theOne.substring(0,end);
			theOne=theOne.substring(end+1).trim();
			
			String txt=String.format("\n\n\nPWR:%s\nRXQ:%s\nBeacons:%s\nData:%s\n", PWR,RXQ,Beacon,Data);
			System.err.println(txt);
			//if (!Data.equals(lastData)){notifyNewData(Integer.parseInt(Data));}
			}catch (Exception e){
				System.err.println(e.getMessage());
			}
		
		}
		
	}


	protected void notifyNewData(int d) {
		
		String txt=String.format("New data=%d, old data=%d", d,lastData);
		System.out.println(txt);
		NewDataEvent e = new NewDataEvent(this, d);
		for (int i = 0; i < AirodumpExecutorListeners.size(); i++) {
			AirodumpExecutorListener l = (AirodumpExecutorListener) AirodumpExecutorListeners
					.elementAt(i);
			l.onNewData(e);
			lastData=d;
		}
	}

}