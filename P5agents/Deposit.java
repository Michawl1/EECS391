package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class Deposit implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		if(state.amountGold > 0 || state.amountWood > 0)
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
		state.amountGold = 0;
		state.amountWood = 0;
		return state;
	}

}
