import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.json.Test;

public class AstarAgent extends Agent {

	class MapLocation implements Comparator<MapLocation>, Comparable<MapLocation>
	{
		public int x, y;
		public MapLocation location;
		public float cost;

		public MapLocation()
		{
			this.x = 0;
			this.y = 0;
			this.location = null;
			this.cost = 0;
		}
		
		public MapLocation(int x, int y, MapLocation cameFrom, float cost) {
			this.x = x;
			this.y = y;
			this.location = cameFrom;
			this.cost = cost;
		}

		/*
		 * @brief getter function for the location
		 * @pre location is a valid MapLocation
		 * @post location has been returned
		 * @details
		 * @returns the location
		 */
		public MapLocation getParent() {
			return location;
		}

		/*
		 * @brief sets the parent object for the MapLocation
		 * @pre
		 * @post location has been set
		 * @param[in] _location the previous location
		 * @details
		 * @return this function performs an action and does return a value
		 */
		public void setParent(MapLocation _location) {
			location = _location;
		}

		/*
		 * @brief Sets the cost for the MapLocation
		 * @pre
		 * @post the cost has been set
		 * @param[in] _cost the cost
		 * @details
		 * @return this function performs an action and does not return a value
		 */
		public void setCost(float _cost) {
			cost = _cost;
		}

		/*
		 * @brief checks if location has a parent
		 * @param 
		 * @return boolean 
		 */
		public boolean hasParent()
		{
			if(this.location!=null)
				return true;
			else
				return false;
		}
		
		 
		/* 
		 * @brief compares the cost of MapLocations 
		 * @param MapLocation 1
		 * @param MapLocation 2
		 * @returns int corresponding to the relation between the two costs
		 */
		@Override
		public int compare(MapLocation _map1, MapLocation _map2) 
		{
			if (_map1.cost > _map2.cost)
			{
				return 1;
			}
			else if (_map1.cost < _map2.cost)
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
		
		/*
		 * @brief checks if the field location and the object's location are the same
		 * @param Object 
		 * @return boolean 
		 */
		@Override 
		public boolean equals(Object _obj)
		{
			MapLocation m_equals = (MapLocation) _obj;
			if(this.x == m_equals.x && this.y == m_equals.y)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
	/*	@Override
		public int hashCode()
		{
			return this.x * this.y + (int)this.cost;
		} */
		
		@Override // toString for the MapLocation coordinates and cost
		public String toString()
		{
			return "x: " + this.x + ", y: " + this.y + ", cost: " + this.cost + " ||";
		}

		/*
		 *@brief compares the cost of the field MapLocation and the entered MapLocation
		 *@param MapLocation 
		 * @return int corresponding to the relationship of the two MapLocations
		 */
		@Override
		public int compareTo(MapLocation o) 
		{
			if(this.cost > o.cost)
			{
				return 1;
			}
			else if (this.cost < o.cost)
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
	}

	Stack<MapLocation> path;
	int footmanID, townhallID, enemyFootmanID;
	MapLocation nextLoc;

	private long totalPlanTime = 0; // nsecs
	private long totalExecutionTime = 0; // nsecs

	public AstarAgent(int playernum) { // Astar Constructor
		super(playernum);

		System.out.println("Constructed AstarAgent");
	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
		// get the footman location
		List<Integer> unitIDs = newstate.getUnitIds(playernum);

		if (unitIDs.size() == 0) {
			System.err.println("No units found!");
			return null;
		}

		footmanID = unitIDs.get(0);

		// double check that this is a footman
		if (!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman")) {
			System.err.println("Footman unit not found");
			return null;
		}

		// find the enemy playernum
		Integer[] playerNums = newstate.getPlayerNumbers();
		int enemyPlayerNum = -1;
		for (Integer playerNum : playerNums) {
			if (playerNum != playernum) {
				enemyPlayerNum = playerNum;
				break;
			}
		}

		if (enemyPlayerNum == -1) {
			System.err.println("Failed to get enemy playernumber");
			return null;
		}

		// find the townhall ID
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

		if (enemyUnitIDs.size() == 0) {
			System.err.println("Failed to find enemy units");
			return null;
		}

		townhallID = -1;
		enemyFootmanID = -1;
		for (Integer unitID : enemyUnitIDs) {
			Unit.UnitView tempUnit = newstate.getUnit(unitID);
			String unitType = tempUnit.getTemplateView().getName().toLowerCase();
			if (unitType.equals("townhall")) {
				townhallID = unitID;
			} else if (unitType.equals("footman")) {
				enemyFootmanID = unitID;
			} else {
				System.err.println("Unknown unit type");
			}
		}

		if (townhallID == -1) {
			System.err.println("Error: Couldn't find townhall");
			return null;
		}

		long startTime = System.nanoTime();
		path = findPath(newstate);
		totalPlanTime += System.nanoTime() - startTime;

		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
		long startTime = System.nanoTime();
		long planTime = 0;

		Map<Integer, Action> actions = new HashMap<Integer, Action>();

		if (shouldReplanPath(newstate, statehistory, path)) {
			long planStartTime = System.nanoTime();
			path = findPath(newstate);
			planTime = System.nanoTime() - planStartTime;
			totalPlanTime += planTime;
		}

		Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

		int footmanX = footmanUnit.getXPosition();
		int footmanY = footmanUnit.getYPosition();

		if (!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

			// stat moving to the next step in the path
			nextLoc = path.pop();

			System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
		}

		if (nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y)) {
			int xDiff = nextLoc.x - footmanX;
			int yDiff = nextLoc.y - footmanY;

			// figure out the direction the footman needs to move in
			Direction nextDirection = getNextDirection(xDiff, yDiff);

			actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
		} else {
			Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

			// if townhall was destroyed on the last turn
			if (townhallUnit == null) {
				terminalStep(newstate, statehistory);
				return actions;
			}

			if (Math.abs(footmanX - townhallUnit.getXPosition()) > 1
					|| Math.abs(footmanY - townhallUnit.getYPosition()) > 1) {
				System.err.println("Invalid plan. Cannot attack townhall");
				totalExecutionTime += System.nanoTime() - startTime - planTime;
				return actions;
			} else {
				System.out.println("Attacking TownHall");
				// if no more movements in the planned path then attack
				actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
			}
		}

		totalExecutionTime += System.nanoTime() - startTime - planTime;
		return actions;
	}

	@Override
	public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
		System.out.println("Total turns: " + newstate.getTurnNumber());
		System.out.println("Total planning time: " + totalPlanTime / 1e9);
		System.out.println("Total execution time: " + totalExecutionTime / 1e9);
		System.out.println("Total time: " + (totalExecutionTime + totalPlanTime) / 1e9);
	}

