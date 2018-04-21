package GAPL_project4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import javafx.util.Pair;


public class mastGamer extends StateMachineGamer {

	private mastTree myTree;
	private int steps = Integer.MAX_VALUE;
	private Map<Pair<Integer,Move>,Pair<Double,Integer>> Qmast = new HashMap<Pair<Integer,Move>,Pair<Double,Integer>>();
	private int iter = 0;

	@Override
	public String getName() {
		return "mastGamer";
	}

	@Override
	public StateMachine getInitialStateMachine() {
        return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		StateMachine theMachine = getStateMachine();
		myTree = createTree();
		mastUtils.MCTS(myTree, theMachine, getRole(), steps, timeout, Qmast, 10, 50);
	}

	public mastTree createTree() throws MoveDefinitionException
	{
		return new mastTree(getCurrentState(), null, getStateMachine());
	}

	public mastTree getmyTree()
	{
		return myTree;
	}



	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// System.out.println("timeout: " + timeout + "\n");
		StateMachine theMachine = getStateMachine();

		List<GdlTerm> lastMoves = getMatch().getMostRecentMoves();
		if (lastMoves != null) {
			List<Move> jointMove = new ArrayList<Move>();
			for (GdlTerm sentence : lastMoves)
			{
			    jointMove.add(theMachine.getMoveFromTerm(sentence));
			}
			myTree = myTree.getChild(jointMove);
			myTree.setParent(null);
		}

		Pair<Move, mastTree> p;
		// TODO: move the root node here to the child corresponding to jointMove
		// else we are still in the initial state of the game
		p = mastUtils.MCTS(myTree, theMachine, getRole(), steps, timeout, Qmast, 10, 50);
		// myTree = p.getValue();
		// System.out.println(myTree.toString());
		Move myMove = p.getKey();

		//System.out.println("Legal moves: "+ Arrays.toString(myTree.getLegalMoves()[0]));
		//System.out.println("Q scores: "+Arrays.toString(myTree.getAllQScores()[0]));
		//System.out.println("N scores: "+Arrays.toString(myTree.getAllNs()[0]));
		iter += myTree.getNoIterations();
		System.out.println("Number of iterations RMGamer: " + iter + "\n");

		return myMove;
	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}


}
