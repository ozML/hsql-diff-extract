package de.ozml.hsqldiffextract;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class manages the arguments which are passed through the command line
 * and offers mnethods to access the values.
 */
public class ArgumentBag {

	private static final String ARG_PREFIX = "-";
	private static final String ARGS_SUFFIX = "=";
	
	private static final String ARG_O_FILE = "oFile";
	private static final String ARG_C_FILE = "cFile";
	private static final String ARG_O_DIR = "oDir";
	private static final String ARG_INTERACTIVE = "interactive";
	private static final String ARG_LAZY = "lazy";

	private static final String ARG_PATTERN = ARG_PREFIX +  "(\\w+)(" + ARGS_SUFFIX + "(.*))?"; 

	private Map<String, String> arguments;
	private boolean isInteractive;
	private boolean isLazyMode;

	/**
	 * Builds a {@link ArgumentBag} instance and passes the parsed argument values
	 * from the argument array to it.
	 * @param args
	 * @return
	 */
	public static ArgumentBag build(String[] args){
		Map<String, String> result = new HashMap<>();
		boolean isInteractive = false;
		boolean isLazyMode = false;

		Pattern pattern = Pattern.compile(ARG_PATTERN);
		for (String arg : args) {
			if(!arg.matches(ARG_PATTERN)){
				continue;
			}

			Matcher matcher = pattern.matcher(arg);
			if(!matcher.find()){
				continue;
			}

			String argKey = matcher.group(1);
			if(argKey.equals(ARG_INTERACTIVE)) {
				isInteractive = true;
			} else if(argKey.equals(ARG_LAZY)){
				isLazyMode = true;
			} else {
				String argValue = matcher.group(3);result.put(argKey, argValue);
				result.put(argKey, argValue);
			}
		}

		return new ArgumentBag(result, isInteractive, isLazyMode);
	}

	/**
	 * Appends the value with the global argument prefix.
	 * @param value
	 * @return
	 */
	public static String addPrefix(String value){
		return ARG_PREFIX + value;
	}

	private ArgumentBag(Map<String, String> arguments, boolean isInteractive, boolean isLazyMode){
		this.arguments = arguments;
		this.isInteractive = isInteractive;
		this.isLazyMode = isLazyMode;
	}

	public boolean isInteractive() {
		return isInteractive;
	}

	public boolean isLazyMode(){
		return isLazyMode;
	}

	/**
	 * Returns the number of arguments contained by this instance.
	 * @return
	 */
	public int size(){
		return arguments.size();
	}

	/**
	 * Returns the value for the original file. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getOriginalFile(){
		String result = null;
		if(!isInteractive){

			return arguments.get(ARG_O_FILE);
		} else {
			Scanner scanner = new Scanner(System.in);
			while(true){
				System.out.println("\nPlease add path to the original file:");
				String line = scanner.nextLine().trim();

				if(!line.isEmpty() && new File(line).isFile()){
					result = line;
					break;
				}
				else{
					System.out.println("\nFile does not exist..");
				}
			}
		}

		return result;
	}

	/**
	 * Returns the value for the changed file. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getChangedFile(){
		String result = null;
		if(!isInteractive){
			return arguments.get(ARG_C_FILE);
		} else {
			Scanner scanner = new Scanner(System.in);
			while(true){
				System.out.println("\nPlease add path to the changed file:");
				String line = scanner.nextLine().trim();

				if(!line.isEmpty() && new File(line).isFile()){
					result = line;
					break;
				}
				else{
					System.out.println("\nFile does not exist..");
				}
			}
		}

		return result;
	}

	/**
	 * Returns the value for the output directory. If the interactive mode is activated
	 * the calue is acquired through user input.
	 * @return
	 */
	public String getOutputDirectory(){
		String result = null;
		if(!isInteractive){
			return arguments.get(ARG_O_DIR);
		} else {
			Scanner scanner = new Scanner(System.in);
			while(true){
				System.out.println("\nPlease add path to the output directory:");
				String line = scanner.nextLine().trim();

				if(!line.isEmpty() && new File(line).isDirectory()){
					result = line;
					break;
				}
				else{
					System.out.println("\nDirectory does not exist..");
				}
			}
		}

		return result;
	}

}