/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.sim.gui.drawer;

import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.sim.gui.SimPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
public class TrajectoryDrawer implements IDrawer{
	private SimPanel panel;
	private List<IVector> points;
	private Color color;
	private int thickness = 2;
	public TrajectoryDrawer(SimPanel panel, List<IVector> points, Color color){
		this.panel = panel;
		this.points = points;
		this.color = color;
	}
	@Override
	
	public void draw(Graphics g) {
		// TODO Auto-generated method stub
		for (int i = 0; i < points.size() - 1; i++) {
			Point p0 = panel.getPixel(points.get(i).toCartesianVector());
			Point p1 = panel.getPixel(points.get(i + 1).toCartesianVector());
			drawThickLine(g, p0.x, p0.y, p1.x, p1.y, thickness, color);
		}
	}
	
	 public static void drawThickLine(
			  Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {
	
			  g.setColor(c);
			  int dX = x2 - x1;
			  int dY = y2 - y1;

			  double lineLength = Math.sqrt(dX * dX + dY * dY);

			  double scale = (double)(thickness) / (2 * lineLength);


			  double ddx = -scale * (double)dY;
			  double ddy = scale * (double)dX;
			  ddx += (ddx > 0) ? 0.5 : -0.5;
			  ddy += (ddy > 0) ? 0.5 : -0.5;
			  int dx = (int)ddx;
			  int dy = (int)ddy;

			  int xPoints[] = new int[4];
			  int yPoints[] = new int[4];

			  xPoints[0] = x1 + dx; yPoints[0] = y1 + dy;
			  xPoints[1] = x1 - dx; yPoints[1] = y1 - dy;
			  xPoints[2] = x2 - dx; yPoints[2] = y2 - dy;
			  xPoints[3] = x2 + dx; yPoints[3] = y2 + dy;

			  g.fillPolygon(xPoints, yPoints, 4);
	  }

	
	

}