	@Override
	public void savePlayerData(OutputStream os) {

	}

	@Override
	public void loadPlayerData(InputStream is) {

	}

	/**
	 * You will implement this method.
	 *
	 * This method should return true when the path needs to be replanned and false
	 * otherwise. This will be necessary on the dynamic map where the footman will
	 * move to block your unit.
	 * 
	 * You can check the position of the enemy footman with the following code:
	 * state.getUnit(enemyFootmanID).getXPosition() or .getYPosition().
	 * 
	 * There are more examples of getting the positions of objects in SEPIA in the
	 * findPath method.
	 *
	 * @param state
	 * @param history
	 * @param currentPath
	 * @return
	 */
	private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath) 
	{
		MapLocation nextLoc = new MapLocation();
		if(!currentPath.isEmpty())
		{
			nextLoc = currentPath.peek();
		}
		
		if(enemyFootmanID != -1 && (state.getUnit(enemyFootmanID).getXPosition() == nextLoc.x) && (state.getUnit(enemyFootmanID).getYPosition() == nextLoc.y)) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * This method is implemented for you. You should look at it to see examples of
	 * how to find units and resources in Sepia.
	 *
	 * @param state
	 * @return
	 */
	private Stack<MapLocation> findPath(State.StateView state) {
		Unit.UnitView townhallUnit = state.getUnit(townhallID);
		Unit.UnitView footmanUnit = state.getUnit(footmanID);

		MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

		MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

		MapLocation footmanLoc = null;
		if (enemyFootmanID != -1) {
			Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
			footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
		}

		// get resource locations
		List<Integer> resourceIDs = state.getAllResourceIds();
		Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

			resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
		}

		return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
	}

