package org.systemj.nodes;

public class EnterNode extends BaseGRCNode {
	
	private String statename;
	private String statecode;

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
		str += ind +"EnterNode\n";
		ind = getIndent(indent);
		str += ind + "Statename: "+ statename + "\n";
		str += ind + "Statecode: " + statecode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

}
