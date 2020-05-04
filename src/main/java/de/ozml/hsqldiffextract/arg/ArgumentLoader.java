package de.ozml.hsqldiffextract.arg;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static de.ozml.hsqldiffextract.arg.Patterns.ARG_PREFIX;
import static de.ozml.hsqldiffextract.arg.Patterns.ARG_SUFFIX;;

public class ArgumentLoader {

	/**
	 * Loads the values from the property file into a string array.
	 * @param filePath
	 * @return
	 */
	public static String[] loadFromPropertyFile(String filePath) {
		String[] result = null;
		try {
			List<String> args = new ArrayList<>();
			Properties p = new Properties();
			p.load(new FileInputStream(filePath));
			
			for (Argument arg : Argument.values()) {
				String propertyKey = ARG_PREFIX + arg.getDefinition();
				if(p.containsKey(propertyKey) && !isProhibited(arg)){
					if(arg.isFlag()){
						args.add(propertyKey);
					}
					else{
						args.add(propertyKey + ARG_SUFFIX + p.getProperty(propertyKey));
					}
				}
			}
			result = args.toArray(new String[args.size()]);
		} catch (IOException e) {}

		return result;
	}

	/**
	 * Checks whether the {@link Argument} is permitted in the property file.
	 * @param arg
	 * @return
	 */
	private static boolean isProhibited(Argument arg){
		return arg == Argument.PropertyFile || arg == Argument.Interactive;
	}

	private ArgumentLoader(){}

}