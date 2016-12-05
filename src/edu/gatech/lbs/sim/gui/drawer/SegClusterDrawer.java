/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Collection;
import java.util.Random;

import edu.gatech.lbs.clustering.*;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadSegmentGeometry;


public class SegClusterDrawer implements IDrawer{
	private RunClustering clustering;
	private SimPanel panel;
	public SegClusterDrawer(RunClustering clustering, SimPanel panel) {
		    this.clustering = clustering;
		    this.panel = panel;
		  }
	@Override

	public void draw(Graphics g){
		Random numGen = new Random();
		List<SegCluster> resultList = clustering.getSegClus();
		
		for (SegCluster mySc : resultList){
			g.setColor(getRandomColor(numGen));
			for (PointsOnSeg pos : mySc.getSegments()){

				RoadSegment seg = pos.getRoadSeg(clustering.getRoadmap());
				IDrawer segDrawer =  new SegmentDrawer(panel, seg, g.getColor());
				segDrawer.draw(g);
			}
		}
	}
	Color getRandomColor(Random numGen) {
		
		return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
		}

}
