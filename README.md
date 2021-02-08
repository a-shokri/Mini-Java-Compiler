**Authors:** Ali Shokri and Manan Joshi.

We have implemented the name analyzer part of the Semantic phase of the Compiler.

We have also included a build file that can build the project using ant.

The jar file can then be executed using the java command.

Sample usage command: java -jar Compiler <options> <filenames>

                      => The options flag allows to provide stage option to the program. At this stage the options
                      available are --lex, --ast, --name, --pp and --help.
                      => The filenames attribute allows to specify an array of files to be parsed separated by spaces.
                      However, only those files will be parsed that have an .emj file extension.
                      
You may find a complete grammar of Mini-Java from [here](https://www.cambridge.org/resources/052182060X/MCIIJ2e/grammar.htm)                      
