package de.ozml.hsqldiffextract.arg;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents possible argument values passed to the application.
 */
public enum Argument {

	OriginalFile("oFile", true),
	ChangedFile("cFile", true),
	OutputDir("oDir", true),
	PropertyFile("pFile", false),
	IncludeTables("inTables", false),
	ExcludeTables("exTables", false),
	Interactive("interactive"),
	Lazy("lazy");

	private String definition;
	private boolean isFlag;
	private boolean isRequired;

	/**
	 * Returns all required arguments.
	 * @return
	 */
	public static List<Argument> getRequiredList() {
		List<Argument> result = new ArrayList<>();
		for (Argument argument : Argument.values()) {
			if(argument.isRequired()){
				result.add(argument);
			}
		}

		return result;
	}

	/**
	 * Returns the argument with the specified definition.
	 * @param definition
	 * @return
	 */
	public static Argument getArgument(String definition) {
		for (Argument argument : Argument.values()) {
			if(argument.definition.equals(definition)){
				return argument;
			}
		}

		return null;
	}

	private Argument(String definition, boolean isFlag, boolean isRequired) {
		this.definition = definition;
		this.isFlag = isFlag;
		this.isRequired = isRequired;
	}

	private Argument(String definition, boolean isRequired) {
		this(definition, false, isRequired);
	}

	private Argument(String definition){
		this(definition, true, false);
	}

	/**
	 * Returns the enum definition string.
	 * @return
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Checks whether the argument is a flag.
	 * @return
	 */
	public boolean isFlag() {
		return isFlag;
	}

	/**
	 * Checks whether the argument is interactive.
	 * @return
	 */
	public boolean isRequired() {
		return isRequired;
	}

	@Override
	public String toString() {
		return definition;
	}

}