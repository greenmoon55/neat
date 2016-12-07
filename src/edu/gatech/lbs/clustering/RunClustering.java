/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;

import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.sim.gui.drawer.BaseClusterDrawer;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;
import edu.gatech.lbs.sim.gui.drawer.RoadMapDrawer;
import edu.gatech.lbs.sim.gui.drawer.SegClusterDrawer;
import edu.gatech.lbs.sim.gui.drawer.MergedClustersDrawer;
import edu.gatech.lbs.sim.gui.drawer.SegmentDrawer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RunClustering {
	protected Collection<Trajectory> trajs;
	protected HashMap<Integer,PointsOnSeg> segments;//input - list of set of t-fragments PointsOnSegments (base clusters)
	protected List<SegCluster> segClus;//output - set of flow clusters 
	protected Simulation sim;
	protected RoadMap roadmap;
	protected MergedClusters mClus;
	protected static int numSegsThres = 5;
	protected static int numTrajsThres = 5;
	protected double startTime;
	protected double totalTime;
	protected double eps;
	protected int neatMode;
	protected int base;
	protected int flow;
	protected int concateCount;// for testing
	
	public RunClustering(String configFilename, String traceFile, String neatMode, String eps){
		this.neatMode=Integer.parseInt(neatMode);
		if (this.neatMode>3){
			TraClusResult tcr = new TraClusResult(configFilename, traceFile);
			tcr.drawSegClusters();
		}
		else {
		sim = new Simulation();
		sim.loadConfiguration(configFilename);

	    startTime = System.nanoTime();
		
		this.segClus = new LinkedList<SegCluster>();
		this.roadmap = (RoadMap) sim.getWorld();
		this.neatMode=Integer.parseInt(neatMode);
		this.eps=Double.parseDouble(eps);
		this.concateCount =0;
		if (this.neatMode <4){
			ImportData im = new ImportData();
			this.trajs = im.LoadTrajectories(traceFile, roadmap);
			
			for (Trajectory myTraj : trajs){
				myTraj.generateRoute();
				
			}
			
			generateSegments();
		}
		}
	}
	public double getTotalTime(){
		return totalTime;
	}
	public int getNeatMode() {
		return neatMode;
	}

	public int getBase() {
		return base;
	}

	public int getFlow() {
		return flow;
	}

	public Collection<Trajectory> getTrajs() {
		return trajs;
	}

	public void drawSegClusters(){
		  try{
		  SimPanel panel = SimPanel.makeGui(this.sim);
		  List<IDrawer> drawers = new ArrayList<IDrawer>();
		    drawers.add(new RoadMapDrawer(this.sim, panel));
		    drawers.add(new SegClusterDrawer(this, panel));
		    panel.setDrawers(drawers);
		    panel.redrawSim();
		    panel.repaint();
		  } catch (Exception e) {
	        System.out.println("No GUI.");
	      }
		    
	  }
	
	public void drawMergedClusters(){
		try{
			  SimPanel panel = SimPanel.makeGui(this.sim);
			  List<IDrawer> drawers = new ArrayList<IDrawer>();
			    drawers.add(new RoadMapDrawer(this.sim, panel));
			    drawers.add(new MergedClustersDrawer(this, panel));
			    panel.setDrawers(drawers);
			    panel.redrawSim();
			    panel.repaint();
			  } catch (Exception e) {
		        System.out.println("No GUI.");
		      }
	
	}
	
	public RoadMap getRoadmap() {
		return roadmap;
	}

	
	public MergedClusters getMClus() {
		return mClus;
	}
	public List<SegCluster> getSegClus() {
		return segClus;
	}
	public Collection<PointsOnSeg> getSegments() {
		return segments.values();
	}
	
	public double getEps() {
		return eps;
	}
	public void setEps(double eps) {
		this.eps = eps;
	}
	public PointsOnSeg getDensestSeg(Collection<PointsOnSeg> posList){
		int startSeg=0;
		int maxDense = 0;
		for (PointsOnSeg pos : posList){
			
			if (pos.numPoints()>maxDense){
				startSeg = pos.getSegid();
				maxDense = pos.numPoints();
			}
			
		}
		
		return segments.get(startSeg);
	}
	public void execute(){
		this.base = this.getSegments().size();
		if (this.neatMode==1) {
			this.totalTime = (System.nanoTime()-startTime)/1e6;
			drawBaseClusters();
			
		} else {

            while (!this.segments.isEmpty()) mergeSegments();//flow-based clustering


            System.out.println("Number of clusters bf: " + this.segClus.size());
            int avgNumOfTrajs = 0;
            for (int i = this.segClus.size() - 1; i >= 0; i--) {
                segClus.get(i).generateTrajList();
                avgNumOfTrajs += segClus.get(i).getTrajList().size();
            }
            avgNumOfTrajs = Math.round(avgNumOfTrajs / this.segClus.size());

            //Filtering clusters which has too few objects traveling on
            for (int i = this.segClus.size() - 1; i >= 0; i--) {
                if (segClus.get(i).getTrajList().size() < avgNumOfTrajs)
                    segClus.remove(i);
            }
            this.totalTime = (System.nanoTime() - startTime) / 1e6;
            this.flow = segClus.size();
            System.out.println("The first phase takes " + (System.nanoTime() - startTime) / 1e6 + " milisecs");
            System.out.println("Number of trajs: " + trajs.size());
            System.out.println("Number of clusters after: " + this.segClus.size());

            optClusteringResult(eps);//final phase
            drawMergedClusters();
        }
	}
	
	public void optClusteringResult(double eps){
		//MERGE PHASE
		//sort segcluster by length in ascending order
		Collections.sort(this.segClus,new SegClusComparator(roadmap));
		
		double avgFlowCompute =0;
		for (int i=0; i<this.segClus.size();i++){
			
			avgFlowCompute += this.segClus.get(i).getFlowComputeCount();
		}

		
		this.mClus = new MergedClusters(this.segClus,roadmap,neatMode,eps);

		this.totalTime = (System.nanoTime()-startTime)/1e6;

	}
	public double getStartTime() {
		return startTime;
	}
	public void generateBaseClusters(){
		
		for (PointsOnSeg pos: segments.values()){
			SegCluster mySegClus = new SegCluster();
			mySegClus.addSeg(pos);
			mySegClus.setStartJunc(pos.getRoadSeg(roadmap).getSourceJunction());
			mySegClus.setEndJunc(pos.getRoadSeg(roadmap).getTargetJunction());
			segClus.add(mySegClus);

		}
		optClusteringResult(eps);
	}
	public void mergeSegments(){
		//start with the densest segment which has the maximum number of trajectories
		
		SegCluster mySegClus = new SegCluster();
		PointsOnSeg ps0 = getDensestSeg(getSegments());
		mySegClus.addSeg(ps0);
		
		mySegClus.setStartJunc(ps0.getRoadSeg(roadmap).getSourceJunction());
		mySegClus.setEndJunc(ps0.getRoadSeg(roadmap).getTargetJunction());
		segments.remove(ps0.getSegid());
		while (mySegClus.isExtensible()){
            PointsOnSeg[] ps = mySegClus.concatenateJuncs(roadmap, segments.values(),1,0,0);
            this.concateCount++;
            if (ps[0]!=null) {
                segments.remove(ps[0].getSegid());
            }
            if (ps[1]!=null)segments.remove(ps[1].getSegid());
		}
		// mySegClus.print();
		segClus.add(mySegClus);
	}

	public void mergeSegmentsDensityBased(double eps){
		ArrayList<PointsOnSeg> segValues = new ArrayList<PointsOnSeg>(getSegments());
		ArrayList<PointsOnSeg> cloneSegments = (ArrayList<PointsOnSeg>)segValues.clone();
		while (!cloneSegments.isEmpty()){
		SegCluster mySegClus = new SegCluster();
		
		PointsOnSeg ps0 = cloneSegments.get(cloneSegments.size()-1);
		mySegClus.addSeg(ps0);
		cloneSegments.remove(cloneSegments.size()-1);
		for (int i=0; i<mySegClus.getSize();i++){
			PointsOnSeg myPos = mySegClus.getSegments().get(i);
			for (int j=cloneSegments.size()-1; j>0; j--){
				PointsOnSeg otherPos = cloneSegments.get(j);
				if (myPos.distToOtherPos(roadmap, otherPos)<eps){
					mySegClus.addSeg(otherPos);
					cloneSegments.remove(j);
				}
			}
		}
		segClus.add(mySegClus);
		
		
		}
		
	}
	
	public List<Integer> getEpsNeighbors(int iCheck, double eps, HashMap<Integer,ArrayList<Double>> distList){
		int n = distList.size();
		if (iCheck>=n) return null;
		List<Integer> neighborIndex = new ArrayList<Integer>();
		if (iCheck<(n-1)){
			for (int k=0;k<distList.get(iCheck).size();k++){// check entry of iCheck in the distList
				if (distList.get(iCheck).get(k)<eps){
					neighborIndex.add(iCheck+k+1);
				}
			}
		}
		for (int j=iCheck-1; j>0; j--){//check other entry that has distance to iCheck		
			if (distList.get(j).get(iCheck-j-1)<eps){
				neighborIndex.add(j);
			}
		}
		return neighborIndex;
	}
	public void clusterSegmentsDBScan(double eps,HashMap<Integer,ArrayList<Double>> distList){
		
		ArrayList<PointsOnSeg> segValues = new ArrayList<PointsOnSeg>(getSegments());
		
		int n = segValues.size();
		int[] classified = new int[n];
		for (int i=0; i<n;i++) classified[i]=0;//initially all the base clusters are unclassified
		for (int i=0; i<n;i++){
			if (classified[i]==0){
			ArrayList<Integer> queue = new ArrayList<Integer>();//queue to expand the cluster
			SegCluster mySegClus = new SegCluster();
			mySegClus.addSeg(segValues.get(i));
			classified[i]=1;
			List<Integer> epsNeighbors = getEpsNeighbors(i, eps, distList);
			
			for (Integer eNeighbor : epsNeighbors){queue.add(eNeighbor);}
			while(!queue.isEmpty()){
				int first = queue.get(0);
				if (classified[first]==0){
					mySegClus.addSeg(segValues.get(first));
					classified[first]=1;
					queue.remove(0);
					List<Integer> eN = getEpsNeighbors(first, eps, distList);
					if(!eN.isEmpty()){
					for (Integer eNeighbor : eN){
						queue.add(eNeighbor);}
					}
				} else queue.remove(0);
							
			}
			segClus.add(mySegClus);
			}
			
		}
		
	}
	
	public double calculateEps(){
		double sum = 0;
		double minDist = Double.MAX_VALUE;
		double maxDist = 0;
		ArrayList<PointsOnSeg> segValues = new ArrayList<PointsOnSeg>(getSegments());
		 for (int i=0; i<this.segments.size()-1; i++)
        {
			 
            for (int j = i+1; j<this.segments.size(); j++)
            {
           	 double dist = segValues.get(i).distToOtherPos(this.roadmap, segValues.get(j));
           	 
           	 if (dist<minDist) minDist = dist;
           	 if (dist>maxDist) maxDist = dist;
           	 
                sum += dist;
                
            }
        }
		 double avgDist = sum/((segValues.size()-1)*(segValues.size()-2)/2);
		 
		 System.out.println("The average distance is: "+avgDist+ "; The minimum distance is: "+minDist
				 +"; The max distance is: "+maxDist);
		 return avgDist;
	}

	// return the adjacent list of distance between each pair of base clusters and calculate Eps threshold
	public HashMap<Integer,ArrayList<Double>> calculateDistList(List<PointsOnSeg> posList){
		HashMap<Integer,ArrayList<Double>> distanceList = new HashMap<Integer,ArrayList<Double>>();
		
		double sum = 0;
		double minDist = Double.MAX_VALUE;
		double maxDist = 0;
		
		int n = posList.size();
		for (int i=0;i<n-1;i++){
			PointsOnSeg myPos = posList.get(i);
			ArrayList<Double> tmpList = new ArrayList<Double>();
			for (int j=i+1; j<n;j++){
				double dist = myPos.distToOtherPos(getRoadmap(), posList.get(j));
				tmpList.add(dist);
				
				if (dist<minDist) minDist = dist;
	           	if (dist>maxDist) maxDist = dist;
				sum += dist;
				
			}
			distanceList.put(i, tmpList);
		}
		double avgDist = sum/((n-1)*(n-2)/2);
		 
		System.out.println("The average distance is: "+avgDist+ "; The minimum distance is: "+minDist
				 +"; The max distance is: "+maxDist);
		setEps(Math.round(avgDist/2));
		return distanceList;
	}
	
	public void generateSegments(){

		segments = new HashMap<Integer, PointsOnSeg>();
		for (Trajectory myTraj : trajs){
			for (PointsOnSeg pos : myTraj.getRoute()){
				if (!segments.containsKey(pos.getSegid())) {
					segments.put(pos.getSegid(), pos);
				} else {
					segments.get(pos.getSegid()).add(pos);//add another t-frag to the base cluster
				}
				if (!segments.get(pos.getSegid()).getTrajIdList().contains(myTraj.getId())){
					segments.get(pos.getSegid()).getTrajIdList().add(myTraj.getId());
				}
			}
			
		}

	}


	public void baseClustersSummary(){
		System.out.println("The base cluster formation takes "+(System.nanoTime()-startTime)/1e6+" milisecs");
		System.out.println("Number of original base clusters: "+this.getSegments().size());
		
	}
	public void drawBaseClusters(){
		try{
			  SimPanel panel = SimPanel.makeGui(this.sim);
			  List<IDrawer> drawers = new ArrayList<IDrawer>();
			    drawers.add(new RoadMapDrawer(this.sim, panel));
			    drawers.add(new BaseClusterDrawer(this, panel));
			    panel.setDrawers(drawers);
			    panel.redrawSim();
			    panel.repaint();
			  } catch (Exception e) {
		        System.out.println("No GUI.");
		      }
	}


	public Collection<List<IVector>> representativeRoutes(List<SegCluster> segClus, RoadMap rm){
		Collection<List<IVector>> repRoutes = new LinkedList<List<IVector>>();
		for (int i=0; i<segClus.size();i++){
			
			repRoutes.add(segClus.get(i).representativePoints(rm));
		}
		return repRoutes;
	}
	
	 
	/**
	 * @param args
	 * path-to-config-file path-to-dataset neatmode [distance threshold])
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		RunClustering rc = new RunClustering(args[0],args[1],args[2],args[3]);

		rc.execute();
		
		
	}

}
