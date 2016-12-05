/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;
import edu.gatech.lbs.core.vector.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Trajectory {
	protected int id;
	protected LinkedList<Point> Points;
	protected LinkedList<PointsOnSeg> Route;// list of t-fragments
	public Trajectory(int id){
		this.id=id;
		this.Points = new LinkedList<Point>();
		this.Route = new LinkedList<PointsOnSeg>();
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public LinkedList<Point> getPoints() {
		return Points;
	}
	public void setPoints(LinkedList<Point> points) {
		Points = points;
	}
	public LinkedList<PointsOnSeg> getRoute() {
		return Route;
	}
	public void setRoute(LinkedList<PointsOnSeg> route) {
		Route = route;
	}
	
	public void generateRoute(){
		PointsOnSeg ps0 = new PointsOnSeg(Points.get(0).getSegid());
		
		ps0.getV().add(Points.get(0).getV());
		Route.add(ps0);
		for (int i=1;i<Points.size();i++){
			if (Points.get(i).getSegid()!=Points.get(i-1).getSegid()){
				PointsOnSeg ps1 = new PointsOnSeg(Points.get(i).getSegid());
				
				ps1.getV().add(Points.get(i).getV());
				Route.add(ps1);
			}
			else{
				if ((i<Points.size()-1)&&(Points.get(i).getSegid()!=Points.get(i+1).getSegid())) 
				Route.getLast().getV().add(Points.get(i).getV());
				if (i==Points.size()-1)Route.getLast().getV().add(Points.get(i).getV()); 
			}
		}
	}
	public List<Integer> generateSegIds(){
		List<Integer> segIds = new ArrayList<Integer>();
		for(PointsOnSeg ps : Route){
			segIds.add(ps.getSegid());
		}
		return segIds;
	}

}
