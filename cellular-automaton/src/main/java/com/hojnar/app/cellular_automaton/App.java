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
	public static void main(String[] args)  
	{
		SimpleBrain b = new SimpleBrain(2, 2, 1);
		b.predict(new double[] {1, 0});
		b.exportBrain("brain");
		//b.getFirstMatrix().print();
		//b.getSecondMatrix().print();
		
		SimpleBrain b2 = SimpleBrain.newFromFile("brain");
		//b2.getFirstMatrix().print();
	}

}
