package de.ozml.hsqldiffextract.arg;

import static de.ozml.hsqldiffextract.arg.Argument.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides methods to build {@link ArgumentBag} instances.
 */
public class ArgumentProcessor {

	/**
	 * Builds a {@link ArgumentBag} instance and passes the parsed argument values
	 * from the argument array to it.
	 * @param args argument array
	 * @return
	 */
	public static ArgumentBag build(String[] args){
		Map<String, String> arguments = parseArguments(args);

		if(arguments.containsKey(PropertyFile.getDefinition())){
			String propertyPath = arguments.get(PropertyFile.getDefinition());
			String[] values = ArgumentLoader.loadFromPropertyFile(propertyPath);
			arguments = parseArguments(values);
		}
		else if(arguments.containsKey(Interactive.getDefinition())){
			List<String> exclude = Argument.getRequiredList()
				.stream().map(arg -> arg.getDefinition())
				.collect(Collectors.toList());
			removeAll(arguments, exclude);
		}

		return new ArgumentBag(arguments);
	}

	/**
	 * Builds a map of {@link Argument}s from the argument string array.
	 * @param args
	 * @return
	 */
	private static Map<String, String> parseArguments(String[] args){
		Map<String, String> result = new HashMap<>();
	
		Pattern argPattern = Pattern.compile(Patterns.ARG_PATTERN);
		for (String arg : args) {
			if(arg.matches(Patterns.ARG_PATTERN)){
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

		return result;
	}

	/**
	 * Removes the objects associated with the specified keys from the given map.
	 * @param map
	 * @param keys
	 */
	private static void removeAll(Map<String, String> map, Collection<String> keys) {
		for (String key : keys) {
			map.remove(key);
		}
	}

	private ArgumentProcessor(){}

}