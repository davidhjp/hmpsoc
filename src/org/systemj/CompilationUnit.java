package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.systemj.nodes.ActionNode;
import org.systemj.nodes.AforkNode;
import org.systemj.nodes.AjoinNode;
import org.systemj.nodes.BaseGRCNode;
import org.systemj.nodes.EnterNode;
import org.systemj.nodes.ForkNode;
import org.systemj.nodes.JoinNode;
import org.systemj.nodes.SwitchNode;
import org.systemj.nodes.TerminateNode;
import org.systemj.nodes.TestNode;

import args.Helper;

public class CompilationUnit {
	private String target;
	private InputStream is;
	private SAXBuilder builder = new SAXBuilder();
	private Document doc;
	private boolean isis = false;
	
	private final static List<String> nodenames = Arrays.asList(new String[]{
			BaseGRCNode.ACTION_NODE,
			BaseGRCNode.AFORK_NODE,
			BaseGRCNode.AJOIN_NODE,
			BaseGRCNode.ENTER_NODE,
			BaseGRCNode.FORK_NODE,
			BaseGRCNode.JOIN_NODE,
			BaseGRCNode.SWITCH_NODE,
			BaseGRCNode.TERMINATE_NODE,
			BaseGRCNode.TEST_NODE
	});
	
	public CompilationUnit(){}
	
	public CompilationUnit(String file) throws JDOMException, IOException{
		target = file;
		doc = builder.build(new File(file));
	}
	
	public CompilationUnit(InputStream is) throws JDOMException, IOException{
		this.is = is;
		isis = true;
		doc = builder.build(this.is);

	}
	
	private List<DeclaredObjects> getDeclaredObjects(){
		Element el = doc.getRootElement();
		Iterator<Element> ee = el.getDescendants(new ElementFilter("IO"));
		Element ioel = ee.next();
		List<Element> cdels = (List<Element>) ioel.getChildren("CD");
		List<DeclaredObjects> l = new ArrayList();
		List<Element> cdsws = (List<Element>)((Element) el.getDescendants(new ElementFilter("AforkNode")).next()).getChild("Children").getChildren();
		
		for(Element cdel : cdels){
			String cdname = cdel.getAttributeValue("Name");
			DeclaredObjects cdit = new DeclaredObjects(cdname);
			for(Element e : (List<Element>)cdel.getChildren()){
				switch(e.getName()){
				case "iSignal":
					cdit.addSignal(e.getChild("Name").getText(), e.getChild("Type").getText(), DeclaredObjects.Mod.INPUT);
					break;
				case "oSignal":
					cdit.addSignal(e.getChild("Name").getText(), e.getChild("Type").getText(), DeclaredObjects.Mod.OUTPUT);
					break;
				case "iChannel":
					cdit.addChannel(e.getChild("Name").getText(), e.getChild("Type").getText(), DeclaredObjects.Mod.INPUT);
					break;
				case "oChannel":
					cdit.addChannel(e.getChild("Name").getText(), e.getChild("Type").getText(), DeclaredObjects.Mod.OUTPUT);
					break;
				default:
					break;
				}
			}
			
			for(Element cdsw : cdsws){
				String cdname2 = cdsw.getChildText("CDName");
				if(cdname.equals(cdname2)){
					Iterator<Element> si = cdsw.getDescendants(new ElementFilter("SignalDeclStmt"));
					while(si.hasNext()){
						Element e = si.next();
						String signame = e.getChildText("Name");
						if(!cdit.hasInternalSignal(signame))
							cdit.addSignal(e.getChildText("Name"), e.getChildText("Type"), DeclaredObjects.Mod.INTERNAL);
					}
				}
			}
			
			Element vardecls = el.getChild("VarDecls");
			for(Element cd : (List<Element>)vardecls.getChildren()){
				String cdname2 = cd.getAttributeValue("Name");
				if(cdname.equals(cdname2)){
					Iterator<Element> si = cd.getDescendants(new ElementFilter("VarDecl"));
					while(si.hasNext()){
						Element e = si.next();
						String signame = e.getChildText("Name");
						boolean isArray = false;
						if(e.getChild("Array") != null)
							isArray=true;
						cdit.addVariable(signame, e.getChildText("Type"),isArray);
					}
				}
			}
			
			l.add(cdit);
			System.out.println(cdit);
		}
		
		return l;
	}
	
