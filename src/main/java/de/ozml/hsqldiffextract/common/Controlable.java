package de.ozml.hsqldiffextract.common;

/**
 * Represents an interface which provides control methods. 
 */
public interface Controlable {
	
	public void start();
	public void stop();
	public void pause();

}