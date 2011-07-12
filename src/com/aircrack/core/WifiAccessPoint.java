package com.aircrack.core;

public class WifiAccessPoint {
	private Integer CellNumber;
	private String HWAddr;
	private String Protocol;
	private String ESSID;
	private String Mode;
	private Integer Channel;
	private Double Quality;
	private Integer SignalLevel;
	private Integer NoiseLevel;
	private Boolean Encrypted;
	private String BitRates;

	public WifiAccessPoint(String StringSource) {
		Integer start = -1;
		Integer end = -1;
		String[] source = StringSource.split("\n");
		start = source[0].indexOf("Cell ") + 5;
		end = source[0].indexOf("-", start);
		CellNumber = Integer.parseInt(source[0].substring(start, end).trim());

		String token = "Address:";
		start = source[0].indexOf(token) + token.length();
		HWAddr = source[0].substring(start).trim();

		token = "Protocol:";
		start = source[1].indexOf(token) + token.length();
		Protocol = source[1].substring(start).trim();

		token = "ESSID:";
		start = source[2].indexOf(token) + token.length();
		ESSID = source[2].substring(start).trim().replace("\"", "");

		token = "Mode:";
		start = source[3].indexOf(token) + token.length();
		Mode = source[3].substring(start).trim();

		token = "Channel:";
		start = source[4].indexOf(token) + token.length();
		Channel = Integer.parseInt(source[4].substring(start).trim());

		token = "Quality:";
		start = source[5].indexOf(token) + token.length();
		end = source[5].indexOf("/100", start);
		Quality = Double.parseDouble((source[5].substring(start, end).trim())) / 100;

		token = "Signal level:";
		start = source[5].indexOf(token) + token.length();
		end = source[5].indexOf(" dBm", start);
		SignalLevel = Integer.parseInt((source[5].substring(start, end).trim()));

		token = "Noise level:";
		start = source[5].indexOf(token) + token.length();
		end = source[5].indexOf(" dBm", start);
		NoiseLevel = Integer.parseInt((source[5].substring(start, end).trim()));

		token = "Encryption key:";
		start = source[6].indexOf(token) + token.length();
		Encrypted = source[6].substring(start).trim().equals("on");

		token = "Bit Rates:";
		start = source[7].indexOf(token) + token.length();
		BitRates = source[7].substring(start).trim();
	}

	public void Refresh(String SourceString) {
		int start = -1;
		int end = 0;
		SourceString = SourceString.trim();
		
		String token = "Quality:";
		start = SourceString.indexOf(token) + token.length();
		end = SourceString.indexOf("/100", start);
		Quality = Double.parseDouble(SourceString.substring(start, end).trim()) / 100;
		
		token = "Signal level:";
		start = SourceString.indexOf(token) + token.length();
		end = SourceString.indexOf(" dBm", start);
		SignalLevel = Integer.parseInt(SourceString.substring(start, end).trim());
		
		token = "Noise level:";
		start = SourceString.indexOf(token) + token.length();
		end = SourceString.indexOf(" dBm", start);
		NoiseLevel = Integer.parseInt(SourceString.substring(start, end).trim());
	}

	public Integer getCellNumber() {
		return CellNumber;
	}

	public String getHWAddr() {
		return HWAddr;
	}

	public String getProtocol() {
		return Protocol;
	}

	public String getESSID() {
		return ESSID;
	}

	public String getMode() {
		return Mode;
	}

	public Integer getChannel() {
		return Channel;
	}

	public Double getQuality() {
		return Quality;
	}

	public Integer getSignalLevel() {
		return SignalLevel;
	}

	public Integer getNoiseLevel() {
		return NoiseLevel;
	}

	public Boolean getEncrypted() {
		return Encrypted;
	}

	public String getBitRates() {
		return BitRates;
	}
}