	private List<Element> getInternalSignalDecls(Element e){
		ArrayList<Element> l = new ArrayList<Element>();
		if(e.getName().equals("SignalDeclStmt")){
			l.add(e);
		}

		List<Element> children = e.getChildren();
		if(children != null){
			for(Element ee : children){
				l.addAll(getInternalSignalDecls(ee));
			}
		}
		return l;
	}
	
	
	/**
	 * Create AGRC Intermediate Representation for back-end code generation
	 * @author hpar081
	 * @throws FileNotFoundException 
	 */
	public void process() throws Exception {
		List<DeclaredObjects> l = getDeclaredObjects();
//		resetVisitTagAGRC((Element)doc.getRootElement().getDescendants(new ElementFilter("AGRC")).next());
		
		// ---- Debug
//		XMLOutputter xmlo = new XMLOutputter();
//		xmlo.setFormat(Format.getPrettyFormat());
//		System.out.println(xmlo.outputString(doc.getRootElement()));
		// ----- 
		
		List<BaseGRCNode> glist = getGRC(l);
		for(BaseGRCNode n : glist){
			splitTestAction(n);
			n.resetVisited();
		}

		for(BaseGRCNode n : glist)
			groupActions(n);
		
		for(BaseGRCNode n : glist)
			setCaseNumber(n, 1);
		
		int mjop = 1;
		if(Helper.getSingleArgInstance().hasOption("j"))
			mjop = Integer.valueOf(Helper.getSingleArgInstance().getOptionValue("j")) + 1;
		for(BaseGRCNode n : glist){
			setActionTag(n, mjop);
			n.resetVisited();
		}
		
		Map<Integer, List<ActionNode>> m = new HashMap<Integer, List<ActionNode>>();
		for(BaseGRCNode n : glist){
			getActions(n, m);
			n.resetVisited();
		}
		
		UglyPrinter printer = new UglyPrinter(glist);
		if(Helper.getSingleArgInstance().hasOption(Helper.D_OPTION)){
			printer.setDir(Helper.getSingleArgInstance().getOptionValue(Helper.D_OPTION));
		}
		printer.setDelcaredObjects(l);
		printer.setActmap(m);
		printer.uglyprint();
		
		// Debug
//		for(BaseGRCNode gg : glist){
//			System.out.println("===");
//			System.out.println(gg.dump(0));
//		}
	}
	
	
	private int setCaseNumber(BaseGRCNode n, int casen) {
		if(n instanceof ActionNode){
			if(((ActionNode) n).getCasenumber() < 0)
				((ActionNode)n).setCasenumber(casen++);
		}
		
		for(BaseGRCNode child : n.getChildren()){
			casen = setCaseNumber(child, casen);
		}
		
		return casen;
	}

	private void splitTestAction(BaseGRCNode n) {
		if(!n.isVisited()){
			n.setVisited(true);
			
			if(n instanceof TestNode){
				if(((TestNode) n).isJavastmt()){
					ActionNode an = new ActionNode();
					an.setStmt(((TestNode)n).getExpr());
					an.setActionType(ActionNode.TYPE.JAVA);
					if(n.getNumParents() > 1)
						throw new RuntimeException("TestNode cannot have more than one parent");
					BaseGRCNode bcn = n.getParent(0);
					for(int i=0; i<bcn.getNumChildren(); i++){
						if(bcn.getChild(i).equals(n)){
							bcn.setChild(i, an);
						}
					}
					an.addChild(n);
					n.setParent(0, an);
					an.setThnum(n.getThnum());
					an.setBeforeTestNode(true);
				}
			}
			
			for(BaseGRCNode child : n.getChildren()){
				splitTestAction(child);
			}
			
		}
	}

	public void getActions(BaseGRCNode n, Map<Integer, List<ActionNode>> m){
		if(!n.isVisited()){
			n.setVisited(true);
			if(n instanceof ActionNode){
				int id = ((ActionNode) n).getJopid();
				if(m.containsKey(id)){
					m.get(id).add((ActionNode)n);
				}
				else{
					ArrayList<ActionNode> l = new ArrayList<ActionNode>();
					l.add((ActionNode)n);
					m.put(id, l);
				}
			}
			for(BaseGRCNode child : n.getChildren()){
				getActions(child, m);
			}
		}
	}
	
