package com.hojnar.app.cellular_automaton;

public enum Direction {
	NORTH, EAST, SOUTH, WEST;
	public int getNum()
	{
		if(this == NORTH)
			return 0;
		if(this == EAST)
			return 1;
		if(this == WEST)
			return 3;
		if(this == SOUTH)
			return 2;
		return -1;
	}
}
