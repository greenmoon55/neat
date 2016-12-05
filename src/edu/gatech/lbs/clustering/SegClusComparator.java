/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;

import java.util.Comparator;
import edu.gatech.lbs.core.world.roadnet.RoadMap;


public class SegClusComparator implements Comparator<SegCluster> {
	protected RoadMap roadmap;
	public SegClusComparator(RoadMap roadmap){
		this.roadmap = roadmap;
	}
	@Override
	
	public int compare(SegCluster sc0, SegCluster sc1) {
		// TODO Auto-generated method stub
		if (sc0.getLength(roadmap)<sc1.getLength(roadmap))
			return -1;
		else if (sc0.getLength(roadmap)>sc1.getLength(roadmap))
			return 1;
		else
		return 0;
	}

}
