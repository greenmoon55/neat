package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;
import java.util.Random;
import edu.gatech.lbs.core.vector.*;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.route.Route;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.VelocityChangeEvent;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;


public class RoadnetFixedDestination_IndividualMobilityModel extends IndividualMobilityModel {
	
	public static final String xmlName = "FixedDestRoadnet";

	  protected RoadnetVector location;
	  protected RoadnetVector destination; // next waypoint (junction)
	  protected RoadnetVector v; // [m/s]
	  protected Route route;
	  protected int routeSegment;
	  protected SimAgent agent;

	  protected RoadMap roadmap;
	  protected ILocationDistribution locationDistribution;
	  protected IParamDistribution speedDistribution;
	  protected IParamDistribution parkingTimeDistribution;
	  protected IParamDistribution stoppingTimeDistribution;
	  protected boolean isFixed;
	  protected boolean isReached;
	  protected int count;
	  
	  
	  protected IVector[] fixedLocations;
	  

	  public RoadnetFixedDestination_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, IParamDistribution parkingTimeDistribution, IParamDistribution stoppingTimeDistribution, long timestamp, RoadMap roadmap) {
	    this.sim = sim;
	    this.agent = agent;

	    this.roadmap = roadmap;
	    this.locationDistribution = locationDistribution;
	    this.speedDistribution = speedDistribution;
	    this.parkingTimeDistribution = parkingTimeDistribution;
	    this.stoppingTimeDistribution = stoppingTimeDistribution;
	    this.timestamp = timestamp;
	    this.fixedLocations = locationDistribution.getFixedLocations();
	    
	    this.isFixed = false;
	    this.isReached=false;
	    this.count = 0;
	  }

	  
	  protected void reachEndOfRoute() {
	    if (parkingTimeDistribution != null) {
	      route = null;
	      stopMoving();
	    } else {
	      planNewRoute();
	      if (stoppingTimeDistribution != null) {
	        stopMoving();
	      } else {
	        startMovingOnNewSegment();
	      }
	    }
	  }

	  protected void stopMoving() {
	    v = new RoadnetVector(location.getRoadSegment(), 0);
	  }

	  protected void startMovingOnNewSegment() {
	    // new segment info:
	    RoadSegment seg = route.getSegment(routeSegment);
	    boolean isForwardTraversed = route.getDirection(routeSegment);

	    // set current destination/waypoint to other end of segment, or the route's end-point:
	    if (routeSegment < route.getSegmentCount() - 1) {
	      destination = new RoadnetVector(seg, isForwardTraversed ? seg.getLength() : 0);
	    } else {
	      destination = route.getTarget();
	    }

	    v = new RoadnetVector(location.getRoadSegment(), (destination.getProgress() > location.getProgress() ? +1 : -1) * (float) Math.abs(speedDistribution.getNextValue(location)));
	  }

	  protected void planNewRoute() {
		// make a new route plan:
		Random rnd = new Random();
		
		// check if agent has reached destination
		if (isReached==true){
			RoadnetVector tmpDest = new RoadnetVector(location.getRoadSegment()
	    			,(float) (rnd.nextDouble() * location.getRoadSegment().getLength()));
			route = roadmap.getShortestRoute(location,tmpDest);
		}else{
		for (int i=0; i<fixedLocations.length;i++){
		    if (location.getRoadSegment().getId()== fixedLocations[i].toRoadnetVector().getRoadSegment().getId()){
		    	isReached=true;
		    	RoadnetVector tmpDest = new RoadnetVector(location.getRoadSegment()
		    			,(float) (rnd.nextDouble() * location.getRoadSegment().getLength()));
		    	route = roadmap.getShortestRoute(location,tmpDest);
		    	break;
		    	}
		    		
		    }
		if (isReached==false){
			if (isFixed==false)
				route = roadmap.getShortestRoute(location, locationDistribution.getNextLocation().toRoadnetVector());
			else{ 
				
	    	//Random rnd = new Random();
			int rndInt = rnd.nextInt(fixedLocations.length);
	    	route = roadmap.getShortestRoute(location, 
	    		fixedLocations[rndInt].toRoadnetVector());
	    System.out.println("to1 segment" + fixedLocations[rndInt].toRoadnetVector().getRoadSegment().getId());
	    	
			}
		}
		}
	    routeSegment = 0;
	  }

	  public SimEvent getNextEvent() {

		if (location ==null){
			if (isFixed==false){
				location = locationDistribution.getNextLocation().toRoadnetVector();
			
			}else {
				Random rnd = new Random();
				int rndInt = rnd.nextInt(2);
				location = fixedLocations[rndInt].toRoadnetVector();
				isFixed = false;
				System.out.println("to segment" + fixedLocations[rndInt].toRoadnetVector().getRoadSegment().getId());
		    }
		}

	    if (v == null) {
	    	
	      reachEndOfRoute();
	      return new VelocityChangeEvent(sim, timestamp, agent, location, v);
	    }

	    // we are currently moving:
	    if (v.getLength() != 0) {
	      // move to destination:
	      timestamp += (long) (1000 * location.vectorTo(destination).getLength() / v.getLength());
	      location = destination;

	      // move to next segment in route:
	      if (route != null && routeSegment < route.getSegmentCount() - 1) {
	        routeSegment++;
	        // new segment info:
	        RoadSegment seg = route.getSegment(routeSegment);
	        boolean isForwardTraversed = route.getDirection(routeSegment);

	        // set current location to correct end of segment:
	        location = new RoadnetVector(seg, isForwardTraversed ? 0 : seg.getLength());

	        if (stoppingTimeDistribution != null) {
	          stopMoving();
	        } else {
	          startMovingOnNewSegment();
	        }
	      }
	      // if reached final segment of route:
	      else {
	    	  count++;
	    	//the second new route will be the fixed destination
			  if (count ==2) isFixed = true;
	        reachEndOfRoute();
	      }
	    }
	    // we are not currently moving:
	    else {
	      // if we are not in a route, we need to plan a new one:
	      if (route == null) {
	        // park before starting to move:
	        if (parkingTimeDistribution != null) {
	          timestamp += (long) (1000 * parkingTimeDistribution.getNextValue(location));
	        }
	        planNewRoute();
	      }

	      // stop at intersection before starting to move:
	      if (stoppingTimeDistribution != null) {
	        timestamp += (long) (1000 * stoppingTimeDistribution.getNextValue(location));
	      }

	      startMovingOnNewSegment();
	    }

	    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
	  }
}
