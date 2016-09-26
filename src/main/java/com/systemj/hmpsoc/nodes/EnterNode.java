package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.HashMap;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;
import com.systemj.hmpsoc.util.Helper;

public class EnterNode extends BaseGRCNode {
	
	private String statename;
	private String statecode;
	
	private HashMap<String, SwitchNode> switches = new HashMap<>();
	
	public HashMap<String, SwitchNode> getSwitchMap() {
		return switches;
	}

	public void setSwitchMap(HashMap<String, SwitchNode> switches) {
		this.switches = switches;
	}

	public String getStatename() {
		return statename;
	}

	public void setStatename(String statename) {
		this.statename = statename;
	}

	public String getStatecode() {
		return statecode;
	}

	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"EnterNode, ID: "+id+"\n";
		ind = getIndent(indent);
		str += ind + "Statename: "+ statename + "\n";
		str += ind + "Statecode: " + statecode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent, DeclaredObjects doo) {
		try{
			int intcode = Integer.parseInt(statecode);
			SwitchNode sn = switches.get(statename);

			if(sn.getNumChildren() - 1 >= intcode) { // only if the number children is greater than what it is trying to encode
				pw.println("  LDR R10 #"+statename.toLowerCase()+"_"+statecode);
				pw.println("  STR R10 $"+Long.toHexString(mp.getSwitchNodePointer()+mp.switchMap.get(statename))+"; encoding "+statename);
			}
		} catch(NumberFormatException e){
			Helper.log.warning("EnerNode trying to encode without a statecode");
		}

		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi, this, doo);
		}
	}

}
