HSQLDiffExtract
===============


HSQLDiffExtract is a command line parsing tool which provides functions to extract changes in the table contents 
of two hsqldb script files. Therefore the content of the script files is parsed and compared. The process is performed offline without database connection. The changes 
are exported as corresponding sql statements for inserts, updates and deletes. The tool only looks for
changes in the row states, not in the table definitions.

**Basic Usage:**  
java -jar hsql-diff-extract-x.x.x.jar `-oFile=<original file>` `-cFile=<changed file>` `-oDir=<output directory>`

Use `@arg` for a list of all arguments, and `@arg:<argument>` for further info.  
*Example: java -jar hsql-diff-extract-x.x.x.jar @arg*
