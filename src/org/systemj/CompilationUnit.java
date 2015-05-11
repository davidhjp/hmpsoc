package org.systemj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.systemj.nodes.AforkNode;
import org.systemj.nodes.BaseGRCNode;

public class CompilationUnit {
	private String target;
	private InputStream is;
	private SAXBuilder builder = new SAXBuilder();
	private Document doc;
	private boolean isis = false;
	
	private final static List<String> nodenames = Arrays.asList(new String[]{
			"ActionNode", "AjoinNode", "EnterNode", "ForkNode", "JoinNode",
			"SwitchNode", "TerminateNode", "TestNode", "AforkNode"
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
	
	private List<Interface> getInterfaces(){
		Element el = doc.getRootElement();
		Iterator<Element> ee = el.getDescendants(new ElementFilter("IO"));
		Element ioel = ee.next();
		List<Element> cdels = (List<Element>) ioel.getChildren("CD");
		List<Interface> l = new ArrayList();
		
		for(Element cdel : cdels){
			Interface cdit = new Interface(cdel.getAttributeValue("Name"));
			for(Element e : (List<Element>)cdel.getChildren()){
				switch(e.getName()){
				case "iSignal":
					cdit.addSignal(e.getChild("Name").getText(), e.getChild("Type").getText(), true);
					break;
				case "oSignal":
					cdit.addSignal(e.getChild("Name").getText(), e.getChild("Type").getText(), false);
					break;
				case "iChannel":
					cdit.addChannel(e.getChild("Name").getText(), e.getChild("Type").getText(), true);
					break;
				case "oChannel":
					cdit.addChannel(e.getChild("Name").getText(), e.getChild("Type").getText(), false);
					break;
				default:
					break;
				}
			}
			l.add(cdit);
			System.out.println(cdit);
		}
		
		return l;
	}
	
	/**
	 * Create AGRC Intermediate Representation for back-end code generation
	 * @author hpar081
	 */
	public void process(){
		List<Interface> l = getInterfaces();
		resetVisitTag((Element)doc.getRootElement().getDescendants(new ElementFilter("AGRC")).next());
		
		// ---- Debug
		XMLOutputter xmlo = new XMLOutputter();
		xmlo.setFormat(Format.getPrettyFormat());
		System.out.println(xmlo.outputString(doc.getRootElement()));
		// ----- 
		
		BaseGRCNode grc = getGRC();
	}

	public static boolean isAGRCNode(Element e){
		return nodenames.contains(e.getName());
	}
	
	private void resetVisitTag(Element e){
		if(isAGRCNode(e)){
			if(e.getAttributeValue("Visited") == null || e.getAttributeValue("Visited").equals("true")){
				e.setAttribute("Visited", "false");
			}
			else{
				return;
			}
		}

		for(Element ee : (List<Element>)e.getChildren()){
			resetVisitTag(ee);
		}
	}

	private BaseGRCNode getGRC() {
		Element grc = (Element) doc.getRootElement().getDescendants(new ElementFilter("AforkNode")).next();
		AforkNode afk = new AforkNode();
		
		for(Element n : (List<Element>)grc.getChildren()){
			getGRCTraverse(afk, n);
		}
		return null;
	}
	
	private BaseGRCNode getGRCTraverse(BaseGRCNode p, Element cur){
		// TODO: create node and traverse
		return null;
	}
}
