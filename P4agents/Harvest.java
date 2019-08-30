package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Harvest implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		List<Integer> resourceIDs = state.m_state.getAllResourceIds();
		boolean harvestFlag = false;
		for(int i = 0; i < resourceIDs.size(); i++)
		{
			if(state.m_state.getResourceNode(resourceIDs.get(i)).getType() == ResourceNode.Type.TREE &&
					state.m_state.getResourceNode(resourceIDs.get(i)).getAmountRemaining() > 0)
			{
				harvestFlag = true;
			}
		}
		
		if(state.amountGold == 0 && state.amountWood == 0 && harvestFlag)
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
		state.amountWood = state.amountWood + 100;
		return state;
	}

}
