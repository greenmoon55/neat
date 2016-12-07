/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;
import java.util.Random;

import edu.gatech.lbs.clustering.*;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.core.vector.*;

public class MergedClustersDrawer implements IDrawer {

	private RunClustering clustering;
	private SimPanel panel;
	public MergedClustersDrawer(RunClustering clustering, SimPanel panel) {
		    this.clustering = clustering;
		    this.panel = panel;
		  }
	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub
		Random numGen = new Random();
		int count=0;
		boolean isIndex = true;//sua
		ArrayList<Color> colorSet = new ArrayList<Color>();
		colorSet.add(Color.gray);
		colorSet.add(Color.DARK_GRAY);
		colorSet.add(Color.LIGHT_GRAY);
		g.setFont(new Font(null,Font.BOLD, 12));
		drawInputPoints(g);
		Collection<List<SegCluster>> mClus = this.clustering.getMClus().getResultSegClus().values();
		g.drawString("Clustering: "+this.clustering.getTrajs().size()+ " trajectories", 20, 20);
		if (this.clustering.getNeatMode()==3)
			g.drawString("# base, flow, final clusters: "+ clustering.getBase()+ ", "+clustering.getFlow()+ ", "+ mClus.size(), 20, 40);
		else if  (this.clustering.getNeatMode()==2)
			g.drawString("# base, flow clusters: "+ clustering.getBase()+ ", "+clustering.getFlow(), 20, 40);
		else
			g.drawString("# base clusters: "+ clustering.getBase(), 20, 40);
		g.drawString("Running time (milisecs): "+(double)Math.round(this.clustering.getTotalTime()*1000)/1000, 20, 60);
		for (List<SegCluster> myList : mClus){
			
			do	
				{g.setColor(getRandomColor(numGen));}
			while(colorSet.contains(g.getColor()));
			colorSet.add(g.getColor());
			Collection<List<IVector>> trajs = clustering.representativeRoutes(myList, clustering.getRoadmap());
			for (List<IVector> traj : trajs){
						
				IDrawer trajDrawer = new TrajectoryDrawer(panel,traj,colorSet.get(colorSet.size()-1));
				trajDrawer.draw(g);
				if (!isIndex){
					Point p = panel.getPixel(traj.get(numGen.nextInt(traj.size())).toCartesianVector());
					g.setColor(Color.black);
					g.drawString(Integer.toString(count), (int)(p.getX()), (int)p.getY());
					isIndex = true;
				}
			}
			isIndex = true;//change
			count++;
		}
		
	}
	public void drawInputPoints(Graphics g){
		g.setColor(Color.DARK_GRAY);
		Collection<Trajectory> trajs = this.clustering.getTrajs();
		for (Trajectory myTraj:trajs){
			LinkedList<edu.gatech.lbs.clustering.Point> pointLs = myTraj.getPoints();
			for (int i=0; i<pointLs.size()-1;i++){
				Point p0 = panel.getPixel(pointLs.get(i).getV().toCartesianVector());
				g.fillOval(p0.x - 1, p0.y - 1, 3, 3);
			}
		}
	}
	Color getRandomColor(Random numGen) {
		
		return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
		}


}
