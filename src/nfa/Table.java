package nfa;

import java.util.ArrayList;


public class Table {

	private ArrayList<State> sList;
	
	private State startState;
	
	private State endState;
	
	public ArrayList<State> getsList() {
		return sList;
	}

	public void setsList(ArrayList<State> sList) {
		this.sList = sList;
	}

	public State getEndState() {
		return endState;
	}

	public void setEndState(State endState) {
		this.endState = endState;
	}

	public Table(){
		sList=new ArrayList<>();
	}
	
	public void push(State s0) {
		// TODO Auto-generated method stub
		sList.add(s0);
	}

	public State getStartState() {
		// TODO Auto-generated method stub
		return startState;
	}

	public State getLastState() {
		// TODO Auto-generated method stub
		return endState;
	}

	
	public void setStartState(State startState) {
		// TODO Auto-generated method stub
		this.startState=startState;
	}
	
	public void setLastState(State endState){
		this.endState=endState;
		this.endState.setFinalState(true);
	}
	
	public State pop(){
		State a=sList.get(sList.size()-1);
		sList.remove(sList.size()-1);
		return a;
	}
}
