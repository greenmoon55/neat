/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;


import edu.gatech.lbs.core.world.roadnet.RoadMap;

//phase 3 to merge flow clusters
public class MergedClusters {
	protected List<SegCluster> segClus;// input - flow clusters
	protected HashMap<Integer, List<SegCluster>> resultSegClus;//output - density based opt
	protected RoadMap roadmap;
	protected HashMap<Integer,List<Integer>> trajIdList;
	
	protected double eps;
	
	public MergedClusters(List<SegCluster> segClus, RoadMap rm, int neatMode, double eps){
		this.segClus = segClus;
		this.roadmap = rm;
		this.trajIdList =  new HashMap<Integer,List<Integer>>();
		this.resultSegClus = new HashMap<Integer, List<SegCluster>>();
		if (neatMode==3){
		merge(eps);} 
		else
		nonMerge();
		
	}
	
	public double getEps() {
		return eps;
	}

	// return the adjacent list of distance between each pair of flow clusters
	public HashMap<Integer,ArrayList<Double>> calculateDistList(List<SegCluster> segClus){
		HashMap<Integer,ArrayList<Double>> distanceList = new HashMap<Integer,ArrayList<Double>>();
		int n = segClus.size();
		double sum=0;
		for (int i=0;i<n-1;i++){
			SegCluster mySc = segClus.get(i);
			ArrayList<Double> tmpList = new ArrayList<Double>();
			for (int j=i+1; j<n;j++){
				double tmpDist = mySc.distToOtherSegCluster(this.roadmap, segClus.get(j));
				tmpList.add(tmpDist);
				sum += tmpDist;
			}
			distanceList.put(i, tmpList);
		}
		double avgDist = sum/((n-1)*(n-2)/2);
		System.out.println("The average distance is: "+avgDist);
		this.eps = avgDist;
		
		return distanceList;
	}
	public void calculateEps(){
		double sum = 0;
		
		 for (int i=0; i<segClus.size()-1; i++)
         {
			 
             for (int j = i+1; j<segClus.size(); j++)
             {
            	 double dist = this.segClus.get(i).distToOtherSegCluster(this.roadmap, this.segClus.get(j));
            	 //System.out.println(dist);
                 sum += dist;
                 
             }
         }
		 double avgDist = sum/((segClus.size()-1)*(segClus.size()-2)/2);
		 System.out.println("The average distance is: "+avgDist);
		 this.eps = avgDist;
		 
	}
/*
	public void generateDistMatrix(){
		double sum = 0;
		 for (int i=0; i<segClus.size(); i++)
         {
			 distMatrix[i][i] = 0;
             for (int j = i+1; j<segClus.size(); j++)
             {
            	 distMatrix[i][j] = this.segClus.get(i).distToOtherSegCluster(this.roadmap, this.segClus.get(j));
            	 distMatrix[j][i] = distMatrix[i][j];
                 sum += distMatrix[i][j];
             }
         }
		 this.eps = (double)sum/((segClus.size()-1)*(segClus.size()-2)/2);
	}
	*/

	
	public HashMap<Integer, List<SegCluster>> getResultSegClus() {
		return resultSegClus;
	}
	public void setEps(double eps) {
		this.eps = eps;
	}

	public void nonMerge(){
		double avgLength = 0;
		int n = this.segClus.size();
		for (int i=0; i<this.segClus.size();i++){
			List<SegCluster> newClus = new ArrayList<SegCluster>();
			newClus.add(segClus.get(i));
			this.resultSegClus.put(i,newClus);
			avgLength += this.segClus.get(i).getLength(roadmap);
		}
		avgLength = avgLength/n;
		
		System.out.println("Average length of flow representatives: " + avgLength );
		System.out.println("minimum length is: "+this.segClus.get(0).getLength(roadmap));
		System.out.println("maximum length is: "+this.segClus.get(n-1).getLength(roadmap));
	}
	public void merge(double eps){
		//calculateEps();
		setEps(eps);
		int count = 0;
		while (segClus.size()>0){
			List<SegCluster> newClus = new ArrayList<SegCluster>();
			newClus.add(segClus.get(segClus.size()-1));
			segClus.remove(segClus.size()-1);
			for (int i=0; i< newClus.size();i++){
				SegCluster mySc = newClus.get(i);
				for (int j=segClus.size()-1; j>=0;j--){
					SegCluster otherSc = segClus.get(j);
					if ((mySc.euclideanDistToOtherSegCluster(otherSc))<this.eps){
						if ((mySc.distToOtherSegCluster(roadmap,otherSc))<this.eps){
							newClus.add(otherSc);
							this.segClus.remove(j);
						}
					}
				}
			}
			this.resultSegClus.put(count,newClus);
			count++;
		}
		
		
	}
	
	public double getLengthOf(List<SegCluster> scList){
		double length=0;
		for (int i=0; i< scList.size();i++){
			length += scList.get(i).getLength(this.roadmap);
		}
		return length;
	}
	
