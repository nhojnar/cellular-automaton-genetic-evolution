package com.hojnar.app.cellular_automaton;
import processing.core.*;
import com.hojnar.app.neuralnetwork.*;
import java.io.*;
import java.util.*;
import gui.*;
import maphandler.*;
import maphandler.Map;

public class App extends PApplet
{
	int numJerries, runsPerFrame, generationTicks, generationTickCounter, generationNumber;
	Map map;
	Gui gui;
	Gui opener;
	
	Slider runsPerFrameSlider, generationTicksSlider, numJerriesSlider;
	Label runsPerFrameLabel, generationTicksLabel, numJerriesLabel, jerryLabel, generationTickLabel, generationNumberLabel;
	Button startButton, stopButton, saveBrainButton, importBrainButton, mapsButton;
	GuiElement guiElements[];
	
	boolean isOpener;
	boolean run, running, started;
	
	double mutateAmount, mutIntensity;
	Slider mutateSlider, mutIntensitySlider;
	Label mutateLabel, mutIntensityLabel;
	
	ArrayList<Jerry> jerries;
	ArrayList<Jerry> savedJerries;
	
	public static void main(String[] args)
	{
		/*
		SimpleBrain b = new SimpleBrain(4, 5, 2);
		b.getFirstMatrix().print();
		b.getSecondMatrix().print();
		b.mutate(0.8, 0.001);
		b.getFirstMatrix().print();
		b.getSecondMatrix().print();
		*/
		PApplet.main("com.hojnar.app.cellular_automaton.App");
	}
	
	public void settings()
	{
		size(1100, 800);
	}
	public void setup()
	{
		frameRate(30);
		
		isOpener = true;
		run = false;
		running = false;
		started = false;
		
		File mapsFolder = new File("maps\\");
		File[] maps = mapsFolder.listFiles();
		String[] mapList = new String[maps.length];
		for(int i = 0; i < maps.length; i++)
			mapList[i] = maps[i].getName();
		
		opener = new Gui(0, 0, width, height, this);
		int counter = 1;
		int counter2 = 1;
		for(String s : mapList)
		{
			opener.addElement(new Button(175 * counter2, 50 * counter, 150, 40, s, () -> initiate(s), this));
			counter++;
			if(counter > 8)
			{
				counter = 1;
				counter2++;
			}
		}
	}
	
	void initiate(String mapName)
	{
		if(mapName.substring(mapName.length() - 4).equals(".map"))
		{
			if(!started)
			{
				started = true;
				map = MapHandler.importMap(mapName.substring(0, mapName.length()-4), this);
				
				jerries = new ArrayList<Jerry>();
				savedJerries = new ArrayList<Jerry>();
				
				
				generationTickCounter = 0;
				generationNumber = 0;

				isOpener = false;
				
				setupGui();
				updateGuiElements();

				randomJerries();



				
			}
			else
			{
				map = MapHandler.importMap(mapName.substring(0, mapName.length()-4), this);
				isOpener = false;
			}
		}
		else
		{
			System.err.println("Error initiating map import");
		}
	}
	
	void reset()
	{
		setup();
	}
	
