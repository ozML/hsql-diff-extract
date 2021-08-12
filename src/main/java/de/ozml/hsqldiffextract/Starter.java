package de.ozml.hsqldiffextract;

import java.io.File;

import de.ozml.hsqldiffextract.arg.ArgumentBag;
import de.ozml.hsqldiffextract.arg.ArgumentProcessor;
import de.ozml.hsqldiffextract.res.Res;

/**
 * Entry point of the program
 */
public class Starter {

	public static void main(String[] args){

		// Check for command
		if(args != null && args.length > 0 && args[0].startsWith(CommandProcessor.COMMAND_PREFIX)) {
			System.out.println(CommandProcessor.executeCommand(args[0]));

			return;
		}

		// Collect arguments
		ArgumentBag argProcessor = ArgumentProcessor.build(args);

		// Print help if necessary
		if(!argProcessor.claimsMet()) {
			System.out.println(Res.loadString("msg.introtext"));

			return;
		}

		// Original file
		System.out.println("\n" + Res.loadString("msg.readofilepath"));
		String originalFile = argProcessor.getOriginalFile();
		if(!argProcessor.isInteractive()){
			System.out.println(originalFile);
			if(!new File(originalFile).exists()){
				System.out.println(Res.loadString("msg.filenonexist"));
				return;
			}
		}

		// Changed file
		System.out.println("\n" + Res.loadString("msg.readcfilepath"));
		String changedFile = argProcessor.getChangedFile();
		if(!argProcessor.isInteractive()){
			System.out.println(changedFile);
			if(!new File(changedFile).exists()){
				System.out.println(Res.loadString("msg.filenonexist"));
				return;
			}
		}

		// Output directory
		System.out.println("\n" + Res.loadString("msg.readodirpath"));
		String outputDir = argProcessor.getOutputDirectory();
		if(!argProcessor.isInteractive()){
			System.out.println(outputDir);
			if(!new File(outputDir).exists()){
				System.out.println(Res.loadString("msg.dirnonexist"));
				return;
			}
		}

		// Configure workflow
		Workflow workflow = new Workflow(originalFile, changedFile, outputDir, argProcessor.isLazyMode());
		workflow.setInclusionFilter(argProcessor.getIncludeTables());
		workflow.setExclusionFilter(argProcessor.getExcludeTables());
		workflow.start();
	}

}