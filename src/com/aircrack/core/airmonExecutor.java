package com.aircrack.core;

import java.util.ArrayList;

public class airmonExecutor {
	private ArrayList<String> killedProcesses;
	private ArrayList<String> daemonProcesses;

	private ArrayList<WifiInterface> finterfaces;
	private SysCommandExecutor exec;

	public airmonExecutor() {
		exec = new SysCommandExecutor();
	}

	public ArrayList<WifiInterface> getInterfaces() {
		try {
			exec.runSyncCommand("airmon-ng",5);
			String[] result = exec.getCommandOutput().trim().split("\n");
			
			if (result.length > 2) {
				if (finterfaces == null) {
					finterfaces = new ArrayList<WifiInterface>();
				} else {
					finterfaces.clear();
				}

				for (int n = 2; n < result.length; n++) {
					
					finterfaces.add(new WifiInterface(result[n]));
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return finterfaces;
	}

	public WifiInterface getInterface(String Name){
		WifiInterface res=null;
		if (finterfaces==null){
			getInterfaces();
		}
		if (finterfaces!=null){
			for (int n=0;n<finterfaces.size();n++){
				if(finterfaces.get(n).getName().equals(Name)){
					res= finterfaces.get(n);
					break;
				}
			}
		}
		return res;
	}
	
	public void stopAnnoyingProcesses(){
		try {
			// First, we get all processes
			exec.runSyncCommand("airmon-ng check",2);
			String[] output=exec.getCommandOutput().split("\n");
			int end=output.length-1;
			if (output[end].contains("Process")){end--;}
			if (output.length>6){
				killedProcesses=new ArrayList<String>();
				daemonProcesses=new ArrayList<String>();
				for (int n=7;n<end;n++){
					killedProcesses.add(output[n].split("\t")[1]);
				}
			}
			
			//Now, we kill those that can be killed (non daemons)
			exec.runSyncCommand("airmon-ng check kill", 20);
			output=exec.getCommandOutput().split("\n");
			if (output[end].contains("Process")){end--;}
			if (output.length>6){
				for (int n=7;n<end;n++){
					String proc=output[n].split("\t")[1];
					killedProcesses.remove(proc);
					daemonProcesses.add(proc);
					if (proc.equals("NetworkManager")){
						exec.runSyncCommand("stop network-manager");
					}else {
						exec.runSyncCommand("stop " + proc);
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void startAnnoyingProcesses(){
		for (int n=0;n<daemonProcesses.size();n++){
			try {
				if (daemonProcesses.get(n).equals("NetworkManager")){
					exec.runSyncCommand("start network-manager", 2);
				}else {
					exec.runSyncCommand("start " + daemonProcesses.get(n), 2);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (int n=0;n<killedProcesses.size();n++){
			try {
				exec.runSyncCommand(killedProcesses.get(n),2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