	public List<Integer> getTrajListOf(List<SegCluster> scList){
		List<Integer> myIdList =  new ArrayList<Integer>();
	
		myIdList.addAll(scList.get(0).getTrajList());
		for (int j=1;j<scList.size();j++){
			List<Integer> tmp = scList.get(j).getTrajList();
			for (int k=0;k<tmp.size();k++){
				if (!myIdList.contains(tmp.get(k)))
					myIdList.add(tmp.get(k));
			}
		}
		return myIdList;
	}
	public double getTotalLength(){
		double total=0;
		for (List<SegCluster> sc: this.resultSegClus.values()){
			total = total + getLengthOf(sc);
		}
		return total;
	}

	//dist =  max_i(clus1){min_j(clus2){dist(sc_i(clus1),sc_j(clus2))}}
	public double distOfMergedClusters(List<SegCluster> scList1, List<SegCluster> scList2){
		double maxij = 0;
		for (int i=0; i<scList1.size();i++){
			double min = scList1.get(i).distToOtherSegCluster(this.roadmap, scList2.get(0));
			for (int j=1; j<scList2.size();j++){
				if (min>scList1.get(i).distToOtherSegCluster(this.roadmap, scList2.get(j)))
					min = scList1.get(i).distToOtherSegCluster(this.roadmap, scList2.get(j));
			}
			if (min>maxij) maxij = min;
		}
		return maxij;

	}
	
	public void generatetrajIdList(){
		
		for (int i=0; i<this.resultSegClus.size();i++){
			this.trajIdList.put(i, getTrajListOf(this.resultSegClus.get(i)));
		}
	}
	public int sharedElements(List<Integer> l1, List<Integer> l2){
		int num=0;
		
		for (Integer e : l1){
			
			if(l2.contains(e))
				 
				num++;
		}
		return num;
	}

	
	public double[] clusterValid(){
		double[] valid = new double[3];
		int n = this.resultSegClus.size();
		int[] tuples = new int[n];//store index of the closest cluster to cluster [i]
		double[] minDists = new double[n];//store the distance from a cluster to its closest cluster
		
		generatetrajIdList();
		
		//calculate distances between clusters
		HashMap<Integer,ArrayList<Double>> distanceList = new HashMap<Integer,ArrayList<Double>>();
		for (int i=0;i<n-1;i++){
			List<SegCluster> mySC = this.resultSegClus.get(i);
			ArrayList<Double> tmpList = new ArrayList<Double>();
			for (int j=i+1; j<n;j++){
				tmpList.add(distOfMergedClusters(mySC,this.resultSegClus.get(j)));
			}
			distanceList.put(i, tmpList);
		}
		
		//find the closest cluster for each cluster
		for (int i=0;i<n;i++){
			if(i<n-1){
			minDists[i]=distanceList.get(i).get(0);
			tuples[i]=i+1;
			for (int j=0;j<distanceList.get(i).size();j++){
				if (distanceList.get(i).get(j)<minDists[i]){
				minDists[i] = distanceList.get(i).get(j);
				tuples[i]= i+j+1;
				}
			}
			} else {
				minDists[i]=distanceList.get(n-2).get(0);
				tuples[i]=n-2;
			}
			for (int j=i-1; j>0; j--){
				
					if (distanceList.get(j).get(i-j-1)<minDists[i]){
						minDists[i] = distanceList.get(j).get(i-j-1);
						tuples[i]= j;
				}
			}
		}
		
		//trajSharedBased
		double trajSharedBased = 0;
		for (int i=0;i<n;i++){
			double tmp0;
			int s = sharedElements(trajIdList.get(i),trajIdList.get(tuples[i]));
			//System.out.print(s);
			//tmp0 = sharedElements(trajIdList.get(i),trajIdList.get(tuples[i]))/
			//(trajIdList.get(i).size()+trajIdList.get(tuples[i]).size()-sharedElements(trajIdList.get(i),trajIdList.get(tuples[i])));
			tmp0=(double)s/(trajIdList.get(i).size()+trajIdList.get(tuples[i]).size()-s);
			//System.out.print(tmp0);
			//System.out.print(trajIdList.get(i).size()+" ");
			//System.out.println(trajIdList.get(tuples[i]).size());
			//for(int k:trajIdList.get(i))System.out.print(k+" ");System.out.println();
			
			//for(int k:trajIdList.get(tuples[i]))System.out.print(k+" ");System.out.println();
			trajSharedBased +=tmp0;
		}
		valid[2] = trajSharedBased/n;
		double sum=0;
		for (int i=0; i<n; i++){
			sum +=minDists[i];
		}
		valid[0]= sum/getTotalLength();
		//lengthBased
		double lengthBased = 0;
		for (int i=0;i<n;i++){
			double tmp1;
			tmp1 = 2*minDists[i]/(getLengthOf(this.resultSegClus.get(i))
					+getLengthOf(this.resultSegClus.get(tuples[i])));
			lengthBased +=tmp1;
		}
		valid[1] = lengthBased/n;
		return valid;
	}
	

}
