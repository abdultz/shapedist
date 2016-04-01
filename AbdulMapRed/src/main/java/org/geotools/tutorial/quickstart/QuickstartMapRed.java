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

//MapRed
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import sun.io.Converters;
//MapRed

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class QuickstartMapRed {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    
    //MapRed
    private static final Log LOG = LogFactory.getLog(QuickstartMapRed.class);

    public static class ShapeFileMapper 
       extends Mapper<Object, Text, Text, Text>{
    private Text one = new Text();
      private Text word = new Text();
      
    public void map(Object key, Text value, Mapper.Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        
        one.set("1");
        
        context.write(word, one);
        LOG.info("word: "+ word+ " one: "+ one);
        
        //MyCode
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        //MapContent map = new MapContent();
        //map.setTitle("Quickstart");
        
        //Style style = SLD.createSimpleStyle(featureSource.getSchema());
        //Layer layer = new FeatureLayer(featureSource, style);
        //map.addLayer(layer);
        
        System.out.println("No. of features in the Shapefile: " +featureSource.getFeatures().toArray().length);
        for(int i=0; i<featureSource.getFeatures().toArray().length; i++)
        {
            System.out.println(featureSource.getFeatures());
        }

        // Now display the map
        //JMapFrame.showMap(map);
        //MyCode
        
      }//While loop ends here
    }//Map function ends here
  }//ShapeFileMapper class ends here
    
    //MapRed
    
    //MapRed
    public static class ShapeCountReducer 
       extends Reducer<Text,Text,Text,Text> {
    private Text result1 = new Text();
    //private Text result2 = new Text();
    //private IntWritable result2 = new IntWritable();
    public void reduce(Text key, Iterable<Text> values, 
                       Reducer.Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (Text val : values) {
        sum += Integer.valueOf(val.toString().split(" ")[0]);
      }
      //result2.set(sum);
      //context.write(key, result2);
      String abc = String.valueOf(sum);
      //String def = String.valueOf(key.getLength());
      LOG.info("abc: "+ abc);
      //LOG.info("def: "+ def);
      result1.set(String.valueOf(sum)+" "+String.valueOf(key.getLength()));
      //result2.set(def);
      context.write(key, result1);
      //context.write(key, result2);
      
    }
  }
    //MapRed
    
    public static void main(String[] args) throws Exception {
        
        
        //MapRed
        Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: QuickstartMapRed <in> [<in>...] <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "QuickstartMapRed");
    job.setJarByClass(QuickstartMapRed.class);
    job.setMapperClass(ShapeFileMapper.class);
    job.setCombinerClass(ShapeCountReducer.class);
    job.setReducerClass(ShapeCountReducer.class);
    //job.setMapOutputKeyClass(Text.class);
    //job.setMapOutputValueClass(IntWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,
      new Path(otherArgs[otherArgs.length - 1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
        //MapRed
        
        
    }

}