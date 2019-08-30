package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
	
	/*
	 * @brief the state passed in during construction
	 */
	private State.StateView m_state;
	
	/*
	 * @brief the x dimension of the map
	 */
	private int m_xExtent;
	
	/*
	 * @brief the y dimension of the map
	 */
	private int m_yExtent;
	
	/*
	 * @brief a list of all of player 0's unit ids
	 */
	private List<Integer> m_player0IDs;
	
	/*
	 * @brief a list of all of player 1's unit ids
	 */
	private List<Integer> m_player1IDs;
	
	/*
	 * @brief initial amount of footmen in the Environment
	 */
	private int numFootmen;
	
	/*
	 * @brief initial amount Archers in the enivironment
	 */
	private int numArchers;
	
	/*
	 * @brief a list of the resource IDs stored in m_state
	 */
	private List<Integer> m_resourceIDs;
	
	//Footman Health Points
	private int footmanHP; 
	
	//Damage Points of a Footman Melee Attack
	private int footmanAttack;
	
	//Archer Health Points; (Utility Feature)
	private int archerHP;
	
	//Damage Points of a Archer Arrow Attack
	private int archerAttack;
	
	
	//

		

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * 
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs belonging to the player.
     * You control player 0, the enemy controls player 1.
     * 
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * 
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * 
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView _state) 
    {
    	m_state = _state;
    	
    	m_xExtent = m_state.getXExtent();
    	m_yExtent = m_state.getYExtent();
    	
    	m_resourceIDs = m_state.getAllResourceIds();
    	
    	m_player0IDs = m_state.getUnitIds(0);
    	m_player1IDs = m_state.getUnitIds(1);
    	
    	numFootmen = m_player0IDs.size();
    	numArchers = m_player1IDs.size();
    	
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() 
    {
        double utility = 0.0;
        int deadArchers = numArchers - m_player1IDs.size(); //Utility goes up relative to the amount of Archers that are no longer alive
        utility = utility + ( deadArchers * 0.5);
        
        if(m_player1IDs.isEmpty())
        	utility = 20; //utility is given an abritary max value when there are no more archers alive
        
        int lowArchers = 0; //amount of archers in the environment with low health
        
        for (int a : m_player1IDs)
        {
        	if(isLowHealth(a))
        		lowArchers ++;
        }
        
        utility = utility + (lowArchers * 0.2);
        
        return utility;  
    }
    
    /*
     * @brief helper function for getUtility(). Determines an id has low health or not
     * @param the id of the archer
     * @return boolean if that archer has health lower than 25% of its base health
     */
    public boolean isLowHealth(int id)
    {
    	UnitView unit = m_state.getUnit(id);
    	double initialHealth = unit.getTemplateView().getBaseHealth();
    	double health = unit.getHP();
    	
    	if(health <= (initialHealth * .25))
    		return true;
    	
    	else
    		return false;
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are 
     * your GameStateChildren.
     * 
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     *
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * 
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * 
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * 
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * 
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     * 
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() 
    {
    	List<GameStateChild> possibleActions = new ArrayList<GameStateChild>();
    	
    	Action temp = new Action(0, null);
    	Map<Integer, Action> actionMap = new HashMap<Integer, Action>();
    	GameStateChild nextState = new GameStateChild(null, null);
    	State tempState = new State();

    	int q = 0;
    	
    	for(Integer player0ID : m_player0IDs) //accounts for any amount of units that player 0 has.
    	{
	    	for(Direction direction : Direction.values()) //checks the cardinal directions to generate new states
	    	{
	    		if(direction == Direction.NORTH || direction == Direction.SOUTH || 
	    				direction == Direction.EAST || direction == Direction.WEST) 
	    		{	
	    			int x = direction.xComponent();
		    		int y = direction.yComponent();
		    		
		    		if(m_state.inBounds(x,y)) //only considers states that are in bound
		    		{
		    			if(m_state.isUnitAt(x,y)) //checks for units at neighboring spaces. Ignores if not archer, performs Attack action if archer.
		    			{
		    				for(Integer player1ID : m_player1IDs) {
			    				if(m_state.unitAt(x,y) == player1ID) {
			    					temp = Action.createPrimitiveAttack(player0ID, player1ID);
			    				}
		    				}
		    				actionMap.put(player0ID, temp);
		    				nextState = new GameStateChild(actionMap, this);
		    			}
		    			else //if there is nothing in a neighboring space, perform Move action.
		    			{
		    				temp = Action.createPrimitiveMove(player0ID, direction);
		    				actionMap.put(player0ID, temp);
		    				nextState = new GameStateChild(actionMap, this);
		    			}
		    		}
	    		}
	    		possibleActions.add(nextState);
	    		System.out.println("Added a possible action " + q);
	    		q++;
	    	}
    	}
    	
    	
        return possibleActions;
    }
    
    /*
     * 
     * Getters and Setter methods
     * 
     * 
     */
    
    /*
     * @brief Returns the state member stored in this class
     */
    public State.StateView getState()
    {
    	return m_state;
    }
    
    /*
     * @brief Returns the xExtent member stored in this class
     */
    public int getXExtent()
    {
    	return m_xExtent;
    }
    
    /*
     * @brief Returns the yExtent member stored in this class
     */
    public int getYExtent()
    {
    	return m_yExtent;
    }
    
    /*
     * @brief Returns the resource IDs list stored in this class
     */
    public List<Integer> getResourceIDs()
    {
    	return m_resourceIDs;
    }
    
    /*
     * @brief Returns the Footman's current health
     */
    public int getFootmanHP()
    {
    	return this.footmanHP;
    }
    
    /*
     * @brief Returns the Archer's current health
     */
    public int getArcherHP()
    {
    	return this.archerHP;
    }
    
    /*
     * @brief Sets the Archer's current health
     */
    public void setArcherHP(int health)
    {
    	this.archerHP = health;
    }
    
    /*
     * @brief Sets the Footman's current health
     */
    public void setFootmanHP(int health)
    {
    	this.footmanHP = health;
    }
    
    
    
}