	void setupGui()
	{
		gui = new Gui(800, 0, 300, 800, this);
		
		runsPerFrameLabel = new Label(100, 45, 14, "Runs per Frame: " + runsPerFrame);
		runsPerFrameSlider = new Slider(20, 60, 160, 1, 20, () -> updateGuiElements(), true);
		runsPerFrameSlider.setValue(runsPerFrame);
		generationTicksLabel = new Label(100, 105, 14, "Ticks per Generation: " + generationTicks);
		generationTicksSlider = new Slider(20, 120, 160, 50, 300, () -> updateGuiElements(), true);
		generationTicksSlider.setValue(generationTicks);
		numJerriesLabel = new Label(100, 165, 14, "Number of Jerries: " + numJerries);
		numJerriesSlider = new Slider(20, 180, 160, 50, 300, () -> updateGuiElements(), true);
		numJerriesSlider.setValue(numJerries);
		
		gui.addElements(new GuiElement[] {runsPerFrameSlider, generationTicksSlider, numJerriesSlider, runsPerFrameLabel, generationTicksLabel, numJerriesLabel});
		
		startButton = new Button(gui.w/4, gui.h - 50, gui.w/2, 100, "Start", () -> start(), this);
		stopButton = new Button(3*gui.w/4, gui.h - 50, gui.w/2, 100, "Stop", () -> stop(), this);
		startButton.setColor(color(0, 200, 50));
		stopButton.setColor(color(200, 50, 0));
		
		gui.addElements(new GuiElement[] {startButton, stopButton});
		
		jerryLabel = new Label(200, 350, 16, "Jerries: " + jerries.size() + "/" + numJerries);
		generationTickLabel = new Label(200, 370, 16, "Generation Tick: " + generationTickCounter + "/" + generationTicks);
		generationNumberLabel = new Label(gui.w/2, gui.h -  140, 34, "Generation " + generationNumber);
		
		gui.addElements(new GuiElement[] {jerryLabel, generationTickLabel, generationNumberLabel});
		
		mapsButton = new Button(7*gui.w/8, gui.w/8, gui.w/4, gui.w/4, "Maps", () -> changeMap(), this);
		
		gui.addElement(mapsButton);
		
		mutateSlider = new Slider(20, 240, 160, 0.1f, 0.9f, () -> updateGuiElements(), true);
		mutateLabel = new Label(100, 225, 14, String.format("Mutation Rate: %.2f", mutateAmount));
		mutIntensitySlider = new Slider(20, 300, 160, 0.05f, 0.5f, () -> updateGuiElements(), true);
		mutIntensityLabel = new Label(100, 285, 14, String.format("Mutation Intensity: %.3f", mutIntensity));
		
		gui.addElements(new GuiElement[] {mutateSlider,mutateLabel,mutIntensitySlider,mutIntensityLabel});
	}
	
	public void start()
	{
		run = true;
		running = true;
	}
	public void stop()
	{
		run = false;
	}
	
	void changeMap()
	{
		if(running == false)
		{
			reset();
		}
	}
	
	void updateGuiElements()
	{
		runsPerFrame = (int) runsPerFrameSlider.getValue();
		runsPerFrameLabel.setLabel("Runs per Frame: " + runsPerFrame);
		generationTicks = (int) generationTicksSlider.getValue();
		generationTicksLabel.setLabel("Ticks per Generation: " + generationTicks);
		numJerries = (int) numJerriesSlider.getValue();
		numJerriesLabel.setLabel("Number of Jerries: " + numJerries);
		jerryLabel.setLabel("Jerries: " + jerries.size() + "/" + numJerries);
		generationTickLabel.setLabel("Generation Tick: " + generationTickCounter + "/" + generationTicks);
		generationNumberLabel.setLabel("Generation " + generationNumber);
		mutateAmount = mutateSlider.getValue();
		mutateLabel.setLabel(String.format("Mutation Rate: %.2f", mutateAmount));
		mutIntensity = mutIntensitySlider.getValue();
		mutIntensityLabel.setLabel(String.format("Mutation Intensity: %.3f", mutIntensity));
	}
	
	public void draw()
	{
		if(isOpener)
		{
			opener.update();
		}
		else
		{
			translate(map.gridScale/2, map.gridScale/2);
			for(int runs = 0; runs < (running ? runsPerFrame : 1); runs++)
			{
				map.update();
				if(running)
				{
					
					for(int i = jerries.size()-1; i >= 0; i--)
					{
						jerries.get(i).update(this);
						if(jerries.get(i).dead)
						{
							savedJerries.add(jerries.remove(i));
						}
					}
					
					generationTickCounter++;
					
					if(jerries.size() == 0 || generationTickCounter > generationTicks)
					{
						for(int j = jerries.size()-1; j >= 0; j--)
							savedJerries.add(jerries.remove(j));
						map.refresh();
						running = false;
						nextGeneration();
						generationTickCounter = 0;

						/** TODO: export best brain every ~~ generations **/
						return;
					}
					mapsButton.setColor(color(130));
				}
				else mapsButton.setColor(color(0, 80, 240));
				
			}
			
			translate(-map.gridScale/2, -map.gridScale/2);
			updateGuiElements();
			gui.update();
			

		}
	}
	@Override
	public void mousePressed()
	{
		if(isOpener && withinGui(opener))
			opener.pressed();
		if(!isOpener && withinGui(gui))
			gui.pressed();
	}
	@Override
	public void mouseDragged()
	{
		if(!isOpener && withinGui(gui))
			gui.dragged();
	}
	@Override
	public void mouseReleased()
	{
		if(!isOpener)
			gui.released();
	}
	
