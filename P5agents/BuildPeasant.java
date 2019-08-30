package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class BuildPeasant implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		if(state.amountGold >= 400)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public GameState apply(GameState state) {
		state.amountGold -= 400;
		return state;
	}

}
