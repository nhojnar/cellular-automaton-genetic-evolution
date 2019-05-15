package com.hojnar.app.cellular_automaton;
import processing.core.*;
import maphandler.*;
import com.hojnar.app.neuralnetwork.*;

public class Jerry 
{
	final int INPUT_NODES = 13, HIDDEN_NODES = 40, OUTPUT_NODES = 7;
	
	PVector pos;
	float health, maxHealth, energy, maxEnergy, hunger, maxHunger, food, maxFood, score;
	float fitness;
	boolean dead, sleeping;
	Tile currentTile;
	Brain brain;
	Map map;
	
	
	public Jerry(PVector pos, float maxHealth, float maxEnergy, NeuralNetwork brain, PApplet app, Map map)
	{
		if(brain != null)
			this.brain = brain.copy();
		else
			this.brain = new NeuralNetwork(INPUT_NODES, HIDDEN_NODES, OUTPUT_NODES, app);
		this.pos = pos;
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.maxEnergy = maxEnergy;
		this.energy = maxEnergy;
		
		this.hunger = 0;
		this.maxHunger = 20;
		this.food = 0;
		this.maxFood = 20;
		
		this.dead = false;
		this.sleeping = false;
		this.currentTile = map.tileAt(pos);
		this.currentTile.occupied = false;
		
		this.score = 0;
		this.fitness = 0f;
		
		this.map = map;
	}
	
	public void update(PApplet app)
	{
		if(!dead)
		{
			app.fill(0, 0, 255);
			
			if(health <= 0)
				die();
			if(hunger >= maxHunger)
			{
				hunger = maxHunger;
				health -= 6;
			}
			if(sleeping)
			{
				app.fill(60);
				sleeping = false;
				energy = maxEnergy;
			}
			else if(energy < 0) sleep();
			else think(app);
			//if(((Main) app).generationTickCounter % 50 == 0 && ((Main) app).generationTickCounter != 0) score += 100;
			score++;
			hunger++;
			
			app.circle(this.pos.x*map.gridScale, this.pos.y*map.gridScale, map.gridScale/3);
		}
	}
	
	void think(PApplet app)
	{
		float inputs[] = new float[this.brain.inputNodes];
	    
	    Tile up = this.pos.y != 0 ? map.tileAt(this.pos.x, this.pos.y-1) : null;
	    Tile right = this.pos.x != map.grid.size()-1 ? map.tileAt(this.pos.x+1, this.pos.y) : null;
	    Tile down = this.pos.y != map.grid.get(0).size()-1 ? map.tileAt(this.pos.x, this.pos.y+1) : null;
	    Tile left = this.pos.x != 0 ? map.tileAt(this.pos.x-1, this.pos.y) : null;
	    
	    inputs[0] = this.health /  this.maxHealth;
	    inputs[1] = this.energy /  this.maxEnergy;
	    inputs[2] = this.hunger /  this.maxHunger;
	    inputs[3] = this.food /  this.maxFood;
	    inputs[4] = this.currentTile.food / this.currentTile.maxFood;
	    inputs[5] = up != null ? up.food / up.maxFood : 0;
	    inputs[6] = right != null ? right.food / right.maxFood : 0;
	    inputs[7] = down != null ? down.food / down.maxFood : 0;
	    inputs[8] = left != null ? left.food / left.maxFood : 0;
	    inputs[9] = up != null ? (up.occupied?1:0) : 1;
	    inputs[10] = right != null ? (right.occupied?1:0) : 1;
	    inputs[11] = down != null ? (down.occupied?1:0) : 1;
	    inputs[12] = left != null ? (left.occupied?1:0) : 1;
	    //inputs[13] = up != null ? (up.terrainType.terrainInt() / 3.0f) : 0;
	    //inputs[14] = right != null ? (right.terrainType.terrainInt() / 3.0f) : 0;
	    //inputs[15] = down != null ? (down.terrainType.terrainInt() / 3.0f) : 0;
	    //inputs[16] = left != null ? (left.terrainType.terrainInt() / 3.0f) : 0;
	    
	    float output[] = this.brain.predict(inputs);
	    float maxO = PApplet.max(output);
	    if(maxO == output[0])
	        this.move(Direction.NORTH);
	    else if(maxO == output[1])
	        this.move(Direction.EAST);
	    else if(maxO == output[2])
	        this.move(Direction.SOUTH);
	    else if(maxO == output[3])
	        this.move(Direction.WEST);
	    else if(maxO == output[4])
	        this.sleep();
	    else if(maxO == output[5])
	        this.harvest();
	    else if(maxO == output[6])
	        this.eat();
	}
	
	public void move(Direction dir)
	{
		Tile target;
		switch(dir)
		{
		case NORTH:
			if(pos.y == 0) return;
			target = map.tileAt(pos.x, pos.y-1);
			if(target.occupied || target.terrainType == TerrainType.WATER) {hunger += 2; return;}
			pos.y -= 1;
			currentTile.occupied = false;
			currentTile = map.tileAt(pos);
			currentTile.occupied = true;
			break;
		case EAST:
			if(pos.x == map.grid.size() - 1) return;
			target = map.tileAt(pos.x + 1, pos.y);
			if(target.occupied || target.terrainType == TerrainType.WATER) {hunger += 2; return;}
			pos.x += 1;
			currentTile.occupied = false;
			currentTile = map.tileAt(pos);
			currentTile.occupied = true;
			break;
		case SOUTH:
			if(pos.y == map.grid.get(0).size() - 1) return;
			target = map.tileAt(pos.x, pos.y + 1);
			if(target.occupied || target.terrainType == TerrainType.WATER) {hunger += 2; return;}
			pos.y += 1;
			currentTile.occupied = false;
			currentTile = map.tileAt(pos);
			currentTile.occupied = true;
			break;
		case WEST:
			if(pos.x == 0) return;
			target = map.tileAt(pos.x - 1, pos.y);
			if(target.occupied || target.terrainType == TerrainType.WATER) {hunger += 2; return;}
			pos.x -= 1;
			currentTile.occupied = false;
			currentTile = map.tileAt(pos);
			currentTile.occupied = true;
			break;
		}
		
		this.energy -= 10;
	}
	
	public void sleep()
	{
		this.sleeping = true;
	}
	
	public void harvest()
	{
		float foodTaken = (currentTile.food > 5) ? 5 : currentTile.food;
		currentTile.food -= foodTaken;
		food += foodTaken;
		energy -= 20;
		if(food > maxFood)
			food = maxFood;
		this.hunger++;
	}
	
	public void eat()
	{
		float foodEaten = (food != 0) ? ((food > 6) ? 6 : food) : 0;
		food -= foodEaten;
		hunger -= foodEaten;
		energy -= 5;
	}
	
	void die()
	{
		dead = true;
		currentTile.occupied = false;
	}
	
	void mutate()
	{
		mutate(0.2f);
	}
	void mutate(float value)
	{
		brain.mutate(value);
	}
}