	boolean withinGui(Gui g)
	{
		return(mouseX >= g.pos.x && mouseX <= g.pos.x + g.w && mouseY >= g.pos.y && mouseY <= g.pos.y + g.h);
	}

	public PVector randomPos()
	{
		PVector pos = new PVector(floor(random(0,map.grid.size())), floor(random(0,map.grid.get(0).size())));
		if(map.tileAt(pos).terrainType == TerrainType.WATER || map.tileAt(pos).occupied || map.tileAt(pos).terrainType == TerrainType.SAND)
			return randomPos();
		return pos;
	}
	public void randomJerries()
	{
		for(int i = 0; i < numJerries; i++)
		{
			jerries.add(new Jerry(randomPos(), 20, 100, null, this, map));
		}
	}


	void nextGeneration(){
		calculateFitness();
		//getBest().brain.exportBrain("brain");
		/*if(export)
		{
			Jerry best = savedJerries.get(0);
			for(int i = 1; i < NUM_JERRIES; i++)
			{
				if(savedJerries.get(i).fitness > best.fitness)
					best = savedJerries.get(i);
			}
			best.brain.exportBrain("best_brain_" + genNum, best.fitness);
			export = false;
		} */
		jerries.clear();
		
		int eliteGroup = floor(savedJerries.size() * .9f);
		for(int i = savedJerries.size(); i > eliteGroup; i--)
		{
			Jerry j = getBest();
			jerries.add(j);
			//Jerry j2 = new Jerry(randomPos(), j.maxHealth, j.maxEnergy, j.brain, this, j.map);
			//j2.mutate(mutateAmount, mutIntensity);
			//jerries.add(j2);
		}
		
		int toGet = numJerries - jerries.size();
		
		calculateFitness();
		for (int i = 0; i < toGet; i++){
			jerries.add(pickOne());
		}
		savedJerries.clear();
		generationNumber++;
		running = true;
		//println("Generation "+ generationNumber);
	}

	Jerry pickOne(){
		int index = 0;
		double r = random(1);
		while (r > 0) {
			r -= savedJerries.get(index).fitness;
			index++;
			if(index > savedJerries.size()-1 || index == 0) index = 1;
		}
		index--;
		Jerry j = savedJerries.get(index);
		Jerry child = new Jerry(randomPos(), j.maxHealth, j.maxEnergy, j.brain, this, map);
		child.mutate(mutateAmount, mutIntensity);
		return child;
	}
	
	Jerry getBest()
	{
		double highest = 0;
		int highestIndex = 0;
		for(int i = 0; i < savedJerries.size(); i++)
		{
			if(savedJerries.get(i).fitness > highest)
			{
				highest = savedJerries.get(i).fitness;
				highestIndex = i;
			}
		}
		Jerry best = savedJerries.get(highestIndex);
		Jerry child = new Jerry(randomPos(), best.maxHealth, best.maxEnergy, best.brain, this, map);
		child.mutate(mutateAmount, mutIntensity);
		return child;
	}

	void calculateFitness(){
		double sum = 0;
		for (Jerry j : savedJerries){
			sum += j.score;
		}

		for (Jerry j : savedJerries){
			j.fitness = j.score/sum;
		}
	}
	
	int max(double[] array)
	{
		double highest = 0;
		int index = 0;
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] > highest)
			{
				highest = array[i];
				index = i;
			}
		}
		return index;
	}
}