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

	
}
