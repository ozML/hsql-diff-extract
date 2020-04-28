HSQLDiffExtract
===============


HSQLDiffExtract is a command line tool which provides functions to extract changes in the table contents 
of two hsqldb script files. Therefore the content of the script files is parsed and compared. The changes 
are exported as corresponding sql statements for inserts, updates and deletes. The tool only looks for
changes in the row states, not in the table definitions per se.


**Arguments:**

Execute the tool with the following arguments.  
Usage: ARG=VALUE


-oFile (original file)`*`:  
*Path to original script file*

-cFile (changed file)`*`:  
*Path to changed script file*

-oDir (output directory)`*`:  
*Path to output directory*

**Flags:**

-interactive (interactive mode):  
*Enabling interactive ignores some arguments (see`*`)*


`*` Only mandatory in noninteractive mode
