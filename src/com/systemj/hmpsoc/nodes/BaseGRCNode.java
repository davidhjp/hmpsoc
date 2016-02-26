package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.systemj.hmpsoc.MemoryPointer;

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
	protected int thnum = -1;
	
	public String id;

	public int getTopThreadNum(){
		if(this.isTopLevel())
			return this.getThnum();
		
		return this.getParent(0).getTopThreadNum();
	}
	
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public void resetVisited(){
		visited = false;
		
		for(BaseGRCNode n : children){
			n.resetVisited();
		}
	}
	
	public void removeChild(BaseGRCNode n){
		children.remove(n);
	}
	
	public void removeParent(BaseGRCNode n){
		parents.remove(n);
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
	
	public void setChildren(List<BaseGRCNode> c){
		children = c;
	}
	
	public void setParents(List<BaseGRCNode> p){
		parents = p;
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
		String str = new String(ind+this.getClass().getSimpleName()+", ID: "+id+"\n");
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		return str;
	}
	public int getThnum() {
		return thnum;
	}
	public void setThnum(int thnum) {
		this.thnum = thnum;
	}
	
	
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode, int cdi){
		pw.println("; TODO: Override "+this.getClass().getSimpleName()+".weirdPrint(..) method"); // TODO
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi);
		}
	}

	protected JoinNode getMatchingJoinNow(int i) {
		if(this instanceof JoinNode){
			if(i == 0)
				return (JoinNode)this;
			else
				i--;
		}
		else if(this instanceof ForkNode)
			i++;
		
		for(BaseGRCNode child : getChildren()){
			JoinNode r = child.getMatchingJoinNow(i);
			if(r != null)
				return r;
		}
		return null;
	}
}
