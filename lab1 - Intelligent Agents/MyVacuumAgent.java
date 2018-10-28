package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.ArrayList;
import java.util.Random;

class Node {
	private int x;
	private int y;
	private int direction;
	private ArrayList<Integer> list;
	
	public Node(int x, int y, int direction, ArrayList<Integer> list) {
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.list = list;
	}

	public int getDirection() {
		return direction;
	}

	public int getY() {
		return y;
	}

	public int getX() {
		return x;
	}
	
	public ArrayList<Integer> getList() {
		return list;
	}
	
}

class MyAgentState {
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN = 0;
	final int WALL = 1;
	final int CLEAR = 2;
	final int DIRT = 3;
	final int HOME = 4;
	final int ACTION_NONE = 0;
	final int ACTION_MOVE_FORWARD = 1;
	final int ACTION_TURN_RIGHT = 2;
	final int ACTION_TURN_LEFT = 3;
	final int ACTION_SUCK = 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	MyAgentState() {
		for (int i = 0; i < world.length; i++)
			for (int j = 0; j < world[i].length; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}

	// Based on the last action and the received percept updates the x & y agent
	// position
	public void updatePosition(DynamicPercept p) {
		Boolean bump = (Boolean) p.getAttribute("bump");

		if (agent_last_action == ACTION_MOVE_FORWARD && !bump) {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info) {
		world[x_position][y_position] = info;
	}

	public void printWorldDebug() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				if (world[j][i] == UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i] == WALL)
					System.out.print(" # ");
				if (world[j][i] == CLEAR)
					System.out.print(" . ");
				if (world[j][i] == DIRT)
					System.out.print(" D ");
				if (world[j][i] == HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	//public int iterationCounter = 1000;
	//public int iterationCounter = 15*15*2;
	public int iterationCounter = 2000;
	public MyAgentState state = new MyAgentState();
	public ArrayList<Integer> instructions = new ArrayList<Integer>();
	public boolean hardcodedWalls = false;
	public boolean goHome = false;
	public boolean[][] visited = new boolean[30][30];


	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other
	// percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if (action == 0) {
			state.agent_direction = ((state.agent_direction - 1) % 4);
			if (state.agent_direction < 0)
				state.agent_direction += 4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action == 1) {
			state.agent_direction = ((state.agent_direction + 1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	public ArrayList<Integer> copyList(ArrayList<Integer> one, ArrayList<Integer> two){
		for (Integer i: one) {
			  two.add(i);
		}
		return two;
	}
	
	public void initVisited() {
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 30; j++) {
				if(state.world[i][j] == state.WALL) {
					visited[i][j] = true;
				}
				else {
					visited[i][j] = false;
				}
			}
		}
	}
	
	private void breadthFirstSearch() {
		ArrayList<Node> nodes = new ArrayList();
		ArrayList<Integer> pathToNextNode = new ArrayList();
		initVisited();
		visited[state.agent_x_position][state.agent_y_position] = true;
		
		nodes.add(new Node(state.agent_x_position, state.agent_y_position, state.agent_direction, new ArrayList<Integer>()));
		while(!nodes.isEmpty()) {
			Node currentNode = nodes.remove(0);
			if (state.world[currentNode.getX()][currentNode.getY()] == state.UNKNOWN) {
				instructions.clear();
				instructions = copyList(currentNode.getList(), instructions);
				break;
			}
			if(goHome == true && currentNode.getX() == 1 && currentNode.getY() == 1) {
				instructions.clear();
				instructions = copyList(currentNode.getList(), instructions);
				break;
			}
			switch(currentNode.getDirection()) {
			case MyAgentState.WEST:
				//check west node
				if(!visited[currentNode.getX()-1][currentNode.getY()] && state.world[currentNode.getX()-1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()-1,currentNode.getY(), currentNode.getDirection(), pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()-1][currentNode.getY()] = true;
				}
				//check north node
				if(!visited[currentNode.getX()][currentNode.getY()-1] && state.world[currentNode.getX()][currentNode.getY()-1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_RIGHT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()-1, currentNode.getDirection()-3, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()-1] = true;
				}
				//check south node
				if(!visited[currentNode.getX()][currentNode.getY()+1] && state.world[currentNode.getX()][currentNode.getY()+1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_LEFT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()+1, currentNode.getDirection()-1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()+1] = true;
				}
				break;
			case MyAgentState.EAST:
				if(!visited[currentNode.getX()+1][currentNode.getY()] && state.world[currentNode.getX()+1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()+1,currentNode.getY(), currentNode.getDirection(), pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()+1][currentNode.getY()] = true;
				}
				//check south node
				if(!visited[currentNode.getX()][currentNode.getY()+1] && state.world[currentNode.getX()][currentNode.getY()+1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_RIGHT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()+1, currentNode.getDirection()+1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()+1] = true;
				}
				//check north node
				if(!visited[currentNode.getX()][currentNode.getY()-1] && state.world[currentNode.getX()][currentNode.getY()-1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_LEFT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()-1, currentNode.getDirection()-1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()-1] = true;
				}
				break;
			case MyAgentState.NORTH:
				if(!visited[currentNode.getX()][currentNode.getY()-1] && state.world[currentNode.getX()][currentNode.getY()-1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()-1, currentNode.getDirection(), pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()-1] = true;
				}
				//check east node
				if(!visited[currentNode.getX()+1][currentNode.getY()] && state.world[currentNode.getX()+1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_RIGHT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()+1,currentNode.getY(), currentNode.getDirection()+1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()+1][currentNode.getY()] = true;
				}
				//check west node
				if(!visited[currentNode.getX()-1][currentNode.getY()] && state.world[currentNode.getX()-1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_LEFT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()-1,currentNode.getY(), currentNode.getDirection()+3, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()-1][currentNode.getY()] = true;
				}
				break;
			case MyAgentState.SOUTH:
				if(!visited[currentNode.getX()][currentNode.getY()+1] && state.world[currentNode.getX()][currentNode.getY()+1] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX(),currentNode.getY()+1, currentNode.getDirection(), pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()][currentNode.getY()+1] = true;
				}
				//check west node
				if(!visited[currentNode.getX()-1][currentNode.getY()] && state.world[currentNode.getX()-1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_RIGHT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()-1,currentNode.getY(), currentNode.getDirection()+1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()-1][currentNode.getY()] = true;
				}
				//check east node
				if(!visited[currentNode.getX()+1][currentNode.getY()] && state.world[currentNode.getX()+1][currentNode.getY()] != state.WALL) {
					pathToNextNode = copyList(currentNode.getList(), new ArrayList<Integer>());
					pathToNextNode.add(state.ACTION_TURN_LEFT);
					pathToNextNode.add(state.ACTION_MOVE_FORWARD);
					Node addNodetoQueue = new Node(currentNode.getX()+1,currentNode.getY(), currentNode.getDirection()-1, pathToNextNode);
					nodes.add(addNodetoQueue);
					visited[currentNode.getX()+1][currentNode.getY()] = true;
				}
				break;
			}
		}
	}

	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions > 0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions == 0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		iterationCounter--;

		if (iterationCounter == 0)
			return NoOpAction.NO_OP;

		state.updatePosition((DynamicPercept) percept);
		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean) p.getAttribute("bump");
		Boolean dirt = (Boolean) p.getAttribute("dirt");
		Boolean home = (Boolean) p.getAttribute("home");
		System.out.println("percept: " + p);
		System.out.println("X: " + state.agent_x_position + ", Y: " + state.agent_y_position);
		System.out.println("Direction: " + state.agent_direction);
		state.printWorldDebug();
		System.out.println("Instructions: " + instructions);
		
		
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.world[state.agent_x_position][state.agent_y_position - 1] = state.WALL;
				break;
			case MyAgentState.SOUTH:
				state.world[state.agent_x_position][state.agent_y_position + 1] = state.WALL;
				break;
			case MyAgentState.WEST:
				state.world[state.agent_x_position - 1][state.agent_y_position] = state.WALL;
				break;
			case MyAgentState.EAST:
				state.world[state.agent_x_position + 1][state.agent_y_position] = state.WALL;
				break;
			}
		}
		
		if (dirt) {
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}
		
		
		
		if(hardcodedWalls) {
			for(int i = 0; i < 16; i++) {
				state.world[0][i] = state.WALL;
				state.world[16][i] = state.WALL;
				state.world[i][0] = state.WALL;
				state.world[i][16] = state.WALL;
			}
			hardcodedWalls = false;
		}
		
		state.world[state.agent_x_position][state.agent_y_position] = state.CLEAR;
		
		if(instructions.isEmpty()) {
			breadthFirstSearch();
		}
		
		if(instructions.isEmpty() && !goHome) {
			if(state.agent_last_action == state.ACTION_MOVE_FORWARD || state.agent_last_action == state.ACTION_SUCK) {
				state.agent_last_action = state.ACTION_TURN_RIGHT;
				state.agent_direction = ((state.agent_direction + 1) % 4);
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}
			else {
				goHome = true;
				breadthFirstSearch();
			}
		}
		
		
		if (!instructions.isEmpty()) {
			state.world[state.agent_x_position][state.agent_y_position] = state.CLEAR;
			int currentInstruction = instructions.remove(0);
			if(currentInstruction == state.ACTION_MOVE_FORWARD && bump) {
				breadthFirstSearch();
				currentInstruction = instructions.remove(0);
			}
			if (currentInstruction == state.ACTION_SUCK) {
				state.agent_last_action = state.ACTION_SUCK;
				return LIUVacuumEnvironment.ACTION_SUCK;
			} else if (currentInstruction == state.ACTION_MOVE_FORWARD) {
				if (!bump) {
					state.agent_last_action = state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
			} else if (currentInstruction == state.ACTION_TURN_LEFT) {
				state.agent_last_action = state.ACTION_TURN_LEFT;
				state.agent_direction = ((state.agent_direction + 4 - 1) % 4);
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
			} else if (currentInstruction == state.ACTION_TURN_RIGHT) {
				state.agent_last_action = state.ACTION_TURN_RIGHT;
				state.agent_direction = ((state.agent_direction + 1) % 4);
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}
		}

		return NoOpAction.NO_OP;

	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
