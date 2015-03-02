package nfa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;





import nfa.State.Transitions;

/**
 * This class implements all the operations used in the Regex Engine. The
 * operations include star,concatenation,union,epsilonClosure and so on
 * */
public class Operations {

	/* The Stack used to perform the operations */
	Stack<Table> operandStack;

	Stack<Character> operator = new Stack<>();

	/*
	 * The set of characters used in the regular expression(Basically language
	 * L)
	 */
	List<Character> ipSet;

	/* State Id of the State */
	int StateID = 0;

	/* Constructor */
	public Operations() {
		operandStack = new Stack<>();
		ipSet = new ArrayList<Character>();
	}

	/**
	 * PUSH symbol ip on the stack, the operation would create two state objects
	 * on the heap and create a transition object on symbol a from state 1 to
	 * state 2.
	 * 
	 * @param ip
	 *            the ip character to be pushed
	 * */
	private void pushChar(char ip) {
		State s0 = new State(++StateID);
		State s1 = new State(++StateID);

		s0.addTransition(ip, s1);

		Table nfaTable = new Table();

		nfaTable.push(s0);
		nfaTable.push(s1);
		nfaTable.setStartState(s0);
		nfaTable.setLastState(s1);

		operandStack.push(nfaTable);

		ipSet.add(ip);
	}

	/**
	 * Concatenates the top two NFA Objects present in the stack
	 * 
	 * @return true if it is able to concatenate,false otherwise
	 * */
	private boolean concat() {

		Table A, B;

		B = operandStack.pop();
		A = operandStack.pop();

		if (B == null || A == null)
			return false;

		State s = A.getLastState();
		s.addTransition('0', B.getStartState());

		if (A.getLastState() != null)
			A.getLastState().setFinalState(false);
		A.setLastState(B.getLastState());

		for (State s0 : B.getsList())
			A.push(s0);

		operandStack.push(A);

		return true;
	}

	/**
	 * Performs the star operation on the first object on the operand stack
	 * 
	 * @return true if it is able to star,false otherwise
	 */
	private boolean star() {
		Table A;

		A = operandStack.pop();

		if (A == null)
			return false;

		State startState = new State(++StateID);
		State endState = new State(++StateID);

		startState.addTransition('0', endState);

		startState.addTransition('0', A.getStartState());

		A.getLastState().addTransition('0', endState);

		A.getLastState().addTransition('0', A.getStartState());

		A.push(endState);

		A.getsList().add(0, startState);

		A.setStartState(startState);

		if (A.getLastState() != null)
			A.getLastState().setFinalState(false);
		A.setLastState(endState);

		operandStack.push(A);

		return true;
	}

	/**
	 * Performs the union operation on the first two objects on the operand
	 * stack
	 * 
	 * @return true if it is able to unite,false otherwise
	 * */
	private boolean union() {
		Table A, B;

		B = operandStack.pop();
		A = operandStack.pop();

		if (B == null || A == null)
			return false;

		State startState = new State(++StateID);
		State endState = new State(++StateID);

		startState.addTransition('0', A.getStartState());
		startState.addTransition('0', B.getStartState());
		A.getLastState().addTransition('0', endState);
		B.getLastState().addTransition('0', endState);

		A.setStartState(startState);

		if (A.getLastState() != null)
			A.getLastState().setFinalState(false);
		A.setLastState(endState);

		A.getsList().add(0, startState);

		B.getsList().add(endState);

		for (State s : B.getsList())
			A.getsList().add(s);

		operandStack.push(A);

		return true;
	}

	/**
	 * Creates a NFA for the given regular expression in postFix form
	 * 
	 * @param regex
	 *            the regular expression in postfix form
	 * @return nfaTable
	 * */
	public Table createNFA(String regex) {

		for (int i = 0; i < regex.length(); ++i) {
			char c = regex.charAt(i);

			if ((c != '|') && (c != '*') && (c != '&')) {
				pushChar(c);
			} else {
				if (c == '|')
					union();

				if (c == '*')
					star();

				if (c == '&')
					concat();
			}
		}

		Table tbl = operandStack.pop();

		return tbl;

	}