	public void setActionTag(BaseGRCNode n, int mjop){
		if(!n.isVisited()){
			n.setVisited(true);
			if(n instanceof ActionNode){
				if(n.getThnum() == -1)
					throw new RuntimeException("ActionNode Thnum < 0 !!");
				int jopid = n.getThnum() % mjop;
				jopid = jopid == 0 ? 1 : jopid;
				((ActionNode) n).setJopid(jopid);
			}

			for(BaseGRCNode child : n.getChildren()){
				setActionTag(child, mjop);
			}
		}
	}
	
	
	public void groupActions(BaseGRCNode n){
		if(n.getNumParents() == 1 && n.getNumChildren() == 1 && n.getParent(0).getNumChildren() == 1){
			if(n instanceof ActionNode){
				ActionNode an = (ActionNode)n;
				if(n.getParent(0) instanceof ActionNode){
					ActionNode pan = (ActionNode)n.getParent(0);

					boolean grouped = false;
					if(pan.getActionType() == ActionNode.TYPE.GROUPED_JAVA){
						if(an.getActionType() == ActionNode.TYPE.JAVA){
							pan.addStmt(an.getStmt());
							grouped = true;
						}
					}
					else if (pan.getActionType() == ActionNode.TYPE.JAVA){
						if(an.getActionType() == ActionNode.TYPE.JAVA){
							pan.setActionType(ActionNode.TYPE.GROUPED_JAVA);
							pan.addStmt(pan.getStmt());
							pan.addStmt(an.getStmt());
							grouped = true;
						}
					}
					if(grouped){
						pan.setChildren(an.getChildren());
						List<BaseGRCNode> parents = an.getChild(0).getParents();
						for(int i=0; i<parents.size(); i++){
							if(parents.get(i).equals(an)){
								parents.set(i, pan);
							}
						}
					}
				}
			}
		}
		
		for(BaseGRCNode child : n.getChildren()){
			groupActions(child);
		}
	}
	

	public static boolean isAGRCNode(Element e){
		return nodenames.contains(e.getName());
	}
	
//	private void resetVisitTag(Element e){
//		e.removeAttribute("Visited");
//		
//		for(Element ee : (List<Element>)e.getChildren()){
//			resetVisitTag(ee);
//		}
//	}
//	
//	private void resetVisitTagAGRC(Element e){
//		if(isAGRCNode(e)){
//			if(e.getAttributeValue("Visited") == null || e.getAttributeValue("Visited").equals("true")){
//				e.setAttribute("Visited", "false");
//			}
//			else{
//				return;
//			}
//		}
//		else
//			e.removeAttribute("Visited");
//
//		for(Element ee : (List<Element>)e.getChildren()){
//			resetVisitTagAGRC(ee);
//		}
//	}

