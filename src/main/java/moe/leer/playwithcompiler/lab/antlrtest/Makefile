all:
	antlr4 HelloScript.g4
	javac *.java
	grun HelloScript stmt -gui

run:
	grun HelloScript stmt -gui

clean:
	/usr/bin/rm -f *.java *.class *.interp *.tokens