	/**
	 * Creates the epsilon closure of states present in the input table
	 * 
	 * @param tbl
	 *            the input table containing the states
	 * @return States in epsilon Closure
	 * */
	private ArrayList<State> epsilonClosure(Table tbl) {

		Stack<State> tblStk = new Stack<State>();
		ArrayList<State> result = new ArrayList<State>();

		for (State s : tbl.getsList()) {
			tblStk.push(s);
		}

		result = tbl.getsList();

		while (!tblStk.isEmpty()) {
			State s = tblStk.pop();

			if (s == null)
				continue;
			for (int i = 0; i < s.getDirection().size(); i++) {

				if ((s.getDirection().get(i).ipChar == '0')
						&& (!result.contains(s.getDirection().get(i).nextState))) {
					result.add(s.getDirection().get(i).nextState);
					tblStk.push(s.getDirection().get(i).nextState);
				}
			}
		}

		return result;

	}

	/**
	 * Creates a DFA table out of the given nfa table
	 * 
	 * @param nfaTbl
	 *            The NFA table to be converted
	 * 
	 * @return the obtained DFATable
	 * */
	public Table convertNfaToDfa(Table nfaTbl) {

		if (nfaTbl.getsList().size() == 0)
			return null;

		int stateId = 0;

		ArrayList<State> currentStateSet = new ArrayList<State>();

		ArrayList<ArrayList<State>> setOfUnmarkedStateSets = new ArrayList<ArrayList<State>>();

		/* To get epsilon closure of first set */
		Table temp = new Table();
		temp.getsList().add(nfaTbl.getStartState());
		currentStateSet = epsilonClosure(temp);

		State DFAStartState = new State(++stateId);
		setOfUnmarkedStateSets.add(currentStateSet);

		HashMap<String, State> dfa = new HashMap<String, State>();

		dfa.put(generateUniqueKey(currentStateSet), DFAStartState);
		Table dfaTable = new Table();
		int i = 0;

		dfaTable.setStartState(DFAStartState);
		while (i < setOfUnmarkedStateSets.size()) {

			currentStateSet = setOfUnmarkedStateSets.get(i);
			i++;
			Table currentTable = new Table();
			currentTable.setsList(currentStateSet);
			State currentState = dfa.get(generateUniqueKey(currentStateSet));

			dfaTable.getsList().add(currentState);

			dfa.put(generateUniqueKey(currentStateSet), currentState);

			for (char c : ipSet) {
				ArrayList<State> nextStateSet = move(c,
						epsilonClosure(currentTable));

				ArrayList<State> removalStateSet = new ArrayList<State>();
				for (State s : nextStateSet) {
					if (s == null)
						removalStateSet.add(s);
				}
				nextStateSet.removeAll(removalStateSet);
				String key = generateUniqueKey(nextStateSet);

				State nextState;

				for (State s : currentStateSet) {
					if (s.isFinalState()) {
						currentState.setFinalState(true);
					}
				}
				/**
				 * check if this set of states exists as a unique state in the
				 * DFA table, if it exists just add transition from current
				 * state otherwise create new State and the set of States to the
				 * unmarkedSet
				 *
				 */
				if (dfa.containsKey(key)) {
					nextState = dfa.get(key);
				} else {
					nextState = new State(++stateId);

					for (State s : nextStateSet) {
						if (s.isFinalState())
							nextState.setFinalState(true);
					}

					dfa.put(key, nextState);
					setOfUnmarkedStateSets.add(nextStateSet);
				}
				currentState.addTransition(c, nextState);
			}

		}
		return dfaTable;
	}

