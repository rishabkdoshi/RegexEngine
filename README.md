This project implements a Regular expression engine parser.It takes in a regular expression as input and finds the corresponding epsilon-NFA using Thompsons Construction Algorithm.The obtained epsilon-NFA is then converted to DFA using the subset construction algorithm.

Once the DFA is generated ,the input string is input to the DFA and traversed to check if the reached state is an accepted state or not.

The graphical representation of the NFA and DFA is generated and will be present on the Desktop in the Graphs folder.