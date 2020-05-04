package de.ozml.hsqldiffextract.arg;

public class Patterns {
	
	static final String ARG_PREFIX = "-";
	static final String ARG_SUFFIX = "=";
	static final String ARG_PATTERN = ARG_PREFIX +  "(\\w+)(" + Patterns.ARG_SUFFIX + "(.*))?";

	private Patterns(){}

}