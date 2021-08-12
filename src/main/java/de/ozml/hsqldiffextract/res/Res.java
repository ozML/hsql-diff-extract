package de.ozml.hsqldiffextract.res;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import de.ozml.hsqldiffextract.arg.Argument;

/**
 * Resource loader class. It provides methods to load different resources.
 */
public class Res {

	private static final String BUNDLE_NAME = "bundle";

	private static final String INFO_PREFIX = "arginfo.";
	private static final String INFO_TITLE_KEY = "title";
	private static final String INFO_FORMAT_KEY = "format";
	private static final String INFO_DESCRIPTION_KEY = "description";

	/**
	 * Loads all available {@link Argument} infos.
	 * @return
	 */
	public static List<ArgumentInfo> loadArgumentInfos() {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
		List<ArgumentInfo> result = new ArrayList<>();
		for (Argument arg : Argument.values()) {
			ArgumentInfo info = loadArgumentInfo(arg.getDefinition(), bundle);
			if(info != null){
				result.add(info);
			}
		}

		return result;
	}

	/**
	 * Loads the info for the {@link Argument} with the provided name.
	 * @param name
	 * @return
	 */
	public static ArgumentInfo loadArgumentInfo(String name) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
		return loadArgumentInfo(name, bundle);
	}

	/**
	 * Loads the info for the {@link Argument} with the provided name.
	 * @param name
	 * @param bundle
	 * @return
	 */
	private static ArgumentInfo loadArgumentInfo(String name, ResourceBundle bundle) {
		ArgumentInfo info = null;
		String title = loadString(INFO_PREFIX + name + "_" + INFO_TITLE_KEY, bundle);
		if(title != null){
			info = new ArgumentInfo(
				name,
				title,
				loadString(INFO_PREFIX + name + "_" + INFO_FORMAT_KEY, bundle),
				loadString(INFO_PREFIX + name + "_" + INFO_DESCRIPTION_KEY, bundle)
			);
		}

		return info;
	}

	/**
	 * Tries to read the resource string with the specified key.
	 * @param key
	 * @return
	 */
	public static String loadString(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
		return loadString(key, bundle);
	}

	/**
	 * Tries to read the resource string with the specified key from the provided bundle.
	 * If no resource by the key is found, {@code null} is returned.
	 * @param key
	 * @param bundle
	 * @return
	 */
	private static String loadString(String key, ResourceBundle bundle) {
		String value = null;
		try {
			value = bundle.getString(key);
		} catch(Exception e){}

		return value;
	}

	private Res() {}

}