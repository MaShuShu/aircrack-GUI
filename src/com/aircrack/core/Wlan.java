package com.aircrack.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Wlan {
	private String fBSSID;
	private String fESSID;
	private Integer fPower;
	private Integer fData;
	private Integer fChannel;
	private String fEncryptation;
	private String fCipher;
	private Node fXMLSource;
	private Integer fNumber;

	public Wlan(Node XMLSource) {
		if (XMLSource != null) {
			this.fXMLSource = XMLSource;
		}
		Parse();
	}

	private void Parse() {
		fNumber = Integer.parseInt(fXMLSource.getAttributes()
				.getNamedItem("number").getTextContent());
		Element node = (Element) fXMLSource;
		fBSSID = ((Element) node.getElementsByTagName("BSSID").item(0))
				.getTextContent();
		fChannel = Integer.parseInt(((Element) node.getElementsByTagName(
				"channel").item(0)).getTextContent());

		Element subnode = ((Element) node.getElementsByTagName("SSID").item(0));
		fESSID = ((Element) subnode.getElementsByTagName("essid").item(0))
				.getTextContent();
		String Crypto = ((Element) subnode.getElementsByTagName("encryption")
				.item(0)).getTextContent().trim();
		fEncryptation = Crypto.split(" ")[0];
		if (Crypto.split(" ").length > 1) {
			fCipher = Crypto.split(" ")[1];
		}

		NodeList subnodes = node.getElementsByTagName("packets");
		for (int n = 0; n < subnodes.getLength(); n++) {
			if (subnodes.item(n).getChildNodes().getLength() > 1){
				subnode=((Element)subnodes.item(n));
				fData=Integer.parseInt(((Element)subnode.getElementsByTagName("data").item(0)).getTextContent());
			}
		}
		subnode = ((Element) node.getElementsByTagName("snr-info").item(0));
		fPower = Integer.parseInt(((Element) subnode
				.getElementsByTagName("last_signal_dbm").item(0)).getTextContent());

	}

	public Node getXMLSource() {
		return fXMLSource;
	}

	public void setXMLSource(Node fXMLSource) {
		this.fXMLSource = fXMLSource;
	}

	public String getBSSID() {
		return fBSSID;
	}

	public String getESSID() {
		return fESSID;
	}

	public Integer getPower() {
		return fPower;
	}

	public Integer getData() {
		return fData;
	}

	public Integer getChannel() {
		return fChannel;
	}

	public String getEncryptation() {
		return fEncryptation;
	}

	public String getCipher() {
		return fCipher;
	}

	public Integer getNumber() {
		return fNumber;
	}
}