	/**
	 * Generates a unique key for the given set of states
	 * 
	 * @param stateList
	 *            the list of states
	 * @return key the unique key for the given set of states
	 * */
	private String generateUniqueKey(ArrayList<State> u) {
		// TODO Auto-generated method stub
		Collections.sort(u);
		StringBuilder sb = new StringBuilder();
		for (State s : u) {
			sb.append(s.getStateId());
		}
		return sb.toString();
	}

	/**
	 * Returns the set of states that can be reached from the given set of
	 * states for the given input
	 * 
	 * @param character
	 *            c the character that is input
	 * @param StateSet
	 *            the set of states that is given
	 * 
	 * @return StateList the list of states that can be reached
	 * */
	private ArrayList<State> move(char c, ArrayList<State> StateSet) {
		// TODO Auto-generated method stub

		ArrayList<State> reachable = new ArrayList<State>();

		for (State s : StateSet) {
			if (s == null)
				continue;
			reachable.add(s.getNextState(c));
		}

		return reachable;
	}

	/**
	 * This displays the table given as input
	 * 
	 * @param tbl
	 *            the table to be displayed
	 * */
	public void displayTable(Table tbl) {
		State s = tbl.getStartState();

		System.out.println("------------------------------------");

		System.out.println("START STATE: " + s.getStateId());

		int i = 0;
		System.out.println("LIST OF ACCEPTING STATES");
		for (State s0 : tbl.getsList()) {
			if (s0.isFinalState()) {
				i++;
				System.out.println(i + ") " + s0.getStateId());
			}
		}

		i = 0;
		System.out.println("LIST OF TRAP STATES");
		for (State s1 : tbl.getsList()) {
			if (s1.isTrap()) {
				i++;
				System.out.println(i + ") " + s1.getStateId());
			}
		}
		if (i == 0) {
			System.out.println("0 trap states found");
		}

		Stack<State> unvisited = new Stack<State>();
		ArrayList<State> visited = new ArrayList<State>();

		System.out.println("TRANSITIONS");

		unvisited.push(s);

		while (!unvisited.isEmpty()) {
			State current = unvisited.pop();

			for (Transitions t : current.getDirection()) {
				System.out.println(current.getStateId() + "---" + t.ipChar
						+ "--->" + t.nextState.getStateId());
				if (visited.contains(t.nextState))
					continue;
				else {
					visited.add(t.nextState);
					unvisited.push(t.nextState);
				}
			}
		}

		System.out.println("------------------------------------");
	}

	/**
	 * Checks if given string is accepted by the dfa given in the table
	 * 
	 * @param tbl
	 *            the dfa table
	 * @param str
	 *            the string to be tested
	 * */
	public boolean check(Table tbl, String str) {
		State currentState = tbl.getStartState();
		State nextState;
		for (int i = 0; i < str.length(); i++) {
			nextState = currentState.getNextState(str.charAt(i));
			currentState = nextState;

			if (nextState == null)
				return false;
		}

		return currentState.isFinalState();
	}

