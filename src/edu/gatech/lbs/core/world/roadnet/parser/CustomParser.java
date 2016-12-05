// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.parser;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomParser extends MapParser {

  // see:
  // http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/FirstProject.java
  // GeoTools jar dependency tree:
  // http://www.geotools.org/quickstart.html
  //
  // For TIGER/Line shapefile MTFCC code interpretation, see:
  // http://www.census.gov/geo/www/tiger/cfcc_to_mtfcc.xls
  public void load(String filename, RoadMap roadmap) {
    junctionMap.clear();
    BoundingBox bounds = roadmap.getBounds();
    boolean isClassed = (roadmap instanceof ClassedRoadMap);

    double unit = 1000 * 40075 / 360; // [m/degree]

    try (BufferedReader br = new BufferedReader(new FileReader(filename)))
    {

      String sCurrentLine;


      while ((sCurrentLine = br.readLine()) != null) {
        ArrayList<CartesianVector> points = new ArrayList<CartesianVector>();
        Scanner scanner = new Scanner(sCurrentLine);
        for (int i = 0; i < 2; i++) {
          double longitude = scanner.nextDouble();
          double latitude = scanner.nextDouble();
          points.add(new CartesianVector(longitude, latitude));
          bounds.includePoint(longitude, latitude);
        }
        RoadJunction[] junctions = getJunctions(points);

        CartesianVector[] pointArray = new CartesianVector[points.size()];
        points.toArray(pointArray);
        RoadSegment seg = new RoadSegment(roadmap.getNextValidId(), junctions[0], junctions[1], false, pointArray, Float.MAX_VALUE);
        roadmap.addRoadSegment(seg);
      }

    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }
}
