package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Mine implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		List<Integer> resourceIDs = state.m_state.getAllResourceIds();
		boolean mineFlag = false;
		for(int i = 0; i < resourceIDs.size(); i++)
		{
			if(state.m_state.getResourceNode(resourceIDs.get(i)).getType() == ResourceNode.Type.GOLD_MINE &&
					state.m_state.getResourceNode(resourceIDs.get(i)).getAmountRemaining() > 0)
			{
				mineFlag = true;
			}
		}
		
		if(state.amountGold == 0 && state.amountWood == 0 && mineFlag)
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
		state.amountGold = state.amountGold + 100;
		return state;
	}
}
