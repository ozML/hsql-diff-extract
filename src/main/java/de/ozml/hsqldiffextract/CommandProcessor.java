package de.ozml.hsqldiffextract;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ozml.hsqldiffextract.res.ArgumentInfo;
import de.ozml.hsqldiffextract.res.Res;

/**
 * Provides functions to execute command line functions.
 */
public class CommandProcessor {
	
	public static final String COMMAND_PREFIX = "@";
	public static final String COMMAND_ARG = "arg";

	/**
	 * Tries to execute the command string.
	 * @param commandString
	 * @return
	 */
	public static String executeCommand(String commandString) {
		String commandPattern = "^" + COMMAND_PREFIX + "(\\w+)(:(.*))?";
		if(commandString != null && commandString.matches(commandPattern)){
			Pattern pattern = Pattern.compile(commandPattern);
			Matcher matcher = pattern.matcher(commandString);

			if(matcher.find()){
				String name = matcher.group(1);
				String param = matcher.group(3);
				Function<String, String> command = getCommand(name);

				if(command != null){
					return command.apply(param);
				}
			}

			return Res.loadString("command.unknown");
		}

		return Res.loadString("command.invalid");
	}

	/**
	 * Returns the command with the provided name.
	 * @param name
	 * @return
	 */
	private static Function<String, String> getCommand(String name) {
		if(name.equals(COMMAND_ARG)){
			return param -> executeArgCommand(param);
		}

		return null;
	}

	/**
	 * Executes the @arg command.
	 * @param param
	 * @return
	 */
	private static String executeArgCommand(String param) {
		String formatString = Res.loadString("command.arg.format.info");
		if(param != null && !param.isEmpty()){
			ArgumentInfo info = Res.loadArgumentInfo(param);
			if(info != null){
				return String.format(formatString, info.getTitle(), info.getFormat(), info.getDescription());
			}
		}

		String shortFormatString = Res.loadString("command.arg.format.infoshort");
		String defaultText = Res.loadString("command.arg.defaulttext") + "\n";
		for (ArgumentInfo argInfo : Res.loadArgumentInfos()) {
			defaultText += "\n" + String.format(shortFormatString, argInfo.getKey(), argInfo.getTitle());
		}

		return defaultText;
	}

	private CommandProcessor() {}

}