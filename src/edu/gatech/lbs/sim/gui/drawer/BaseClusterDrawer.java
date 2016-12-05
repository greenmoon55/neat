/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.gatech.lbs.clustering.PointsOnSeg;
import edu.gatech.lbs.clustering.RunClustering;
import edu.gatech.lbs.clustering.SegCluster;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.gui.SimPanel;

public class BaseClusterDrawer implements IDrawer{

	private RunClustering clustering;
	private SimPanel panel;
	public BaseClusterDrawer(RunClustering clustering, SimPanel panel) {
		    this.clustering = clustering;
		    this.panel = panel;
		  }
	@Override
	public void draw(Graphics g){
		Random numGen = new Random();
		ArrayList<PointsOnSeg> segList = new ArrayList(clustering.getSegments());

		g.setFont(new Font(null,Font.BOLD, 12));
		g.drawString("Clustering: "+this.clustering.getTrajs().size()+ " trajectories", 20, 20);
		
		g.drawString("Number of base clusters: "+ clustering.getBase(), 20, 40);
		g.drawString("Running time (milisecs): "+(double)Math.round(this.clustering.getTotalTime()*1000)/1000, 20, 60);
			for (PointsOnSeg pos : segList){
				
				g.setColor(getRandomColor(numGen));

				RoadSegment seg = pos.getRoadSeg(clustering.getRoadmap());

				IDrawer segDrawer =  new SegmentDrawer(panel, seg, g.getColor());
				segDrawer.draw(g);
			}

		
	}
	Color getRandomColor(Random numGen) {
		
		return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
		}

}

