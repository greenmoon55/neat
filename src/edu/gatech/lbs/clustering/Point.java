/**
 * Copyright (c) 2011, Disl CoC Georgia Tech
 * Authors: Binh Han (binhhan@gatech.edu)
 */
package edu.gatech.lbs.clustering;
import edu.gatech.lbs.core.vector.*;


public class Point {

		protected int segid;

		protected IVector v;
		public Point(int segid, IVector v){
			this.segid = segid;
			this.v = v;
		}
		public int getSegid() {
			return segid;
		}
		public void setSegid(int segid) {
			this.segid = segid;
		}
		public IVector getV() {
			return v;
		}
		public void setV(IVector v) {
			this.v = v;
		}
	

	}