	/**
	 * This is the method you will implement for the assignment. Your implementation
	 * will use the A* algorithm to compute the optimum path from the start position
	 * to a position adjacent to the goal position.
	 *
	 * Therefore your you need to find some possible adjacent steps which are in
	 * range and are not trees or the enemy footman. Hint: Set<MapLocation>
	 * resourceLocations contains the locations of trees
	 *
	 * You will return a Stack of positions with the top of the stack being the
	 * first space to move to and the bottom of the stack being the last space to
	 * move to. If there is no path to the townhall then return null from the method
	 * and the agent will print a message and do nothing. The code to execute the
	 * plan is provided for you in the middleStep method.
	 *
	 * As an example consider the following simple map
	 *
	 * F - - - - x x x - x H - - - -
	 *
	 * F is the footman H is the townhall x's are occupied spaces
	 *
	 * xExtent would be 5 for this map with valid X coordinates in the range of [0,
	 * 4] x=0 is the left most column and x=4 is the right most column
	 *
	 * yExtent would be 3 for this map with valid Y coordinates in the range of [0,
	 * 2] y=0 is the top most row and y=2 is the bottom most row
	 *
	 * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
	 *
	 * The path would be
	 *
	 * (1,0) (2,0) (3,1) (2,2) (1,2)
	 *
	 * Notice how the initial footman position and the townhall position are not
	 * included in the path stack
	 *
	 * @param start             Starting position of the footman
	 * @param goal              MapLocation of the townhall
	 * @param xExtent           Width of the map
	 * @param yExtent           Height of the map
	 * @param resourceLocations Set of positions occupied by resources
	 * @return Stack of positions with top of stack being first move in plan
	 */
	private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent,
										   MapLocation enemyFootmanLoc, 
										   Set<MapLocation> resourceLocations) 
	{
		start.setParent(null);
		
		Object[] resources = resourceLocations.toArray();
		
		PriorityQueue<MapLocation> openList = new PriorityQueue<MapLocation>();
		Stack<MapLocation> closedList = new Stack<MapLocation>();
		
		if(isValid(start, enemyFootmanLoc, xExtent, yExtent, resources) == false)
		{
			System.out.println("the start cannot be located on the map");
			return null;
		}
		
		if(isValid(goal, enemyFootmanLoc, xExtent, yExtent, resources) == false)
		{
			System.out.println("the goal cannot be located on the map");
			return null;
		}
		

		openList.add(start);
		
		int x = 0; //cost that increments at each iteration of the while loop 
		
		while (!openList.isEmpty()) 
		{
			MapLocation m = openList.poll();
			
			closedList.push(m);
			
			if(m.equals(goal))
			{
				break;
			}
			
			for(int i = 0; i < 8; i++)
			{
				MapLocation neighbor = new MapLocation();
				switch(i)
				{
				case 0:
					neighbor = new MapLocation(m.x + 1, m.y, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 1:
					neighbor = new MapLocation(m.x + 1, m.y + 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 2:
					neighbor = new MapLocation(m.x, m.y + 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 3:
					neighbor = new MapLocation(m.x - 1, m.y + 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 4:
					neighbor = new MapLocation(m.x - 1, m.y, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 5:
					neighbor = new MapLocation(m.x - 1, m.y - 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 6:
					neighbor = new MapLocation(m.x, m.y - 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				case 7:
					neighbor = new MapLocation(m.x + 1, m.y - 1, m, 0);
					neighbor.setCost(AStarHeuristic(neighbor, goal) + x);
					if(!closedList.contains(neighbor) && isValid(neighbor, enemyFootmanLoc, xExtent, yExtent, resources))
					{
						openList.add(neighbor);
					}
					break;
				default:
					break;
				}
			}

			x++;
		}
		
		Stack<MapLocation> finalList = new Stack<MapLocation>();
		int size = closedList.size();
		
		MapLocation g = closedList.pop();
		
		//Kills the footman if it can't reach the goal
		if ( Math.abs(g.x - goal.x) > 1 || Math.abs(g.y - goal.y) > 1)
		{
			System.out.println("Path Not Found");
			System.exit(0);
		}
		
		while(g.hasParent())
		{
			g = g.getParent();
			finalList.push(g);
		}
		finalList.pop();
	
		System.out.println("Final path: " + finalList);
		
		return finalList;
	}

	/**
	 * @brief implements the chebyshev distance as a heuristic
	 * @pre
	 * @post
	 * @param[in]
	 * @return the heuristic cost of the provided points
	 */
	public int AStarHeuristic(MapLocation start, MapLocation goal) 
	{
		return Math.max(Math.abs(goal.x - start.x), Math.abs(goal.y - start.y));
	}
	
	/**
	 * @brief checks if the Map location is valid
	 * @param _test the map location that you want to test if valid
	 * @param _xExtent the x scope of the map
	 * @param _yExtent the y scope of the map
	 * @return whether or not _test is a valid map location
	 */
	public boolean isValid(MapLocation _test, MapLocation _footMan, int _xExtent, int _yExtent, Object[] _resources)
	{
		for(int i = 0; i < _resources.length; i++)
		{
			if(_resources[i].equals(_test))
			{
				return false;
			}
				
		}
		if(_footMan != null && _test.x == _footMan.x && _test.y == _footMan.y)
		{
			return false;
		}
		
		if(_test.x >= 0 && _test.y >=0 &&
		   _test.x < _xExtent && _test.y < _yExtent)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Primitive actions take a direction (e.g. Direction.NORTH,
	 * Direction.NORTHEAST, etc) This converts the difference between the current
	 * position and the desired position to a direction.
	 *
	 * @param xDiff Integer equal to 1, 0 or -1
	 * @param yDiff Integer equal to 1, 0 or -1
	 * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
	 */

	private Direction getNextDirection(int xDiff, int yDiff) {

		// figure out the direction the footman needs to move in
		if (xDiff == 1 && yDiff == 1) {
			return Direction.SOUTHEAST;
		} else if (xDiff == 1 && yDiff == 0) {
			return Direction.EAST;
		} else if (xDiff == 1 && yDiff == -1) {
			return Direction.NORTHEAST;
		} else if (xDiff == 0 && yDiff == 1) {
			return Direction.SOUTH;
		} else if (xDiff == 0 && yDiff == -1) {
			return Direction.NORTH;
		} else if (xDiff == -1 && yDiff == 1) {
			return Direction.SOUTHWEST;
		} else if (xDiff == -1 && yDiff == 0) {
			return Direction.WEST;
		} else if (xDiff == -1 && yDiff == -1) {
			return Direction.NORTHWEST;
		}

		System.err.println("Invalid path. Could not determine direction");
		return null;
	}
}
