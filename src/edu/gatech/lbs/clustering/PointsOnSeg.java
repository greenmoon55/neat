/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.world.roadnet.*;

import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;

public class PointsOnSeg {
	protected int segid;

	protected List<IVector> v;
	protected List<Integer> trajIdList;
	public PointsOnSeg(int id){
		segid = id;
		v = new LinkedList<IVector>();
		trajIdList = new ArrayList<Integer>();
	}
	public List<Integer> getTrajIdList() {
		return trajIdList;
	}
	public void setTrajIdList(List<Integer> trajIdList) {
		this.trajIdList = trajIdList;
	}
	public int getSegid() {
		return segid;
	}
	public void setSegid(int segid) {
		this.segid = segid;
	}
	public List<IVector> getV() {
		return v;
	}
	public void setV(List<IVector> v) {
		this.v = v;
	}
	public int numPoints(){
		return v.size();
	}
	public int numTrajs(){
		return trajIdList.size();
	}
	
	public float getSpeedLimit(RoadMap rm){
		return this.getRoadSeg(rm).getSpeedLimit();
	}
	public RoadSegment getRoadSeg(RoadMap roadmap){
		return roadmap.getRoadSegment(segid);
	}
	public boolean isNeighbor(RoadMap roadmap, PointsOnSeg otherSeg){
		RoadSegment rs1= this.getRoadSeg(roadmap);
		RoadSegment rs2= otherSeg.getRoadSeg(roadmap);
		if ((rs1.getJunctionIndex(rs2.getSourceJunction())!=-1)||
				(rs1.getJunctionIndex(rs2.getTargetJunction())!=-1))
		return true;
		else return false;
	}
	public IVector getSharedJunction(RoadMap roadmap,PointsOnSeg neighborSeg) {
		RoadSegment rs1= this.getRoadSeg(roadmap);
		RoadSegment rs2= neighborSeg.getRoadSeg(roadmap);
		if (rs1.getJunctionIndex(rs2.getSourceJunction())!=-1) {
			return rs2.getSourceLocation();
		}
		else return rs2.getTargetLocation();
	}
	public boolean isEndPoint(RoadMap rm, RoadJunction junc){
		RoadSegment rs = this.getRoadSeg(rm);
		if (rs == null) {
			System.out.println("wtf");
		}
		return (rs.getJunctionIndex(junc)!=-1);
			
	}
	public void add(PointsOnSeg pos){
		if (this.getSegid()==pos.getSegid()){
		for (IVector coor : pos.getV()){
			this.getV().add(coor);
		}
		}
	}


	
	public List<Integer> getIntersectTrajs(PointsOnSeg pos){
		  Hashtable firstIdList = new Hashtable();
		  List<Integer> result = new LinkedList<Integer>(); 
		  for (int i=0; i<this.getTrajIdList().size();i++){
		  firstIdList.put(this.getTrajIdList().get(i),"true");
		   }
		  for (int j=0; j<pos.getTrajIdList().size();j++){
		    int idj = pos.getTrajIdList().get(j);
		    if (firstIdList.containsKey(idj)) result.add(idj); 
		  }
		  return result;
		 }
	public int getNumOfSharedTrajs(PointsOnSeg pos){
		if (!getIntersectTrajs(pos).isEmpty()) return getIntersectTrajs(pos).size();
		else return 0;
		
	}
	
	public double distToOtherPos(RoadMap rm, PointsOnSeg otherPos){
		RoadSegment rs1 = this.getRoadSeg(rm);
		RoadSegment rs2 = otherPos.getRoadSeg(rm);
		double dss = rm.getShortestRoute(rs1.getSourceJunction().getRoadnetLocation()
				, rs2.getSourceJunction().getRoadnetLocation()).getLength();
		double dse = rm.getShortestRoute(rs1.getSourceJunction().getRoadnetLocation()
				, rs2.getTargetJunction().getRoadnetLocation()).getLength();
		double des = rm.getShortestRoute(rs1.getTargetJunction().getRoadnetLocation()
				, rs2.getSourceJunction().getRoadnetLocation()).getLength();
		double dee = rm.getShortestRoute(rs1.getTargetJunction().getRoadnetLocation()
				, rs2.getTargetJunction().getRoadnetLocation()).getLength();
		return Math.max(Math.min(dss, dse), Math.min(des, dee));
	}
}
