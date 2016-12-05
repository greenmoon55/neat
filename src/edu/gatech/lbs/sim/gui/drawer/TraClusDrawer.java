package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import edu.gatech.lbs.clustering.TraClusResult;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.gui.SimPanel;

public class TraClusDrawer implements IDrawer {
	private TraClusResult traClus;
	private SimPanel panel;
	public TraClusDrawer(TraClusResult traClus, SimPanel panel) {
		    this.traClus = traClus;
		    this.panel = panel;
		  }
	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub
		Random numGen = new Random();
		int count =1;
		Collection<List<IVector>> traClusters = traClus.getTraClusters();
		g.setFont(new Font(null,Font.BOLD, 12));
		g.drawString("Number of clusters: "+ traClusters.size(), 20, 40);
		for (List<IVector> clus : traClusters){
			g.setColor(getRandomColor(numGen));
			drawClus(g, clus,g.getColor(),2);
			//draw cluster's number
			Point p = panel.getPixel(clus.get(numGen.nextInt(clus.size())).toCartesianVector());
			g.setColor(new Color(160, 0, 211));
			count++;
		}
	}
	
	Color getRandomColor(Random numGen) {
		
		return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
		}
// draw each line segment in a cluster of line segments	
	public void drawClus(Graphics g, List<IVector> points, Color color, int thickness){
		for (int i = 0; i < points.size() - 1; i++) {
			if (i%2==1)continue;
			Point p0 = panel.getPixel(points.get(i).toCartesianVector());
			Point p1 = panel.getPixel(points.get(i + 1).toCartesianVector());
			drawThickLine(g, p0.x, p0.y, p1.x, p1.y, thickness, color);
		}
		
	}
	public void drawThickLine(
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
