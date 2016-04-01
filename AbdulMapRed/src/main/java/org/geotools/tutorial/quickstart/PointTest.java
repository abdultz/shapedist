/**
 * Copyright 2015 Abdul Zummerwala <abdul.zummerwala@yahoo.co.in>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geotools.tutorial.quickstart;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Point;
/**
 * 
 * Prompts the user for a shapefile and displays all the points
 * 
 * Additionally it also shows the X and Y coordinates for
 * the points. It also exposes the coordinates for further processing
 * 
 */
public class PointTest {

     /**
     * Depends on GeoTools library and JTS (Java Topology Suite)
     */
    
        public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        if(store != null)
        {
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("Quickstart");
        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        
        SimpleFeatureIterator iterator = featureSource.getFeatures().features();
        
        System.out.println("No. of features in the Shapefile: " +featureSource.getFeatures().toArray().length);
        
        try
        {
            while(iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                GeometryFactory geometryfactory = JTSFactoryFinder.getGeometryFactory(null);
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                Coordinate coord = new Coordinate(geom.getCoordinate().x, geom.getCoordinate().y,geom.getCoordinate().z);
                Point point = geometryfactory.createPoint(coord);
                System.out.println("x: "+ point.getX()+ "; y: "+ point.getY());//+ "; z: "+ point.getZ());
            }
        }
        catch(Exception e)
        {
            
        }
           
        // Now display the map
        //JMapFrame.showMap(map);
        }
        else
        {
            System.out.println("store = null");
        }
    }

}