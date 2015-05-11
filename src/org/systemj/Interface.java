package org.systemj;

import java.util.ArrayList;
import java.util.List;

public class Interface {
	private String name;
	
	class Signal {
		public boolean isIn = true;
		public String type;
		public String name;
		
		public void setOut() { isIn = false; }
		public Signal(String n, String t, boolean d){
			isIn = d;
			type = t;
			name = n;
		}
	}
	
	class Channel {
		public boolean isIn = true;
		public String type;
		public String name;
		
		public void setOut() { isIn = false; }
		
		public Channel(String n, String t, boolean d){
			isIn = d;
			type = t;
			name = n;
		}
	}
	
	public Interface(String n) { name = n; }
	public String getCDName() { return name; }
	
	private List<Signal> isignals = new ArrayList<Signal>();
	private List<Signal> osignals = new ArrayList<Signal>();
	private List<Channel> ichans = new ArrayList<Channel>();
	private List<Channel> ochans = new ArrayList<Channel>();
	
	public void addSignal(String n, String t, boolean d){
		if(d)
			isignals.add(new Signal(n,t,d));
		else
			osignals.add(new Signal(n,t,d));
	}
	
	public void addChannel(String n, String t, boolean d){
		if(d)
			ichans.add(new Channel(n,t,d));
		else
			ochans.add(new Channel(n,t,d));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("============ "+name+" Interface ============\n");
		sb.append("-------- Input signals --------\n");
		for(Signal s : isignals){
			sb.append("Name : "+s.name+" Type : "+s.type+"\n");
		}
		sb.append("-------- Output signals --------\n");
		for(Signal s : osignals){
			sb.append("Name : "+s.name+" Type : "+s.type+"\n");
		}
		sb.append("-------- Input channels --------\n");
		for(Channel s : ichans){
			sb.append("Name : "+s.name+" Type : "+s.type+"\n");
		}
		sb.append("-------- Output channels --------\n");
		for(Channel s : ochans){
			sb.append("Name : "+s.name+" Type : "+s.type+"\n");
		}
		return sb.toString();
	}
}