	void drawGraphNfa(Table tbl) throws IOException, InterruptedException {
		PrintWriter pw = new PrintWriter(new File(
				"~/Desktop/Graphs/drawNfa.R"));

		pw.println("rm(list = ls(all = T))");
		pw.println("library(Rgraphviz)");
		pw.println();

		int adjMat[][] = new int[tbl.getsList().size() + 1][tbl.getsList()
				.size() + 1];
		for (State s : tbl.getsList()) {
			for (Transitions t : s.getDirection()) {
				adjMat[s.getStateId()][t.nextState.getStateId()] = 1;
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("test.matrix<-matrix(c(");

		for (int i = 0; i < adjMat.length; i++) {
			for (int j = 0; j < adjMat.length; j++) {
				if (i == 0 || j == 0) {
					continue;
				}
				System.out.println(i + " " + j);
				sb.append(adjMat[j][i]);
				sb.append(",");
			}
		}
		sb.deleteCharAt(sb.toString().length() - 1);
		sb.append("), ncol=" + (adjMat.length - 1) + ", nrow="
				+ (adjMat.length - 1) + ")");

		pw.println(sb.toString());

		for (int i = 0; i < sb.toString().length(); i++)
			sb.deleteCharAt(i);

		StringBuilder names = new StringBuilder();

		names.append("names(test.matrix)<-c(");
		int i;
		for (i = 1; i < adjMat.length; i++) {
			names.append("\"" + i + "\"" + ", ");
		}
		names.deleteCharAt(names.toString().length() - 1);
		names.deleteCharAt(names.toString().length() - 1);
		names.append(")");

		pw.println("row" + names.toString());
		pw.println("col" + names.toString());

		pw.println("am.graph<-new(\"graphAM\", adjMat=test.matrix, edgemode=\"directed\")");
		pw.println("eatrs = list()");
		pw.println("nAttrs = list()");

		StringBuilder sb2 = new StringBuilder();

		sb2.append("eatrs$label = c(");

		for (char c : ipSet) {
			for (State s : tbl.getsList()) {
				if (s.getNextState(c) != null) {
					sb2.append("\"" + s.getStateId() + "~"
							+ s.getNextState(c).getStateId() + "\"" + "="
							+ "\"" + c + "\"" + ",");
					adjMat[s.getStateId()][s.getNextState(c).getStateId()] = -1;
				}
			}
		}
		for (int p = 0; p < adjMat.length; p++) {
			for (int j = 0; j < adjMat.length; j++) {
				if (adjMat[p][j] == 1) {
					sb2.append("\"" + p + "~" + j + "\"" + "=" + "\"" + "e"
							+ "\"" + ",");
				}
			}
		}
		sb2.deleteCharAt(sb2.toString().length() - 1);
		sb2.append(")");

		pw.println(sb2.toString());

		StringBuilder sb3 = new StringBuilder();

		sb3.append("nAttrs$shape <- c(");
		sb3.append("\"" + tbl.getStartState().getStateId() + "\"" + "="
				+ "\"plaintext\",");
		for (State s : tbl.getsList()) {
			if (s.isFinalState()) {
				sb3.append("\"" + s.getStateId() + "\"" + "=" + "\"box\",");
			}
		}
		sb3.deleteCharAt(sb3.toString().length() - 1);
		sb3.append(")");

		pw.println(sb3.toString());
		String strf = "~/Desktop/Graphs";
		
		pw.println("pdf(\"" + strf
				+ "/nfa.pdf\",width = 8,height = 8)");
		pw.println(" plot(am.graph, attrs = list(node = list(fillcolor = \"lightblue\"),edge = list(arrowsize=0.5)), nodeAttrs=nAttrs,edgeAttrs = eatrs)");

		pw.println("dev.off()");
		pw.close();
		File f2 = new File(strf+"/drawNfa.R");
		
		while(!f2.exists())
		{
			System.out.println("Not yet created");
		}
		System.out.println("Rscript "+strf+"/drawNfa.R");
		Process p = Runtime.getRuntime().exec("Rscript "+strf+"/drawNfa.R");
		p.waitFor();
		
	}

	void drawGraph(Table tbl) throws IOException, InterruptedException {
		PrintWriter pw = new PrintWriter(new File(
				  "~/Desktop/Graphs/draw.R"));

		pw.println("rm(list = ls(all = T))");
		pw.println("library(Rgraphviz)");
		pw.println();

		int adjMat[][] = new int[tbl.getsList().size() + 1][tbl.getsList()
				.size() + 1];
		for (State s : tbl.getsList()) {
			for (Transitions t : s.getDirection()) {
				adjMat[s.getStateId()][t.nextState.getStateId()] = 1;
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("test.matrix<-matrix(c(");

		for (int i = 0; i < adjMat.length; i++) {
			for (int j = 0; j < adjMat.length; j++) {
				if (i == 0 || j == 0) {
					continue;
				}
				System.out.println(i + " " + j);
				sb.append(adjMat[j][i]);
				sb.append(",");
			}
		}
		sb.deleteCharAt(sb.toString().length() - 1);
		sb.append("), ncol=" + (adjMat.length - 1) + ", nrow="
				+ (adjMat.length - 1) + ")");

		pw.println(sb.toString());

		for (int i = 0; i < sb.toString().length(); i++)
			sb.deleteCharAt(i);

		StringBuilder names = new StringBuilder();

		names.append("names(test.matrix)<-c(");
		int i;
		for (i = 1; i < adjMat.length; i++) {
			names.append("\"" + i + "\"" + ", ");
		}
		names.deleteCharAt(names.toString().length() - 1);
		names.deleteCharAt(names.toString().length() - 1);
		names.append(")");

		pw.println("row" + names.toString());
		pw.println("col" + names.toString());

		pw.println("am.graph<-new(\"graphAM\", adjMat=test.matrix, edgemode=\"directed\")");
		pw.println("eatrs = list()");
		pw.println("nAttrs = list()");

		StringBuilder sb2 = new StringBuilder();

		sb2.append("eatrs$label = c(");

		for (char c : ipSet) {
			for (State s : tbl.getsList()) {
				sb2.append("\"" + s.getStateId() + "~"
						+ s.getNextState(c).getStateId() + "\"" + "=" + "\""
						+ c + "\"" + ",");

			}
		}
		sb2.deleteCharAt(sb2.toString().length() - 1);
		sb2.append(")");

		pw.println(sb2.toString());

		StringBuilder sb3 = new StringBuilder();

		sb3.append("nAttrs$shape <- c(");
		sb3.append("\"" + tbl.getStartState().getStateId() + "\"" + "="
				+ "\"plaintext\",");
		for (State s : tbl.getsList()) {
			if (s.isFinalState()) {
				sb3.append("\"" + s.getStateId() + "\"" + "=" + "\"box\",");
			}
		}
		sb3.deleteCharAt(sb3.toString().length() - 1);
		sb3.append(")");

		pw.println(sb3.toString());
		String strf = "~/Desktop/Graphs"; 
		pw.println("pdf(\"" + strf
				+ "/dfa.pdf\",width = 8,height = 8)");
		pw.println(" plot(am.graph, attrs = list(node = list(fillcolor = \"lightblue\"),edge = list(arrowsize=0.5)), nodeAttrs=nAttrs,edgeAttrs = eatrs)");

		pw.println("dev.off()");
		pw.close();
		File f2 = new File(strf+"/draw.R");
		
		while(!f2.exists())
		{
			System.out.println("Not yet created");
		}
		Process p = Runtime.getRuntime().exec("Rscript "+strf+"/draw.R");
       		p.waitFor();    
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		Operations ob = new Operations();

		/* The object used to read inputs */
		Scanner ipObj = new Scanner(System.in);

		/* The string used to store infixRegEx */
		String infixRegEx = null;

		/* The string used to store postFixRegEx */
		String postfixRegEx = null;

		System.out.println("Enter the Regular Expression");
		infixRegEx = ipObj.next();
		ipObj.close();

		postfixRegEx = RegExConverter.infixToPostfix(infixRegEx);

		System.out.println("The expression in postFix form is " + postfixRegEx);
		System.out.println();

		Table nfa = ob.createNFA(postfixRegEx);
		System.out.println("NFA");
		ob.displayTable(nfa);
		System.out.println("DFA");
		Table dfa = ob.convertNfaToDfa(nfa);
		ob.displayTable(dfa);
		ob.drawGraphNfa(nfa);
		ob.drawGraph(dfa);

		System.out.println("Enter the string to be checked");
		System.out.println(ob.check(dfa,ipObj.next()));
	}

}
