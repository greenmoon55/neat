package edu.gatech.lbs.clustering;
import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;
import edu.gatech.lbs.sim.gui.drawer.RoadMapDrawer;
import edu.gatech.lbs.sim.gui.drawer.TraClusDrawer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TraClusResult {
	protected Collection<List<IVector>> traClusters;
	protected HashMap<Integer,List<Integer>> trajIdClus;
	protected HashMap<Integer,List<Integer>> segIdClus;
	protected Simulation sim;
		
	public Collection<List<IVector>> getTraClusters() {
		return traClusters;
	}
	public TraClusResult(String configFilename, String trace){
		sim = new Simulation();
		sim.loadConfiguration(configFilename);
		ImportData im =  new ImportData();
		this.traClusters = im.loadTraClusRe(trace);
		this.trajIdClus = im.getTrajIdClus();
		this.segIdClus = im.getSegIdClus();
	}
	
	public void drawSegClusters(){
		  try{
		  SimPanel panel = SimPanel.makeGui(this.sim);
		  List<IDrawer> drawers = new ArrayList<IDrawer>();
		    drawers.add(new RoadMapDrawer(this.sim, panel));
		    drawers.add(new TraClusDrawer(this, panel));
		    panel.setDrawers(drawers);
		    panel.redrawSim();
		    panel.repaint();
		  } catch (Exception e) {
	        System.out.println("No GUI.");
	      }
		    
	  }
	public HashMap<Integer, List<Integer>> getTrajIdClus() {
		return trajIdClus;
	}
	public double traClusLength(RoadMap rm, List<Integer> segIdSet){
		double sum=0;
		for (int i=0; i<segIdSet.size();i++){
			sum += (double)rm.getRoadSegment(segIdSet.get(i)).getLength();
		}
		return sum;
	}
	public void clusterValidity(){
		RoadMap rm = (RoadMap)this.sim.getWorld();
		int n = segIdClus.size();
		double[] lengths = new double[n];
		double avgLength = 0;
		
		for (int i=0; i<n; i++){
			lengths[i] = traClusLength(rm, segIdClus.get(i));
			avgLength += lengths[i];
			if (i==0) for (int j=0; j<segIdClus.get(i).size(); j++){System.out.println(segIdClus.get(i).get(j) +" ");}
		}
		avgLength = avgLength/n;
		Arrays.sort(lengths);
		System.out.println(n);
		System.out.println("Average length of traclus representatives: " + avgLength );
		System.out.println("minimum length is: "+lengths[0]);
		System.out.println("maximum length is: "+lengths[n-1]);
		
	}
	/**
	 * @param arg
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TraClusResult tcr = new TraClusResult(args[0], args[1]);
		tcr.drawSegClusters();
	}

}
