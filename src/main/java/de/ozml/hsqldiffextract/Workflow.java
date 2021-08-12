package de.ozml.hsqldiffextract;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.ozml.hsqldiffextract.common.EagerRowSource;
import de.ozml.hsqldiffextract.common.LazyRowRource;
import de.ozml.hsqldiffextract.common.RowSource;
import de.ozml.hsqldiffextract.entity.Table;
import de.ozml.hsqldiffextract.parser.RowParser;
import de.ozml.hsqldiffextract.parser.TableParser;
import de.ozml.hsqldiffextract.res.Res;

/**
 * Represents the inner workflow of the program.
 */
public class Workflow {

	private static final String TABLE_OUTPUT_FILE = "00-ReadTables.txt";

	private String originalFile;
	private String changedFile;
	private String outputDir;
	private boolean isLazyMode;
	private List<String> inclusionFilter;
	private List<String> exclusionFilter;

	public Workflow(String originalFile, String changedFile, String outputDir, boolean isLazyMode) {
		this.originalFile = originalFile;
		this.changedFile = changedFile;
		this.outputDir = outputDir;
		this.isLazyMode = isLazyMode;
	}

	public List<String> getInclusionFilter() {
		return inclusionFilter;
	}

	public void setInclusionFilter(List<String> inclusionFilter) {
		this.inclusionFilter = inclusionFilter;
	}

	public List<String> getExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(List<String> exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}

	public void start() {
		// Start
		System.out.println("\n" + Res.loadString("msg.startworkflow"));

		// Read table definitions
		System.out.println("\n" + Res.loadString("msg.collecttables"));
		System.out.println(String.format(Res.loadString("msg.format.defswriteto"), outputDir + "\\" + TABLE_OUTPUT_FILE));

		System.out.println("\n" + String.format(Res.loadString("msg.format.collecttablesfrom"), Res.loadString("ofile")));
		List<Table> oTables = TableParser.readTablesFromFile(originalFile);
		System.out.println(String.format(Res.loadString("msg.format.tablesread"), "" + oTables.size()));
		
		System.out.println("\n" + String.format(Res.loadString("msg.format.collecttablesfrom"), Res.loadString("cfile")));
		List<Table> cTables = TableParser.readTablesFromFile(changedFile);
		System.out.println(String.format(Res.loadString("msg.format.tablesread"), "" + cTables.size()));
		
		// Apply filters
		if(inclusionFilter != null || (inclusionFilter != null && exclusionFilter != null)){
			System.out.println("\n" + Res.loadString("msg.applyinfilter"));
			applyInclusionFilter(oTables);
			applyInclusionFilter(cTables);
		} else if(exclusionFilter != null) {
			System.out.println("\n" + Res.loadString("msg.applyexfilter"));
			applyExclusionFilter(oTables);
			applyExclusionFilter(cTables);
		}
		
		printTables("*\n* " + Res.loadString("msg.originaltables") + ":\n*", oTables, false);
		printTables("*\n* " + Res.loadString("msg.changedtables") + ":\n*", cTables, true);

		// Process changes
		System.out.println("\n" + Res.loadString("msg.determinchanges"));
		for (Table oTable : oTables) {
			for (Table cTable : cTables) {
				if(oTable.getName().equals(cTable.getName())){
					System.out.println("\n" + String.format(Res.loadString("msg.format.processtable"), oTable.getName()));

					// Read rows
					RowSource oTableSource = buildRowSource(oTable, originalFile);
					RowSource cTableSource = buildRowSource(cTable, changedFile);
					System.out.println(String.format(Res.loadString("msg.format.readrowsresult"), "" + oTableSource.count(), "" + cTableSource.count()));
					
					if(oTableSource.count() > 0 && cTableSource.count() > 0){
						DiffProcessor diffProcessor = new DiffProcessor(oTable.getName(), outputDir);
						diffProcessor.process(oTableSource, cTableSource);
						System.out.println(Res.loadString("msg.done"));
					} else {
						System.out.println(Res.loadString("msg.skipped"));
					}
				}
			}
		}

		// End
		System.out.println("\n" + Res.loadString("msg.workflowcompleted"));
	}

	/**
	 * Applies the inclusion filter by removing all tables not listed.
	 * @param tables
	 */
	private void applyInclusionFilter(List<Table> tables) {
		tables.removeIf(table -> !inclusionFilter.contains(table.getName().toLowerCase()));
	}

	/**
	 * Applies the exclusion filter by removing all listed tables listed.
	 * @param tables
	 */
	private void applyExclusionFilter(List<Table> tables) {
		tables.removeIf(table -> exclusionFilter.contains(table.getName().toLowerCase()));
	}

	/**
	 * Returns a suitable row source.
	 * @param table
	 * @param filePath
	 * @return
	 */
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