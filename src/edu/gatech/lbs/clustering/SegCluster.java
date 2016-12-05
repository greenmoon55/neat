/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;
import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.core.world.roadnet.*;
import edu.gatech.lbs.core.world.roadnet.route.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
public class SegCluster {
	protected LinkedList<PointsOnSeg> segments;// flow clusters
	protected RoadJunction startJunc;
	protected RoadJunction endJunc;
	protected boolean extendStart;
	protected boolean extendEnd;
	protected List<Integer> TrajIdList;
	protected int flowComputeCount; 
	public SegCluster(){
		this.segments = new LinkedList<PointsOnSeg>();
		this.TrajIdList = new ArrayList<Integer>();
		this.extendEnd = true;
		this.extendStart = true;
		this.flowComputeCount =0;
	}

	public int getSize(){
		return segments.size();
	}
	public int getFlowComputeCount(){
		return this.flowComputeCount;
	}
	
	public List<Integer> getTrajList(){
		
		return TrajIdList;
	}
	public void generateTrajList(){
		
		for (int i=0; i<segments.size(); i++){
			List<Integer> tl = segments.get(i).getTrajIdList();
			for (int j=0; j<tl.size(); j++){
				if (!TrajIdList.contains(tl.get(j))){
					TrajIdList.add(tl.get(j));
				}
			}
		}
	}
	public LinkedList<PointsOnSeg> getSegments() {
		return segments;
	}


	public void setSegments(LinkedList<PointsOnSeg> segments) {
		this.segments = segments;
	}


	public RoadJunction getStartJunc() {
		return startJunc;
	}

	public void setStartJunc(RoadJunction startJunc) {
		this.startJunc = startJunc;
	}

	public RoadJunction getEndJunc() {
		return endJunc;
	}

	public void setEndJunc(RoadJunction endJunc) {
		this.endJunc = endJunc;
	}

	public void addSeg(PointsOnSeg pos){
		this.segments.add(pos);
	}
	public List<PointsOnSeg> candidatesToAdd(RoadJunction junc, RoadMap rm, Collection<PointsOnSeg> posList){
		List<PointsOnSeg> cands =  new ArrayList<PointsOnSeg>();
		for (PointsOnSeg pos : posList){
			if (pos.isEndPoint(rm, startJunc)){
				cands.add(pos);
			}
		}
		return cands;
		
	}
	public PointsOnSeg maxDense(List<PointsOnSeg> posList){//return the densest base cluster among the f-neighbors to join
		int maxDense = 0;
		int iDensest=0;
		for (int i=0; i<posList.size();i++){
			if (posList.get(i).numTrajs()>maxDense){
				iDensest =i;
				maxDense = posList.get(i).numTrajs();
			}
		}
		return posList.get(iDensest);
	}
	
