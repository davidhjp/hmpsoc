package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.MemoryPointer;

public class TestNode extends BaseGRCNode {
	private String expr;
	private boolean javastmt;
	private boolean rev;

	public boolean isRev() {
		return rev;
	}

	public void setRev(boolean rev) {
		this.rev = rev;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"TestNode\n";
		ind = getIndent(indent);
		str += ind + "Expr: "+ expr + "\n";
		if(isJavastmt())
			str += ind + "Java If"+ "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

	public boolean isJavastmt() {
		return javastmt;
	}

	public void setJavastmt(boolean javastmt) {
		this.javastmt = javastmt;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
//		int c = mp.signalMap.get(SigName);
//		long c1 = (c / mp.WORD_SIZE) + mp.getInternalSignalPointer();
//		long c2 = 1 << (c % mp.WORD_SIZE);
		String lbl = "TEST"+(mp.cc++)+"CD"+cdi;
		if(!this.isJavastmt()){
			long c1 = 0;
			long c2 = 0;
			boolean found = false;
			String st = null;
			if(mp.insignalMap.containsKey(this.expr)){
				int c = mp.insignalMap.get(this.expr);
				c1 = (c / mp.WORD_SIZE) + mp.getPreInputSignalPointer();
				c2 = 1 << (c % mp.WORD_SIZE);
				st = "ISig";
			}
			else if(mp.osignalMap.containsKey(this.expr)){
				int c = mp.osignalMap.get(this.expr);
				c1 = (c / mp.WORD_SIZE) + mp.getPreOutputSignalPointer();
				c2 = 1 << (c % mp.WORD_SIZE);
				st = "OSig";
			}
			else if(mp.signalMap.containsKey(this.expr)){
				int c = mp.signalMap.get(this.expr);
				c1 = (c / mp.WORD_SIZE) + mp.getPreInternalSignalPointer();
				c2 = 1 << (c % mp.WORD_SIZE);
				st = "Sig";
			}
			if(st == null)
				throw new RuntimeException("Currently only single signal testing is allowed: "+this.expr);
			
			pw.println("  LDR R10 $"+Long.toHexString(c1)+"; loading "+st);
			pw.println("  AND R10 R10 #$"+Long.toHexString(c2)+"; negating other signals to get "+this.expr);
			pw.println("  PRESENT R10 "+lbl);
		}
		else{
			long dl_ptr = mp.getDataLockPointer();
			long ttnum = this.thnum - mp.getToplevelThnum();
			dl_ptr += ttnum;
			pw.println("  LDR R10 $"+Long.toHexString(dl_ptr));
			pw.println("  AND R10 R10 #$0001 ; checking Java-if result");
			pw.println("  PRESENT R10 "+lbl);
		}
		
		if(this.isRev()){
			this.getChild(1).weirdPrint(pw, mp, termcode, cdi);;
		}
		else{
			this.getChild(0).weirdPrint(pw, mp, termcode, cdi);;
		}
		String oelse = "OVERELSE"+(mp.cc++)+"CD"+cdi;
		pw.println("  JMP "+oelse);
		pw.println(lbl+" NOOP");
		
		if(this.isRev()){
			this.getChild(0).weirdPrint(pw, mp, termcode, cdi);;
		}
		else{
			this.getChild(1).weirdPrint(pw, mp, termcode, cdi);;
		}
		
		pw.println(oelse+" NOOP");
		
	}
	
	

}
