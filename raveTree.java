package GAPL_project4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class raveTree {

	private raveTree parent;
	private StateMachine machine;
	private MachineState state;
	private Move[][] legalMoves = null; // 2d array of legal moves for every role [no. roles][no. legal moves for given role]

	/* Role variables */
	private List<Role> roles;
	private Map<Role,Integer> roleIndex = new HashMap<Role,Integer>();
	private int nRoles;

	/* Children */
	private Map<List<Move>,raveTree> children = new HashMap<List<Move>,raveTree>();

	/* Q scores, child and self visit counter*/
	private double[][] Qs = null; // 2d array of Q values for each role for each move
	private double[][] Qrave = null;
	private int[][] Ns = null; // Counts how many times each child state has been visited
	private int[][] Nrave = null;
	private int N = 0;
	private int iterations = 0;

	public raveTree(MachineState state, raveTree parent, StateMachine sm) throws MoveDefinitionException {
		this.state = state;
		this.parent = parent;
		machine = sm;
		initalize();
	}

	private void initalize() throws MoveDefinitionException {
		// Init legal moves, roles, and Qs and Ns to 0
		roles = machine.getRoles();
		nRoles = roles.size();
		for(int i = 0; i < nRoles; i++) {
			roleIndex.put(roles.get(i), (Integer) i);
		}
		legalMoves = new Move[nRoles][];
		Qs = new double[nRoles][];
		Ns = new int[nRoles][];
		Qrave = new double[nRoles][];
		Nrave = new int[nRoles][];
		Move[] movesArr;
		for(int i = 0; i < roles.size(); i++) {
			if(!machine.isTerminal(this.getState())) {
				List<Move> moves = machine.getLegalMoves(state, roles.get(i));
				movesArr = moves.toArray(new Move[moves.size()]);
				legalMoves[i] = movesArr;
				Qs[i] = new double[movesArr.length];
				Ns[i] = new int[movesArr.length];
				Qrave[i] = new double[movesArr.length];
				Nrave[i] = new int[movesArr.length];
			}
		}
	}

	public raveTree getParent() {
		return parent;
	}

	public void setParent(raveTree p) {
		parent = p;
	}

	public MachineState getState() {
		return state;
	}

	public boolean isTerminal() {
		return machine.isTerminal(state);
	}

	public Move[][] getLegalMoves() {
		return legalMoves;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public int getNoRoles() {
		return nRoles;
	}

	public int getRoleIndex(Role role) {
		return roleIndex.get(role).intValue();
	}

	public int[] getJointMoveIndex(List<Move> jointMove) throws MoveDefinitionException
	{
		int[] jointMoveIndex = new int[jointMove.size()];
		int roleIdx = 0;
		for(Move move : jointMove)
		{
			boolean foundMove = false;
			for(int i = 0; i < legalMoves[roleIdx].length; i++)
			{
				if(move.equals(legalMoves[roleIdx][i]))
				{
					foundMove = true;
					jointMoveIndex[roleIdx] = i;
				}
			}
			if(!foundMove) return null;
			roleIdx++;
		}
		return jointMoveIndex;
	}


	public void addChild(List<Move> M) throws MoveDefinitionException, TransitionDefinitionException {
		MachineState childState = machine.getNextState(state, M);
		children.put(M, new raveTree(childState,this,machine));
	}

	public raveTree getChild(List<Move> M) throws MoveDefinitionException, TransitionDefinitionException {
		if (children.get(M) == null) {
			addChild(M);
		}
		return children.get(M);
	}

	public boolean hasChild(List<Move> M) {
		return (children.get(M) != null);
	}

	public raveTree[] getChildren() {
		List<raveTree> arr = new ArrayList<raveTree>();
		for (List<Move> key : children.keySet()) {
		    arr.add(children.get(key));
		}
		return arr.toArray(new raveTree[arr.size()]);
	}

	public double[][] getAllQScores() {
		return Qs;
	}

	public double getQScore(int role, int move) {
		return Qs[role][move];
	}

	public void updateQScore(int role, int move, double val, double k) {
		double beta = Math.sqrt(k/(3*N + k));
		Qs[role][move] = beta*Qrave[role][move] + (1 - beta)*Qs[role][move];
	}

	public double[][] getAllQrave() {
		return Qrave;
	}

	public double getQrave(int role, int move) {
		return Qrave[role][move];
	}

	public void updateQrave(int role, int move, double val) {
		Qrave[role][move] += (val - Qrave[role][move])/((double) Nrave[role][move] + 1);
	}

	public int[][] getAllNs() {
		return Ns;
	}

	public int getNs(int role, int move) {
		return Ns[role][move];
	}

	public void incrNs(int role, int move) {
		Ns[role][move] += 1;
	}

	public int[][] getAllNrave() {
		return Nrave;
	}

	public int getNrave(int role, int move) {
		return Nrave[role][move];
	}

	public void incrNrave(int role, int move) {
		Nrave[role][move] += 1;
	}

	public int getNoSimulation() {
		return N;
	}

	public void incrNoSimulation() {
		N++;
	}

	public int getNoIterations()
	{
		return iterations;
	}

	public void incrNoIterations()
	{
		iterations++;
	}

	@Override
	public String toString()
	{
		String s = "\nCurrent state:\n";
		s += state.toString() + "\n\nChildren:\n";
		for(raveTree c : this.getChildren())
		{
			s += c.getState().toString() + "\n";
		}
		return s;
	}
}
