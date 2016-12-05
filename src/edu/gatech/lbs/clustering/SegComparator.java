/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;
import java.util.Comparator;
public class SegComparator implements Comparator<PointsOnSeg> {
	public SegComparator(){
		super();
	}
	@Override
	
	public int compare(PointsOnSeg ps0, PointsOnSeg ps1) {
		// TODO Auto-generated method stub
		if (ps0.getV().size()<ps1.getV().size())
			return -1;
		else if (ps0.getV().size()<ps1.getV().size())
			return 1;
		else
		return 0;
	}

}