	public PointsOnSeg chooseMergeNeighbor(RoadMap rm, PointsOnSeg endRouteSeg, List<PointsOnSeg> fNeighborhood, 
			double wq, double wk, double wv){
		double maxSelectivity = 0;
		int chosenIndex=0;
		int fNeighborhoodSize = fNeighborhood.size();
		int[] denominators = {0,0,0};
		int[] zeroDe = {1,1,1};
		int[][] neighborAbsWeights = new int[fNeighborhoodSize][3];// stores the 3 absolute weights q, k, v of each f-neighbor
		double[] neighborRelWeights = new double [fNeighborhoodSize];// store the relative weights
		for (int i=0; i<fNeighborhoodSize; i++){
			neighborAbsWeights[i][0]= endRouteSeg.getNumOfSharedTrajs(fNeighborhood.get(i));
			neighborAbsWeights[i][1]= fNeighborhood.get(i).numPoints();
			neighborAbsWeights[i][2]= (int) fNeighborhood.get(i).getSpeedLimit(rm);	
			for (int j=0; j<3;j++){
				denominators[j]+=neighborAbsWeights[i][j];
			}
		}
		for (int j=0; j<3;j++){
			if (denominators[j]==0){zeroDe[j] = 0;
			System.out.println("One of the denominators is zero"+j);
			}
		}

		if (zeroDe[0]+zeroDe[1]+zeroDe[2]<3) {
			//System.out.println("One of the denominators is zero");
			return null;
		} else {
		for (int i=0; i<fNeighborhoodSize; i++){
			neighborRelWeights[i] = wq*neighborAbsWeights[i][0]/denominators[0]+wk*neighborAbsWeights[i][1]/denominators[1]
			                                   +wv*neighborAbsWeights[i][2]/denominators[2];
			if (maxSelectivity <neighborRelWeights[i]){ 
				maxSelectivity = neighborRelWeights[i];
				chosenIndex = i;
			}
		}
		}
		return fNeighborhood.get(chosenIndex);
	}
	public PointsOnSeg[] concatenateJuncs(RoadMap rm, Collection<PointsOnSeg> posList, double wq, double wk, double wv){
		int countTrajs0 = 0;
		int countTrajs1 = 0;
		PointsOnSeg[] ps = new PointsOnSeg[2];
		List<PointsOnSeg> candFront = new LinkedList<PointsOnSeg>();
		List<PointsOnSeg> candEnd = new LinkedList<PointsOnSeg>();
		for (PointsOnSeg pos : posList){
			if (extendStart){
			if (pos.isEndPoint(rm, startJunc)){

				/*if (pos.numTrajs()>countTrajs0){//chosen by road density
					countTrajs0 = pos.numTrajs();
					ps[0] = pos;
				}*/
				int flow =segments.getFirst().getNumOfSharedTrajs(pos);
				this.flowComputeCount++;

				if (flow>0)candFront.add(pos);//pos is an f-neighbor of startJunc
				if (flow>countTrajs0){//calculate maximum flow
					countTrajs0 = flow;
					ps[0] = pos;
				}
//				if (pos.getSpeedLimit(rm)>countTrajs0){//chosen by road speed limit
//					countTrajs0 = (int)pos.getSpeedLimit(rm);
//					ps[0] = pos;
//				}
			}
			}
			
			if (extendEnd){
				if (pos.isEndPoint(rm, endJunc)){
					
					/*if (pos.numTrajs()>countTrajs1){
						countTrajs1 = pos.numTrajs();
						ps[1] = pos;
					}*/
					int flow =segments.getLast().getNumOfSharedTrajs(pos);
					this.flowComputeCount++;
					
					if (flow>0)candEnd.add(pos);//pos is an f-neighbor of endJunc
					if (flow>countTrajs1){
						countTrajs1 = flow;
						ps[1] = pos;
					}
					
//					if (pos.getSpeedLimit(rm)>countTrajs1){
//						countTrajs1 = (int)pos.getSpeedLimit(rm);
//						ps[1] = pos;
//					}
				}
			}
		}

		
		//use factors 
		
//		if (candFront.size()!=0) ps[0] = chooseMergeNeighbor(rm, segments.getFirst(), candFront, wq, wk, wv);
//		if (candEnd.size()!=0) ps[1] = chooseMergeNeighbor(rm, segments.getLast(), candEnd, wq, wk, wv);
		
		
		
		if (ps[0]==null) extendStart = false;
		else {this.segments.addFirst(ps[0]);
		this.startJunc = (ps[0].getRoadSeg(rm).getSourceJunction()==this.startJunc)? 
				ps[0].getRoadSeg(rm).getTargetJunction():ps[0].getRoadSeg(rm).getSourceJunction();
		}
		if (ps[1]==null) extendEnd = false;
		else {
			this.segments.addLast(ps[1]);
			this.endJunc = (ps[1].getRoadSeg(rm).getSourceJunction()==this.endJunc)? 
			ps[1].getRoadSeg(rm).getTargetJunction():ps[1].getRoadSeg(rm).getSourceJunction();
		}
		return ps;
	}

// returns the list of points on the segments of the routes from first to last 	
	public List<IVector> representativePoints(RoadMap rm){
		List<IVector> repPoints = new LinkedList<IVector>();
		for (int i=0;i<segments.getFirst().getV().size();i++){
			repPoints.add(segments.getFirst().getV().get(i));
		}
		
		for (int i =0; i<segments.size()-1;i++){
			repPoints.add(segments.get(i).getSharedJunction(rm, segments.get(i+1)));
		}
		for (int i=0;i<segments.getLast().getV().size();i++){
			repPoints.add(segments.getLast().getV().get(i));
		}
		return repPoints;
	}
	public boolean isExtensible(){
		return (extendStart || extendEnd);
	}
	public boolean isExtendStart() {
		return extendStart;
	}

	public void setExtendStart(boolean extendStart) {
		this.extendStart = extendStart;
	}

	public boolean isExtendEnd() {
		return extendEnd;
	}

	public void setExtendEnd(boolean extendEnd) {
		this.extendEnd = extendEnd;
	}
	
	public int numOfSharedTrajs(SegCluster otherSegCluster){
		int num=0;
		for (int i=0;i<this.TrajIdList.size();i++){
			if(otherSegCluster.getTrajList().contains(this.TrajIdList.get(i)))
				num++;
		}
		return num;
	}
	//modified hausdorff distance
	public double distToOtherSegCluster(RoadMap rm, SegCluster otherSegCluster){
		
		double dss = rm.getShortestRoute(this.getStartJunc().getRoadnetLocation()
				, otherSegCluster.getStartJunc().getRoadnetLocation()).getLength();
		double dse = rm.getShortestRoute(this.getStartJunc().getRoadnetLocation()
				, otherSegCluster.getEndJunc().getRoadnetLocation()).getLength();
		double des = rm.getShortestRoute(this.getEndJunc().getRoadnetLocation()
				, otherSegCluster.getStartJunc().getRoadnetLocation()).getLength();
		double dee = rm.getShortestRoute(this.getEndJunc().getRoadnetLocation()
				, otherSegCluster.getEndJunc().getRoadnetLocation()).getLength();

		double max1= Math.max(Math.min(dss, dse), Math.min(des, dee));
		double max2= Math.max(Math.min(dss, des), Math.min(dse, dee));
		return Math.max(max1,max2);
		}
	public double euclideanDistToOtherSegCluster(SegCluster otherSegCluster){
		double dss = this.getStartJunc().getCartesianLocation().toPoint(
				otherSegCluster.getStartJunc().getCartesianLocation());
		double dse = this.getStartJunc().getCartesianLocation().toPoint(
				otherSegCluster.getEndJunc().getCartesianLocation());
		double des = this.getEndJunc().getCartesianLocation().toPoint(
				otherSegCluster.getStartJunc().getCartesianLocation());
		double dee = this.getEndJunc().getCartesianLocation().toPoint(
				otherSegCluster.getEndJunc().getCartesianLocation());
		double max1= Math.max(Math.min(dss, dse), Math.min(des, dee));
		double max2= Math.max(Math.min(dss, des), Math.min(dse, dee));
		return Math.max (Math.max(dss,dse),Math.max(des,dee));
	}
	public double getLength(RoadMap rm){
		double length = 0;
		for (int i=0; i<this.segments.size();i++){
			length += this.segments.get(i).getRoadSeg(rm).getLength();
			
		}
		return length;
	}
}


