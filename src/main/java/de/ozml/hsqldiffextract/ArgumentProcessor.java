package de.ozml.hsqldiffextract;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class manages the arguments which are passed through the command line
 * and offers mnethods to access the values.
 */
public class ArgumentProcessor {

	private static final String ARG_PREFIX = "-";
	private static final String ARGS_SUFFIX = "=";
	private static final String ARG_PATTERN = ARG_PREFIX +  "(\\w+)(" + ARGS_SUFFIX + "(.*))?";

	private Map<String, String> arguments;

	/**
	 * Builds a {@link ArgumentProcessor} instance and passes the parsed argument values
	 * from the argument array to it.
	 * @param args argument array
	 * @return
	 */
	public static ArgumentProcessor build(String[] args){
		Map<String, String> result = new HashMap<>();

		// Build argument map
		Pattern argPattern = Pattern.compile(ARG_PATTERN);
		for (String arg : args) {
			if(arg.matches(ARG_PATTERN)){
				Matcher matcher = argPattern.matcher(arg);
				if(!matcher.find()){
					continue;
				}

				String argDef = matcher.group(1);
				Argument argEnum = Argument.getArgument(argDef);
				if(argEnum == null){
					continue;
				}

				String argValue = argEnum.isFlag() ? argDef : matcher.group(3);

				result.put(argDef, argValue);
			}
		}

		// If interactive mode is requested the mandatory arguments must not be
		// present beforehand, hence are removed from map.
		if(result.containsKey(Argument.Interactive.getDefinition())){
			Argument.getRequiredList().forEach(arg -> result.remove(arg.getDefinition()));
		}

		return new ArgumentProcessor(result);
	}

	/**
	 * Appends the value with the global argument prefix.
	 * @param value
	 * @return
	 */
	public static String addPrefix(String value){
		return ARG_PREFIX + value;
	}

	private ArgumentProcessor(Map<String, String> arguments){
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
		return collectArg(Argument.Lazy.getDefinition()) != null;
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
		String msg = "\nPlease add path to the original file:";
		String errMsg = "\nFile does not exist..";
		String argDef = Argument.OriginalFile.getDefinition();

		return collectArg(argDef, line -> fileExists(line), msg, errMsg);
	}

	/**
	 * Returns the value for the changed file. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getChangedFile(){
		String msg = "\nPlease add path to the changed file:";
		String errMsg = "\nFile does not exist..";
		String argDef = Argument.ChangedFile.getDefinition();

		return collectArg(argDef, line -> fileExists(line), msg, errMsg);
	}

	/**
	 * Returns the value for the output directory. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getOutputDirectory(){
		String msg = "\nPlease add path to the output directory:";
		String errMsg = "\nDirectory does not exist..";
		String argDef = Argument.OutputDir.getDefinition();

		return collectArg(argDef, line -> dirExists(line), msg, errMsg);
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