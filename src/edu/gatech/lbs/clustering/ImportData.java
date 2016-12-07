/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.List;
import java.util.HashMap;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class ImportData {
	protected HashMap<Integer,List<Integer>> trajIdClus;//list of trajId in each cluster
	protected HashMap<Integer,List<Integer>> segIdClus;//list of segId in each cluster
	
public ImportData(){
	trajIdClus = new HashMap<Integer,List<Integer>>();
	segIdClus = new HashMap<Integer,List<Integer>>();
}
public HashMap<Integer, List<Integer>> getTrajIdClus() {
		return trajIdClus;
	}

public HashMap<Integer, List<Integer>> getSegIdClus() {
	return segIdClus;
}
public Collection<Trajectory> LoadTrajectories(String path, RoadMap roadmap) {
	HashMap<Integer,Trajectory> trajectories = new HashMap<Integer,Trajectory>();
	BufferedReader reader = null;
	InputStream in = null;
	try {

		in = FileHelper.openFileOrUrl(path);
	    reader = new BufferedReader(new InputStreamReader(in));
	    
	    
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	
	} catch (IOException e) {
		e.printStackTrace();
	
	}

	String line = null;
	StringTokenizer tk = null;
    int currentTrajId = 0;
    int prevSegId = -1;
    int prevOrigTrajId = -1;

    try {
		while ((line = reader.readLine()) != null) {
			tk = new StringTokenizer(line, " ");

			int origTrajId = Integer.parseInt(tk.nextToken());
            int trajId;
            if (prevOrigTrajId != origTrajId && prevOrigTrajId != -1) {
                trajId = ++currentTrajId;
            } else {
                trajId = currentTrajId;
            }
            prevOrigTrajId = origTrajId;

			int segId = Integer.parseInt(tk.nextToken());
            if (segId != prevSegId && prevSegId != -1) {
                RoadSegment currentSegment = roadmap.getRoadSegment(segId);
                RoadSegment prevSegment = roadmap.getRoadSegment(prevSegId);
                RoadJunction curSourceJunction = currentSegment.getSourceJunction();
                RoadJunction curTargetJunction = currentSegment.getTargetJunction();
                RoadJunction prevSourceJunction = prevSegment.getSourceJunction();
                RoadJunction prevTargetJunction = prevSegment.getTargetJunction();
                if (curSourceJunction.getId() != prevSourceJunction.getId()
                        && curSourceJunction.getId() != prevTargetJunction.getId()
                        && curTargetJunction.getId() != prevSourceJunction.getId()
                        && curTargetJunction.getId() != prevTargetJunction.getId()) {
                    trajId = ++currentTrajId;
                }
            }
            prevSegId = segId;

			double x = Double.parseDouble(tk.nextToken());
			double y = Double.parseDouble(tk.nextToken());
			CartesianVector v = new CartesianVector(x,y); 
			Point p = new Point(segId,v);
			
			  if (!trajectories.containsKey(trajId)){
				  Trajectory traj = new Trajectory(trajId);
				  trajectories.put(trajId, traj);
			  }
			  trajectories.get(trajId).getPoints().add(p);
		}
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	try {
		if (reader != null) {
			reader.close();
			in.close();
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	
	
	Collection<Trajectory> trajs = trajectories.values();
	return trajs;
	
}

// to read Traclus results with segId info
public Collection<List<IVector>> loadTraClusRe(String pathToFile){
	HashMap<Integer,List<IVector>> traClusters = new HashMap<Integer,List<IVector>>();
	LinkedList<IVector> clus = new LinkedList<IVector>();
	ArrayList<Integer> trajIds =  new ArrayList<Integer>();//list of trajId for one cluster
	ArrayList<Integer> segIds =  new ArrayList<Integer>();//list of segId for one cluster
	
		
	File testData = new File(pathToFile);
	BufferedReader reader = null;
	try{
	reader = new BufferedReader(new FileReader(testData));
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	
	String line = null;
	int clusterId=0;
	
	try {
		reader.readLine();
		reader.readLine();
		
		while ((line = reader.readLine()) != null) {
			String[] st = line.split(" ");
			
			if (st[0].equals("cluster")){
				
				reader.readLine();//empty line
				traClusters.put(clusterId,(List<IVector>) clus.clone());
				this.trajIdClus.put(clusterId, (List<Integer>)trajIds.clone());
				this.segIdClus.put(clusterId, (List<Integer>)segIds.clone());
				clusterId++;
				clus.clear();
				trajIds.clear();
				segIds.clear();
				
			} else{
				trajIds.add(Integer.parseInt(st[0]));
				
				for (int i=1; i<st.length-1;i++){
					if ((i%3)!=1)continue;
					if (!segIds.contains(Integer.parseInt(st[i]))) segIds.add(Integer.parseInt(st[i]));
					double x = Double.parseDouble(st[i+1]);
					double y = Double.parseDouble(st[i+2]);
					CartesianVector v = new CartesianVector(x,y);
					
					clus.add(v);
				
			}
			
		}
		traClusters.put(clusterId,clus);
		}		
	} catch (Exception e) {
		e.printStackTrace();
	}
	try {
		if (reader != null) {
			reader.close();
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
	return traClusters.values();

}


public static void main(String[] args) {
	// TODO Auto-generated method stub
	//ImportData im = new ImportData();
	//Collection<Trajectory> t = im.LoadTrajectories("http://www.cc.gatech.edu/~bhan31/atl-500-3.txt");//http://www.cc.gatech.edu/projects/disl/courses/cs6675/summary-template.txt
	//System.out.print(Math.round(900/t.size()));
}
}
