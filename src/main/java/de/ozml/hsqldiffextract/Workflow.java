package de.ozml.hsqldiffextract;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.ozml.hsqldiffextract.entity.Table;
import de.ozml.hsqldiffextract.parser.RowParser;
import de.ozml.hsqldiffextract.parser.TableParser;
import de.ozml.hsqldiffextract.util.EagerRowSource;
import de.ozml.hsqldiffextract.util.LazyRowRource;
import de.ozml.hsqldiffextract.util.RowSource;

/**
 * Represents the inner workflow of the program.
 */
public class Workflow {

	private static final String TABLE_OUTPUT_FILE = "00-ReadTables.txt";

	private String originalFile;
	private String changedFile;
	private String outputDir;
	private boolean isLazyMode;

	public Workflow(String originalFile, String changedFile, String outputDir, boolean isLazyMode) {
		this.originalFile = originalFile;
		this.changedFile = changedFile;
		this.outputDir = outputDir;
		this.isLazyMode = isLazyMode;
	}
	
	public void start(){
		// Start
		System.out.println("\nStarting workflow");

		// Read table definitions
		System.out.println("\nCollecting tables");
		System.out.println("Definitions written to: " + outputDir + "\\" + TABLE_OUTPUT_FILE);

		System.out.println("\nCollecting tables from original file");
		List<Table> oTables = TableParser.readTablesFromFile(originalFile);
		System.out.println(oTables.size() + " tables read");
		printTables("*\n* Original tables:\n*", oTables, false);

		System.out.println("\nCollecting tables from changed file");
		List<Table> cTables = TableParser.readTablesFromFile(changedFile);
		System.out.println(cTables.size() + " tables read");
		printTables("*\n* Changed tables:\n*", oTables, true);

		// Process changes
		System.out.println("\nDetermine changes");
		for (Table oTable : oTables) {
			for (Table cTable : cTables) {
				if(oTable.getName().equals(cTable.getName())){
					System.out.println("\nProcessing table " + oTable.getName());

					// Read rows
					RowSource oTableSource = buildRowSource(oTable, originalFile);
					RowSource cTableSource = buildRowSource(cTable, changedFile);
					System.out.println("Rows: original=" + oTableSource.count() +", changed=" + cTableSource.count());
					
					if(oTableSource.count() > 0 && cTableSource.count() > 0){
						DiffProcessor diffProcessor = new DiffProcessor(oTable.getName(), outputDir);
						diffProcessor.process(oTableSource, cTableSource);
						System.out.println("Done");
					} else {
						System.out.println("Skipped");
					}
				}
			}
		}

		// End
		System.out.println("\nWorkflow completed");
	}

	private RowSource buildRowSource(Table table, String filePath){
		if(isLazyMode){
			return new LazyRowRource(table, filePath, RowParser.readRowLinesFromTable(table, filePath));
		} else {
			return new EagerRowSource(table, RowParser.readRowsFromTable(table, filePath));
		}
	}

	private void printTables(String headLine, List<Table> tables, boolean append){
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(outputDir + "\\" + TABLE_OUTPUT_FILE, append));
			writer.newLine();
			writer.write(headLine);
			writer.newLine();
			writer.newLine();

			for(int i = 0; i < tables.size(); i++){
				Table table = tables.get(i);
				writer.write("Table " + table.getName() + " (\n");

				for(int j = 0; j < table.getColumns().length; j++){
					writer.write("  " + table.getColumns()[j].toString());
					if(j != table.getColumns().length - 1){
						writer.newLine();
					}
				}

				writer.write("\n)\n");
				writer.newLine();
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{if(writer != null) writer.close();} catch(Exception e){}
		}
	}

}