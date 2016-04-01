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

import java.io.File;
import java.util.Date;

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

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class QuickstartTimed {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        Date dat1 = new Date();
        System.out.println("Started QuickstartTimed at time: "+ dat1.getTime()  + "(ms)...");
        
        //For loop starts
        for (int h = 0; h < 1024; h++) 
        {
        
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        if(store != null)
        {
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("QuickstartTimed");
        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        
        System.out.println("No. of features in the Shapefile: " +featureSource.getFeatures().toArray().length + " :iter: "+h);
        //for(int i=0; i<featureSource.getFeatures().toArray().length; i++)
        //{
        //    System.out.println(featureSource.getFeatures());
        //}

        // Now display the map
        //JMapFrame.showMap(map);
        }
        else
        {
            System.out.println("store = null");
        }
        
        }//For loop completes
        
        Date dat2 = new Date();
        
        System.out.println("Completed QuickstartTimed at time: "+ dat2.getTime()  + "(ms)...");
    }

}