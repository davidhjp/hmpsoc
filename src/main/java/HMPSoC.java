import java.io.FileReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import args.Helper;

import com.google.gson.Gson;
import com.systemj.hmpsoc.CompilationUnit;

import static com.systemj.hmpsoc.util.Helper.log;

/**
 * HMPSoC main class
 * @author hpar081
 *
 */
public class HMPSoC {



	public static Options initOptions() {
		Options options = new Options();
		options.addOption(Option.builder(Helper.VERBOSE_OPTION).longOpt(Helper.VERBOSE_LONG_OPTION).desc("Verbose mode").build());
		options.addOption(Option.builder(Helper.D_OPTION).hasArg().argName("directory").desc("Generate files to this output directory").build());
		options.addOption(Option.builder(Helper.JOP_RECOP_NUM_OPTION).hasArg().argName("alloc").desc("Specify JOP/ReCOP configuration").build());
		options.addOption(Option.builder(Helper.HELP_OPTION).longOpt(Helper.HELP_LONG_OPTION).desc("Print this help message").build());
		options.addOption(Option.builder(Helper.CONFIG_OPTION).longOpt(Helper.CONFIG_LONG_OPTION).hasArg().argName("file").desc("Specify SystemJ Configuration").build());
		options.addOption(Option.builder(Helper.DIST_MEM_OPTION).longOpt(Helper.DIST_MEM_LONG_OPTION).desc("Target distributed memory system").build());
		options.addOption(Option.builder(Helper.COMPILE_ONLY_OPTION).desc("Do not resolve symbolic links").build());
		options.addOption(Option.builder(Helper.DYN_DISPATCH_OPTION).desc("Datacall based on dynamic dispatching").build());
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
		hf.printHelp("java -jar hmpsoc.jar [OPTIONS] <filename>", options);
	}

	public static void main(String[] args) {
		Options options = initOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			String systemJConfig = null;
			cmd = parser.parse(options, args);
			Helper.setSingleArgInstance(cmd);

			for(Option o : cmd.getOptions()){
				if(o.getOpt().equals(Helper.HELP_OPTION)){
					printHelp(options);
					System.exit(0);
				}
				else{
					switch(o.getOpt()){
					case Helper.VERBOSE_OPTION:
						log.setLevel(Level.INFO);
						break;
					case Helper.JOP_RECOP_NUM_OPTION:
						String fname = o.getValue(Helper.JOP_RECOP_NUM_OPTION);
						Gson gs = new Gson();
						Helper.pMap = gs.fromJson(new FileReader(fname), Helper.Mapping.class);
						if(Helper.pMap.rAlloc == null)
							throw new RuntimeException("Required data in "+fname+" : rAlloc");
						Iterator<Integer> i = (Iterator<Integer>) Helper.pMap.rAlloc.values().iterator();
						while(i.hasNext()){
							int nn = i.next();
							if(Helper.pMap.nReCOP <= nn)
								throw new RuntimeException("ReCOP ID "+nn+" is outside the range of valid ID's. Valid ID's are 0 to " + (Helper.pMap.nReCOP-1));
						}
						if(Helper.pMap.nJOP < 0 || Helper.pMap.nReCOP < 0){
							throw new ParseException("Numbers of JOP/ReCOP should be greater than 0");
						}
						break;
					case Helper.CONFIG_OPTION:
					case Helper.CONFIG_LONG_OPTION:
						systemJConfig = o.getValue();
						break;
					default:
						break;
					}
				}
			}

			List<String> arglists = cmd.getArgList();
			if(!arglists.isEmpty()){
				for(String f : arglists){
					CompilationUnit cu = new CompilationUnit(f, systemJConfig);
					cu.process();
				}
			}
			else{
				CompilationUnit cu = new CompilationUnit(System.in, systemJConfig);
				cu.process();
			}
		} catch (Exception | Error e){
			if(cmd.hasOption(Helper.VERBOSE_OPTION))
				e.printStackTrace();
			else
				log.severe(e.toString());
			System.exit(1);
		}
	}
}
