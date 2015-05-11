import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom.JDOMException;
import org.systemj.CompilationUnit;
import org.systemj.CompilerPrintStream;

import args.Helper;

/**
 * HMPSoC main class
 * @author hpar081
 *
 */
public class HMPSoC {



	public static Options initOptions() {
		Options options = new Options();
		options.addOption(Option.builder(Helper.VERBOSE_OPTION).longOpt(Helper.VERBOSE_LONG_OPTION).desc("Verbose mode").build());
		options.addOption(Option.builder(Helper.HELP_OPTION).longOpt(Helper.HELP_LONG_OPTION).desc("Print this help message").build());
		return options;
	}

	public static void printHelp(Options options) {
		HelpFormatter hf = new HelpFormatter();
		hf.setOptionComparator(new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				if(o1.getOpt().equals(Helper.HELP_OPTION)){
					return 1;
				}
				else if(o2.getOpt().equals(Helper.HELP_OPTION)){
					return -1;
				}
				return 0;
			}
		});
		CompilerPrintStream.setVerbose();
		hf.printHelp("jar hmpsoc.jar [OPTIONS] <filename>", options);
		CompilerPrintStream.resetVerbose();
	}

	public static void main(String[] args) {
		CompilerPrintStream cps = new CompilerPrintStream(System.out);
		CompilerPrintStream cpser = new CompilerPrintStream(System.err);
		System.setOut(cps);
		System.setErr(cpser);
		Options options = initOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
			Helper.setSingleArgInstance(cmd);
		} catch (ParseException e) {
			CompilerPrintStream.setVerbose();
			System.err.println(e.getMessage());
			System.exit(1);
		}

		for(Option o : cmd.getOptions()){
			if(o.getOpt().equals(Helper.HELP_OPTION)){
				printHelp(options);
				System.exit(0);
			}
			else{
				switch(o.getOpt()){
				case "v":
					CompilerPrintStream.setVerbose();
					CompilerPrintStream.setDefaultVerbose();
					break;
				}
			}
		}

		List<String> arglists = cmd.getArgList();
		try{
			if(!arglists.isEmpty()){
				for(String f : arglists){
					CompilationUnit cu = new CompilationUnit(f);
					cu.process();
				}
			}
			else{
				CompilationUnit cu = new CompilationUnit(System.in);
				cu.process();
			}
		} catch (Exception e){
			CompilerPrintStream.setVerbose();
			if(cmd.hasOption("v"))
				e.printStackTrace();
			else
				System.err.println(e.getMessage());
			
			System.exit(1);
		}
	}
}
