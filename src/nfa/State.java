package nfa;

import java.util.ArrayList;

public class State implements Comparable<State> {

	private int stateId;

	private boolean finalState = false;

	private boolean trap = false;

	public boolean isTrap() {
		trap = true;
		for (Transitions t : this.direction) {
			if (t.nextState.getStateId() != this.stateId) {
				trap = false;
				break;
			}
		}

		return trap;
	}

	public void setTrap(boolean trap) {
		this.trap = trap;
	}

	class Transitions {
		char ipChar;
		State nextState;
	}

	private ArrayList<Transitions> direction = new ArrayList<Transitions>();

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public ArrayList<Transitions> getDirection() {
		return direction;
	}

	public void setDirection(ArrayList<Transitions> direction) {
		this.direction = direction;
	}

	public boolean isFinalState() {
		return finalState;
	}

	public void setFinalState(boolean finalState) {
		this.finalState = finalState;
	}

	public State(int i) {
		// TODO Auto-generated constructor stub
		stateId = i;
	}

	public void addTransition(char c, State s) {
		// TODO Auto-generated method stub

		Transitions ob = new Transitions();
		ob.ipChar = c;
		ob.nextState = s;
		direction.add(ob);
	}

	public State getNextState(char c) {
		State nextState = null;
		for (Transitions t : direction) {
			if (t.ipChar == c) {
				nextState = t.nextState;
				break;
			}
		}
		return nextState;
	}

	@Override
	public int compareTo(State s0) {
		// TODO Auto-generated method stub
		return this.stateId - s0.stateId;
	}

}
