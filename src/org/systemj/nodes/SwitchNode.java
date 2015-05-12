package org.systemj.nodes;

public class SwitchNode extends BaseGRCNode {
	private String CDName;
	private String Statename;

	public String getStatename() {
		return Statename;
	}

	public void setStatename(String statename) {
		Statename = statename;
	}

	public String getCDName() {
		return CDName;
	}

	public void setCDName(String cDName) {
		CDName = cDName;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"SwitchNode\n";
		ind = getIndent(indent);
		str += ind + "Statename: "+ Statename + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

	
}
