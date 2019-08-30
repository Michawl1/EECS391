package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;

public class Move implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		
		List<Integer> peasantIDs = state.m_state.getAllPeasantIds();
		for ( int i = 0; i < peasantIDs.size() ; i++)
		{
			if(!(state.getUnit(i).getTemplateView().getName().toLowerCase() == "peasant"))
			{
				for(Direction direction: Direction.values())
		         {
		         	 if(!(direction == Direction.NORTH || direction == Direction.SOUTH || 
		                      direction == Direction.EAST || direction == Direction.WEST))
		         	 {        
		         		 int x = direction.xComponent();
		         		 int y = direction.yComponent();
		              
		         		 if(!(m_state.inBounds(x,y)))
		         		 {
		         			 if(i == (peasantIDs.size()-1))
		         				 return true;
		         		 }
			}
		}
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		
		
	}

}
