package org.systemj.nodes;

import java.util.ArrayList;
import java.util.List;

public class BaseGRCNode {

	public static final String ACTION_NODE		= "ActionNode"    ;
	public static final String AJOIN_NODE 		= "AjoinNode"     ;
	public static final String ENTER_NODE		= "EnterNode"     ;
	public static final String FORK_NODE        = "ForkNode"      ;
	public static final String JOIN_NODE        = "JoinNode"      ;
	public static final String SWITCH_NODE      = "SwitchNode"    ;
	public static final String TERMINATE_NODE   = "TerminateNode" ;
	public static final String TEST_NODE        = "TestNode"      ;
	public static final String AFORK_NODE       = "AforkNode"     ;

	protected boolean isTopLevel = false;
	protected boolean visited = false;
	protected List<BaseGRCNode> children = new ArrayList<BaseGRCNode>();
	protected List<BaseGRCNode> parents = new ArrayList<BaseGRCNode>();
	protected String id = this.getClass().getSimpleName()+"@"+Integer.toHexString(this.hashCode());
	

	
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	
	public void addChild(BaseGRCNode n){
		children.add(n);
	}
	public BaseGRCNode getChild(int i){
		return children.get(i);
	}
	public void setChild(int i, BaseGRCNode n){
		children.set(i, n);
	}
	public int getNumChildren(){
		return children.size();
	}
	
	public void addParent(BaseGRCNode p){
		parents.add(p);
	}
	public BaseGRCNode getParent(int i){
		return parents.get(i);
	}
	public int getNumParents(){
		return parents.size();
	}
	public void setParent(int i, BaseGRCNode p){
		parents.set(i, p);
	}
	
	public List<BaseGRCNode> getChildren(){
		return children;
	}
	public List<BaseGRCNode> getParents(){
		return parents;
	}
	
	public static void connectParentChild(BaseGRCNode parent, BaseGRCNode child){
		parent.addChild(child);
		child.addParent(parent);
	}
	
	public boolean isTopLevel() { return isTopLevel; }
	public void setTopLevel() { isTopLevel = true; }
	
	
	public static String getIndent(int indent){
		String ind = "";
		for(int i=indent; i>0 ; i--){
			ind += " ";
		}
		return ind;
	}
	
	public static String getIndent(int indent, char c){
		String ind = "";
		for(int i=indent; i>0 ; i--){
			ind += c;
		}
		return ind;
	}
	
//	public String dump() {
//		String str = new String(this.getClass().getSimpleName()+/*"@"+Integer.toHexString(this.hashCode())+*/"\n");
//		for(BaseGRCNode child : children){
//			str += child.dump(1);
//		}
//		return str;
//	}

	
	public String dump(int indent){
		String ind = getIndent(indent,'-');
		String str = new String(ind+this.getClass().getSimpleName()+/*"@"+Integer.toHexString(this.hashCode())+*/"\n");
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		return str;
	}
	
	
}
