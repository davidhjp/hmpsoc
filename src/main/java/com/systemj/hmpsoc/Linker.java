package com.systemj.hmpsoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import args.Helper;

public class Linker {
	private String ifile;
	private File ofile;
	private String[] jopbin;
	private static final String PATTERN_DCALL_ANNOTATION = "\\.*@Datacall\\s*\\((.*?)\\)";
	private static final String PATTERN_ARGS = "\"([^,]+)\"";
	private static final String PATTERN_JOP_METHOD = "(\\d+):.*?(?:JOP(\\d+))?.(\\w+).MethodCall_(\\d+)";

	public Linker(String rasm, String[] jopbin) {
		ifile = rasm;
		ofile = new File(Paths.get(ifile.split("\\.(?=[^\\.]+$)")[0] + ".asm").getFileName().toString());
		this.jopbin = jopbin;
	}
	
	private static String getTarget(String jopid, String cdname) {
		return (jopid == null ? "" : UglyPrinter.getJOPPackageName(Integer.valueOf(jopid)) + "_") + cdname;
	}

	public void link() throws IOException {
		Map<String, Map<String, String>> madr = new HashMap<>();
		Pattern dc = Pattern.compile(PATTERN_DCALL_ANNOTATION);
		Pattern args = Pattern.compile(PATTERN_ARGS);
		Pattern jm = Pattern.compile(PATTERN_JOP_METHOD);
		
		for(String jopbin : jopbin){
			try(Stream<String> jlines = Files.lines(Paths.get(jopbin));){
				jlines.forEach(l -> {
					Matcher m = jm.matcher(l);
					if (m.find()) {
						String adr = m.group(1);
						String jopid = m.group(2);
						String cdname = m.group(3);
						String casen = m.group(4);
						final String target = getTarget(jopid, cdname);
						if (!madr.containsKey(target))
							madr.put(target, new HashMap<>());
						Map<String, String> adrm = madr.get(target);
						adrm.put(casen, adr);
					}
				});
			}
		}
		
		try (Stream<String> rlines = Files.lines(Paths.get(ifile));) {
			Stream<String> mapped = rlines.map(l -> {
				Matcher m = dc.matcher(l);
				if(m.find()){
					String annot = m.group(1);
					m = args.matcher(annot);
					Deque<String> q = new ArrayDeque<>();
					while(m.find()){
						q.push(m.group(1));
					}
					String cdname = q.removeLast();
					String jopid = q.removeLast();
					jopid = jopbin.length == 1 ? null : jopid;
					String casen = q.removeLast();
					final String target = getTarget(jopid, cdname);
					Map<String,String> adrm = madr.get(target);
					if(adrm == null)
						throw new RuntimeException("Could not resolve symbolic link "+target);
					String adr = adrm.get(casen);
					if(adr == null)
						throw new RuntimeException("Could not resolve symolic link "+annot);
					return l.replaceAll(PATTERN_DCALL_ANNOTATION, "#"+adr);
				}
				return l;
			});

			String odir = Helper.getSingleArgInstance().getOptionValue(Helper.D_OPTION);
			if (odir != null)
				ofile = new File(odir, ofile.getName());
			try (PrintWriter pw = new PrintWriter(new FileWriter(ofile))) {
				mapped.forEach(l -> pw.println(l));
			}
		}
	}

	public static void main(String[] args) {
		Pattern dc = Pattern.compile("(\\d+):.*?(?:JOP(\\d+))?.(\\w+).MethodCall_(\\d+)");
		Matcher m = dc.matcher("// 1232: hmpsoc.JOP2.ConveyorCD.MethodCall_123(sfwpaga) ");
		if(m.find()){
			System.out.println(m.group(4));
		} else
			System.out.println("not match");
		System.exit(1);
	}

}
