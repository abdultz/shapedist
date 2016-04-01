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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import org.eclipse.jdt.internal.compiler.ast.Argument;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class QuickstartRD {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        
        
        File origfile = JFileDataStoreChooser.showOpenFile("shp", null);
        if (origfile == null) {
            return;
        }

        File newfile = new File("/home/shadoop/shapefile.shp");  

        if(newfile.exists())
        {
            newfile.delete();
            System.out.println("existing file deleted");
        }
        
        if(newfile.createNewFile())
        {
            //file.createNewFile();
            if (newfile == null) {
                System.out.println("Cannot create tempfile");
                return ;
            }
            //FileInputStream fis = new FileInputStream(file1);

            //byte[] Shapefile = Files.readAllBytes(origpath);
            RandomAccessFile f = new RandomAccessFile(origfile, "r");
            byte[] Shapefile = new byte[(int)f.length()];
            f.read(Shapefile);
            
            FileOutputStream fos = new FileOutputStream(newfile);
            fos.write(Shapefile);
            fos.flush();
            fos.close();

            System.out.println("file path: "+newfile.getPath());
            System.out.println("shapefile length: "+ Shapefile.length);

            FileDataStore store = FileDataStoreFinder.getDataStore(origfile);
            FileDataStore store1 = FileDataStoreFinder.getDataStore(newfile);

            if(store != null)
            {

                SimpleFeatureSource featureSource = store1.getFeatureSource();
                if(featureSource != null)
                {
                    System.out.println("No. of features in the Shapefile: " +featureSource.getFeatures().toArray().length);
                }
                else
                {
                    System.out.println("featuresource = null");
                }
            }
            else
            {
                System.out.println("Store = null");
            }

            if(store1 != null)
            {

                SimpleFeatureSource featureSource1 = store1.getFeatureSource();
                if(featureSource1 !=null)
                {
                    System.out.println("No. of features in the Shapefile: " +featureSource1.getFeatures().toArray().length);
                }
                else
                {
                    System.out.println("featuresource1 = null");
                }
                
            }
            else
            {
                System.out.println("Store1 = null");
            }
        }
    }
}