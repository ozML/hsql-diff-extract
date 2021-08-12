package de.ozml.hsqldiffextract.arg;

import static de.ozml.hsqldiffextract.arg.Argument.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.ozml.hsqldiffextract.res.Res;

/**
 * The class manages the arguments which are passed through the command line
 * and offers mnethods to access the values.
 */
public class ArgumentBag {

	private Map<String, String> arguments;

	public ArgumentBag(Map<String, String> arguments){
		this.arguments = arguments;
	}

	/**
	 * Returns whether interactive mode is requested
	 * @return
	 */
	public boolean isInteractive() {
		return collectArg(Argument.Interactive.getDefinition()) != null;
	}

	/**
	 * Returns whether lazy mode is requested.
	 * @return
	 */
	public boolean isLazyMode(){
		return collectArg(Lazy.getDefinition()) != null;
	}

	/**
	 * Returns the number of arguments contained by this instance.
	 * @return
	 */
	public int size(){
		return arguments.size();
	}

	/**
	 * Checks whether the argument requirements are met. The conditions are that non
	 * interactive mode is enabled and all mandatory arguments are present.
	 * @return
	 */
	public boolean claimsMet() {
		Predicate<Argument> p = arg -> arguments.containsKey(arg.getDefinition());
		return isInteractive() || Argument.getRequiredList().stream().allMatch(p);
	}

	/**
	 * Returns the value for the original file. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getOriginalFile(){
		String msg = "\n" + String.format(Res.loadString("msg.format.promptfile"), Res.loadString("ofile"));
		String errMsg = "\n" + Res.loadString("msg.filenonexist");
		String argDef = OriginalFile.getDefinition();

		return collectArg(argDef, line -> fileExists(line), msg, errMsg);
	}

	/**
	 * Returns the value for the changed file. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getChangedFile(){
		String msg = "\n" + String.format(Res.loadString("msg.format.promptfile"), Res.loadString("cfile"));
		String errMsg = "\n" + Res.loadString("msg.filenonexist");
		String argDef = ChangedFile.getDefinition();

		return collectArg(argDef, line -> fileExists(line), msg, errMsg);
	}

	/**
	 * Returns the value for the output directory. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getOutputDirectory(){
		String msg = "\n" + String.format(Res.loadString("msg.format.promptdir"), Res.loadString("odir"));
		String errMsg = "\n" + Res.loadString("msg.dirnonexist");
		String argDef = OutputDir.getDefinition();

		return collectArg(argDef, line -> dirExists(line), msg, errMsg);
	}

	/**
	 * Returns the table inclusion list. The list contains the names of the tables which 
	 * should be included. Only the tables contained in this list shall be computed.
	 * All names are converted to lower case.
	 * @return
	 */
	public List<String> getIncludeTables() {
		String inclusions = collectArg(IncludeTables.getDefinition());
		if(inclusions == null || inclusions.isBlank()){
			return null;
		}

		return Arrays.asList(inclusions.split(","))
			.stream().map(entry -> entry.trim().toLowerCase())
			.collect(Collectors.toList());
	}

	/**
	 * Returns the table exclusion list. The list contains the names of the tables which
	 * should be excluded. All tables contained in this list shall not be computed.
	 * All names are converted to lower case.
	 * @return
	 */
	public List<String> getExcludeTables() {
		String exclusions = collectArg(ExcludeTables.getDefinition());
		if(exclusions == null || exclusions.isBlank()){
			return null;
		}

		return Arrays.asList(exclusions.split(","))
			.stream().map(entry -> entry.trim().toLowerCase())
			.collect(Collectors.toList());
	}

	/**
	 * Calls {@link #collectArg(String, boolean, Predicate, String, String)} with the {@code nonInteractive}
	 * argument set to true.
	 * @param arg argument definition string
	 * @return
	 */
	private String collectArg(String arg) {
		return collectArg(arg, true, null, null, null);
	}

	/**
	 * Calls {@link #collectArg(String, boolean, Predicate, String, String)} with the {@code nonInteractive}
	 * argument set to false.
	 * @param arg argument definition string
	 * @param validation validation function for user input
	 * @param msg prompt message
	 * @param errMsg error message
	 * @return
	 */
	private String collectArg(String arg, Predicate<String> validation, String msg, String errMsg) {
		return collectArg(arg, false, validation, msg, errMsg);
	}

	/**
	 * Starts a routine to collect the specified arguments value. The arg parameter must
	 * be a definiton value of an {@link Argument} enum instance. The routine can be
	 * interactive or noninteractive, which depends on the return value of {@link #isInteractive()}
	 * and the value of the parameter {@code nonInteractive}. In noninteractive mode all following
	 * parameters are ignored and the value is directly read from the internal value map without 
	 * further validation. In interactive mode the arguments value is received through user input
	 * from command line. A validation of the input can be performed with the {@code validation}
	 * parameter. If {@code null} is passed for the parameter the validation is skipped. The parameter
	 * {@code msg} is used as prompt message and the {@code errMsg} as message for an invalid input.
	 * The value is cached after succeeded read attempt and therefore accessed through the cache on
	 * further requests.
	 * @param arg argument definition string
	 * @param nonInteractive mode flag
	 * @param validation validation function for user input
	 * @param msg prompt message
	 * @param errMsg error message
	 * @return
	 */
	private String collectArg(String arg, boolean nonInteractive, Predicate<String> validation, String msg, String errMsg) {
		String result = null;
		if(nonInteractive || arguments.containsKey(arg) || !isInteractive()){
			return arguments.get(arg);
		} else {
			Scanner scanner = new Scanner(System.in);
			while(true){
				System.out.println(msg);
				String line = scanner.nextLine().trim();

				if(validation != null && validation.test(line)){
					result = line;
					arguments.put(arg, line);
					break;
				}
				else{
					System.out.println(errMsg);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if the specified file exists.
	 * @param file file path
	 * @return
	 */
	private boolean fileExists(String file) {
		return !file.isEmpty() && new File(file).isFile();
	}

	/**
	 * Checks if the specified directory exists.
	 * @param directory directory path
	 * @return
	 */
	private boolean dirExists(String directory) {
		return !directory.isEmpty() && new File(directory).isDirectory();
	}

}