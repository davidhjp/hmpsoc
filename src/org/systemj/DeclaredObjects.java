package org.systemj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeclaredObjects {
	private String name;
	
	public enum Mod {
		INPUT, OUTPUT, INTERNAL
	}
	
	class Signal {
		public Mod dir;
		public String type;
		public String name;
		
		public Signal(String n, String t, Mod d){
			dir = d;
			type = t;
			name = n;
		}
	}
	
	class Channel {
		public Mod dir;
		public String type;
		public String name;
		
		
		public Channel(String n, String t, Mod d){
			dir = d;
			type = t;
			name = n;
		}
	}
	
	class Var {
		public String name;
		public String type;
		public boolean array = false;
		
		public Var(String n, String type, boolean array){
			name=n;
			this.type = type;
			this.array = array;
		}
	}
	
	public DeclaredObjects(String n) { name = n; }
	public String getCDName() { return name; }
	
	private List<Signal> isignals = new ArrayList<Signal>();
	private List<Signal> osignals = new ArrayList<Signal>();
	private List<Signal> signals = new ArrayList<Signal>();
	private List<Channel> ichans = new ArrayList<Channel>();
	private List<Channel> ochans = new ArrayList<Channel>();
	private List<Var> vardecls = new ArrayList<Var>();
	
	public Iterator<Signal> getInputSignalIterator(){
		return isignals.iterator();
	}
	
	public Iterator<Signal> getOutputSignalIterator(){
		return osignals.iterator();
	}
	
	public Iterator<Signal> getInternalSignalIterator(){
		return signals.iterator();
	}
	
	public Iterator<Channel> getOutputChannelIterator(){
		return ochans.iterator();
	}
	
	public Iterator<Channel> getInputChannelIterator(){
		return ichans.iterator();
	}
	
	public Iterator<Var> getVarDeclIterator(){
		return vardecls.iterator();
	}
	
	public void addVariable(String n, String type, boolean array){
		vardecls.add(new Var(n,type,array));
	}
	
	public void addSignal(String n, String t, Mod d){
		if(d == Mod.INPUT)
			isignals.add(new Signal(n,t,d));
		else if(d == Mod.OUTPUT)
			osignals.add(new Signal(n,t,d));
		else if(d == Mod.INTERNAL)
			signals.add(new Signal(n,t,d));
	}
	
	public void addChannel(String n, String t, Mod d){
		if(d == Mod.INPUT)
			ichans.add(new Channel(n,t,d));
		else if(d == Mod.OUTPUT)
			ochans.add(new Channel(n,t,d));
		else if(d == Mod.INTERNAL)
			throw new RuntimeException("Channels can only be their input/output");
	}
	
	public boolean hasInternalSignal(String n){
		for(Signal s : signals){
			if(s.name.equals(n))
				return true;
		}
		return false;
	}
	
	public String getInternalSignalType(String n){
		for(Signal s : signals){
			if(s.name.equals(n))
				return s.type;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("============ "+name+" Interface ============\n");
		sb.append("-------- Input signals --------\n");
		for(Signal s : isignals){
			sb.append("Name : "+s.name+", Type : "+s.type+"\n");
		}
		sb.append("-------- Output signals --------\n");
		for(Signal s : osignals){
			sb.append("Name : "+s.name+", Type : "+s.type+"\n");
		}
		sb.append("-------- Internal signals --------\n");
		for(Signal s : signals){
			sb.append("Name : "+s.name+", Type : "+s.type+"\n");
		}
		sb.append("-------- Input channels --------\n");
		for(Channel s : ichans){
			sb.append("Name : "+s.name+", Type : "+s.type+"\n");
		}
		sb.append("-------- Output channels --------\n");
		for(Channel s : ochans){
			sb.append("Name : "+s.name+", Type : "+s.type+"\n");
		}
		sb.append("-------- Variable decls --------\n");
		Iterator<Var> iter = this.vardecls.iterator();
		while(iter.hasNext()){
			Var v = iter.next();
			sb.append("Name: "+v.name+", Type: "+v.type+", Array?: "+v.array+"\n");
		}
		return sb.toString();
	}
}