	private List<BaseGRCNode> getGRC(List<DeclaredObjects> l) {
		Element grc = (Element) doc.getRootElement().getDescendants(new ElementFilter("AforkNode")).next();
		Element children = grc.getChild("Children");
		AforkNode afk = new AforkNode();
		List<BaseGRCNode> ll = new ArrayList<BaseGRCNode>();
		
		for(Element n : (List<Element>)children.getChildren()){
			String cdname = n.getChildText("CDName");
			DeclaredObjects co = null;
			for(DeclaredObjects c : l){
				if(c.getCDName().equals(cdname))
					co = c;
			}
			if(co == null) throw new RuntimeException("Could not find a matched CD in CommObjects");
			BaseGRCNode gn = getGRCTraverse(afk, n, new HashMap<String,BaseGRCNode>(), co);
			ll.add(gn);
		}
//		this.resetVisitTagAGRC(grc);
		return ll;
	}
	
	
	private  BaseGRCNode getNode(Element e, DeclaredObjects co){
		switch(e.getName()){
		case BaseGRCNode.ACTION_NODE:
			ActionNode an = new ActionNode();
			an.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			Element cel = e.getChild("SignalDeclStmt");
			if(cel != null){
				an.setStmt(cel.getChildText("Name")+".setClear();");
				an.setActionType(ActionNode.TYPE.SIG_DECL);
				return an;
			}
//			cel = e.getChild("VariableDeclaration");
//			if(cel != null){
//				an.setStmt(cel.getChildText("Name")+" = "+cel.getChildText("VarInit")+";");
//				an.setActionType(ActionNode.TYPE.VAR_DECL);
//				return an;
//			}
			cel = e.getChild("EmitStmt");
			if(cel != null){
				String name = cel.getChildText("Name");
				an.setSigName(name);
				String eval = cel.getChildText("Expr");
				if(eval != null){
					String type = co.getInternalSignalType(name);
					an.setSigType(co.getInternalSignalType(name));
					an.setEmitVal(eval);
					switch(type){
					case "int":
						eval = name+".setValue(new Integer("+eval+"));";
						break;
					case "short":
						eval = name+".setValue(new Short("+eval+"));";
						break;
					case "long":
						eval = name+".setValue(new Long("+eval+"));";
						break;
					case "byte":
						eval = name+".setValue(new Byte("+eval+"));";
						break;
					case "float":
						eval = name+".setValue(new Float("+eval+"));";
						break;
					case "char":
						eval = name+".setValue(new Character("+eval+"));";
						break;
					case "double":
						eval = name+".setValue(new Double("+eval+"));";
						break;
					}
					an.setStmt(eval);
				}
				an.setActionType(ActionNode.TYPE.EMIT);
				return an;
			}
			List<Element> l = e.getChildren();
			cel = l.get(0);
			an.setStmt(cel.getChildText("Expr"));
			an.setActionType(ActionNode.TYPE.JAVA);
			return an;
		case BaseGRCNode.AJOIN_NODE:
			AjoinNode ajn = new AjoinNode();
			ajn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			return ajn;
		case BaseGRCNode.ENTER_NODE:
			EnterNode en = new EnterNode();
			en.setStatecode(e.getChildText("Statecode"));
			en.setStatename(e.getChildText("Statename"));
			en.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			return en;
		case BaseGRCNode.FORK_NODE:
			ForkNode fn = new ForkNode();
			fn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			return fn;
		case BaseGRCNode.JOIN_NODE:
			JoinNode jn = new JoinNode();
			jn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			return jn;
		case BaseGRCNode.SWITCH_NODE:
			SwitchNode sn = new SwitchNode();
			sn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			sn.setStatename(e.getChildText("Statename"));
			String cdname = e.getChildText("CDName");
			if(cdname != null){
				sn.setCDName(cdname);
			}
			return sn;
		case BaseGRCNode.TERMINATE_NODE:
			TerminateNode tn = new TerminateNode();
			tn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			tn.setTermcode(e.getChildText("Value"));
			return tn;
		case BaseGRCNode.TEST_NODE:
			TestNode test = new TestNode();
			test.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			test.setExpr(e.getChildText("Expr"));
			if(e.getChild("Java") != null)
				test.setJavastmt(true);
			return test;
		case BaseGRCNode.AFORK_NODE:
			AforkNode afn = new AforkNode();
			afn.setThnum(Integer.valueOf(e.getAttributeValue("ThNum")));
			return afn;
		default:
			throw new RuntimeException("Unrecognized node type : "+e.getName());
		}
	}
	
	private BaseGRCNode getGRCTraverse(BaseGRCNode p, Element cur, Map<String,BaseGRCNode> m, DeclaredObjects co){
		if(!cur.getName().equals("NodeRef")){
			BaseGRCNode n = getNode(cur,co);
			BaseGRCNode.connectParentChild(p, n);
			m.put(cur.getAttributeValue("ID"), n);
			
			Element children = cur.getChild("Children");
			if(children != null){
				for(Element e : (List<Element>)children.getChildren()){
					getGRCTraverse(n, e, m,co);
				}
			}
			
			return n;
		}
		else{
			BaseGRCNode node = m.get(cur.getText());
			if(node != null)
				BaseGRCNode.connectParentChild(p, node);
			return node;
		}
		
	}
}














