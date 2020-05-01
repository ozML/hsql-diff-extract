package de.ozml.hsqldiffextract;

import java.io.File;

/**
 * Entry point of the program
 */
public class Starter {

	public static void main(String[] args){

		// Collect arguments
		ArgumentProcessor argProcessor = ArgumentProcessor.build(args);
		
		// Print help if necessary
		if(!argProcessor.claimsMet()) {
			System.out.println(
				"HSQLDiffExtract is a help utility to determine changes between two HSQLDB database script files." +
				"\nPass in the file with the original state and the file with the changes." +
				"\nThe tool will parse the files and write sql statements for the changes into output files." +
				"\nIt is compatible with HSQLDB 2.2.9 memory tables." +
				"\n\n" +
				"Arguments:\nUsage -> -ARG=VALUE\n\n" +
				"-oFile (original file)*: Path to original script file\n" +
				"-cFile (changed file)*: Path to changed script file\n" +
				"-oDir (output directory)*: Path to output directory\n" +
				"\n" +
				"Flags:\nUsage -> -FLAG\n\n" +
				"-interactive (interactive mode): Enabling interactive mode does ignore the other arguments.\n" +
				"-lazy (lazy mode): Only a part of the rows are cached at a time, but speed is decreased.\n" +
				"\n\n* Mandatory in noninteractive mode" +
				"\n"
			);

			return;
		}

		// Original file
		System.out.println("\nReading original file path");
		String originalFile = argProcessor.getOriginalFile();
		if(!argProcessor.isInteractive()){
			System.out.println(originalFile);
			if(!new File(originalFile).exists()){
				System.out.println("File does not exist");
				return;
			}
		}

		// Changed file
		System.out.println("\nReading changed file path");
		String changedFile = argProcessor.getChangedFile();
		if(!argProcessor.isInteractive()){
			System.out.println(changedFile);
			if(!new File(changedFile).exists()){
				System.out.println("File does not exist");
				return;
			}
		}

		// Output directory
		System.out.println("\nReading path to ouput directory");
		String outputDir = argProcessor.getOutputDirectory();
		if(!argProcessor.isInteractive()){
			System.out.println(outputDir);
			if(!new File(outputDir).exists()){
				System.out.println("Directory does not exist");
				return;
			}
		}

		Workflow workflow = new Workflow(originalFile, changedFile, outputDir, argProcessor.isLazyMode());
		workflow.start();
	}

}