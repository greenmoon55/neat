/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Collection;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.core.vector.*;

public class TrajectoriesDrawer implements IDrawer{
	private Simulation sim;
	private SimPanel panel;
	public TrajectoriesDrawer(Simulation sim, SimPanel panel) {
		    this.sim = sim;
		    this.panel = panel;
		  }
	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Color.green);
		
		Collection<List<IVector>> trajs = sim.getTrajectories().values();
		for (List<IVector> traj : trajs){
			IDrawer trajDrawer = new TrajectoryDrawer(panel,traj,g.getColor());
			trajDrawer.draw(g);
		}
		
	}

}
