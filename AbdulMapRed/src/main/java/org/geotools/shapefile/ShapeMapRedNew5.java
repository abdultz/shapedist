/**
 * Copyright 2015 Abdul Zummerwala <abdul.zummerwala@yahoo.co.in>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.geotools.shapefile;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedWriter;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;

public class ShapeMapRedNew5 {

    public static class ShapeFileMapper extends Mapper<Text, BytesWritable, Text, Text> {

        public void map(Text filepath, BytesWritable filecontent, Context context) throws IOException, InterruptedException {
            //get the Shape Feature Count for the Shapefile
            String count;
            try {

                count = countFeaturesNew_oldshpx(filecontent.getBytes());
                if (count == "-1") {
                    System.out.println("count: " + count + "; key: " + filepath);
                    return;
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                context.setStatus("Internal error - can't find the method to count the features of Shapefile");
                return;
            }
            Text countText = new Text(count);
            System.out.println("count: " + count + "; key: " + filepath);
            //put the file in the map where the md5 is the key, so duplicates will
            // be grouped together for the reduce function
            context.write(countText, filepath);
        }
    }

    //New Old Mapper Begins
    static String countFeaturesNew_old(byte[] ShapeFile) throws NoSuchAlgorithmException, IOException {
        //Count of features in this file

        try {
            File newfile = new File("/home/shadoop/shapefile.shp");

            if (newfile.exists()) {
                newfile.delete();
                System.out.println("existing file deleted");
            }

            if (newfile.createNewFile()) {
                //file.createNewFile();
                if (newfile == null) {
                    System.out.println("Cannot create tempfile");
                    return "-1";
                } else {

                    FileOutputStream fos = new FileOutputStream(newfile);
                    fos.write(ShapeFile);
                    fos.flush();
                    fos.close();

                    System.out.println("new file path: " + newfile.getPath());
                    System.out.println("shapefile length: " + ShapeFile.length);

                    FileDataStore store1 = FileDataStoreFinder.getDataStore(newfile);

                    if (store1 != null) {

                        SimpleFeatureSource featureSource1 = store1.getFeatureSource();
                        if (featureSource1 != null) {
                                //System.out.println("No. of features in the Shapefile: " + featureSource1.getFeatures().toArray().length);

                            //New Code Starts
                            SimpleFeatureIterator iterator = featureSource1.getFeatures().features();

                            System.out.println("No. of features in the Shapefile: " + featureSource1.getFeatures().toArray().length);

                            StringBuffer strbufretval = new StringBuffer();

                            try {
                                double totalarea = 0;
                                double totalperimeter = 0;

                                while (iterator.hasNext()) {

                                    SimpleFeature feature = iterator.next();
                                    GeometryFactory geometryfactory = JTSFactoryFinder.getGeometryFactory(null);
                                    Geometry geom = (Geometry) feature.getDefaultGeometry();

                                        //feature.getType() gives SimpleFeatureTypeImpl railways identified extends lineFeature(the_geom:MultiLineString,osm_id:osm_id,name:name,type:type)
                                    //feature.getType().getSuper() gives SimpleFeatureTypeImpl http://www.opengis.net/gml:lineFeature identified extends Feature()
                                    //feature.getType().getSuper().getName() gives http://www.opengis.net/gml:lineFeature
                                    //feature.getType().getSuper().getName().getLocalPart() gives lineFeature / pointFeature / polygonFeature
                                    //which is WHAT WE WANT
                                    //Note: The below must be put in a Switch Statement
                                    //Point Code Starts
                                    if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("pointFeature")) {
                                        Coordinate coord = new Coordinate(geom.getCoordinate().x, geom.getCoordinate().y, geom.getCoordinate().z);
                                        Point point = geometryfactory.createPoint(coord);
                                        String out = "x: " + point.getX() + "; y: " + point.getY();
                                        strbufretval.append("Point:\n");
                                        strbufretval.append("x: ").append(point.getX()).append("; y: ").append(point.getY()).append("\n");
                                        System.out.println("Point:");//+ "; z: "+ point.getZ());
                                        System.out.println(out);//+ "; z: "+ point.getZ());
                                    } //Point Code Ends
                                    else if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("lineFeature")) {
                                        Coordinate coords[] = geom.getCoordinates();
                                        LineString ls = geometryfactory.createLineString(coords);
                                        StringBuffer empty = new StringBuffer();
                                        empty.append("LS length: ").append(ls.getLength()).append(" ;\n");
                                        for (Coordinate cord : coords) {
                                            empty.append(" X:").append(cord.x).append(", Y:").append(cord.y).append(" ;\n");
                                        }
                                        strbufretval.append("Line:\n");
                                        strbufretval.append(empty.toString()).append("\n");
                                        System.out.println("Line:");
                                        System.out.println(empty.toString());

                                    } else if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("polygonFeature")) {
                                        Coordinate coords[] = geom.getCoordinates();
                                        Polygon pg = geometryfactory.createPolygon(coords);
                                        double thisarea = pg.getArea();
                                        double thisperimeter = pg.getLength();
                                        totalarea += thisarea;
                                        totalperimeter += thisperimeter;
                                        StringBuffer empty = new StringBuffer();
                                        empty.append("PG Area: ").append(thisarea).append(" ;").append(" PG Peri: ").append(thisperimeter).append(" ;\n");

                                        for (Coordinate cord : coords) {
                                            empty = empty.append(" X:").append(cord.x).append(", Y:").append(cord.y).append(" ;\n");
                                        }
                                        strbufretval.append("Polygon:\n");
                                        strbufretval.append(empty.toString()).append("\n");
                                        System.out.println("Polygon:");
                                        System.out.println(empty.toString());

                                    }

                                }//while loop ends - we finished through all features of the shapefile
                            } catch (Exception e) {

                            } finally {
                                iterator.close();
                                store1.dispose();

                            }
                            //New Code Ends
                            //return String.valueOf(featureSource1.getFeatures().toArray().length);
                            return String.valueOf(strbufretval.toString());

                        } else {
                            System.out.println("featuresource1 = null");
                            return "-1";
                        }

                    } else {
                        System.out.println("Store1 = null");
                        return "-1";
                    }
                }
            } else {
                return "-1";
            }
        }//try ends
        catch (Exception e) {
            System.out.println("caught and exception");
            return "-1";
        }
    }//CountFeaturesNewOld ends

    //Mapper Begins
    static String countFeaturesNew(byte[] shpxbytes) throws NoSuchAlgorithmException, IOException {

        long shpxlength = (long) shpxbytes.length;
        //The first 8 bytes will provide the length of shx file
        //which is appended to the Shapefile
        ByteBuffer bb = ByteBuffer.wrap(new byte[]{shpxbytes[0], shpxbytes[1],
            shpxbytes[2], shpxbytes[3], shpxbytes[4], shpxbytes[5],
            shpxbytes[6], shpxbytes[7]});

        long l = bb.getLong();
        boolean shxok = false;
        boolean shpok = false;
        File shxfile = new File("/home/shadoop/shapefile.shx");
        if (shxfile.exists()) {
            try {
                shxfile.delete();
                System.out.println("shxfile existing - file deleted");

            } catch (Exception x) {
                System.out.println("Exception deleting file: " + x.toString());
            }
        }
        if (shxfile.createNewFile()) {
            //file.createNewFile();
            if (shxfile == null) {
                System.out.println("Cannot create shxfile");
                return "0";
            } else {
                FileOutputStream fosshx = new FileOutputStream(shxfile);
                fosshx.write(shpxbytes, 8, (int) l);
                fosshx.flush();
                fosshx.close();
                System.out.println("shx file written - bytes: " + (int) l);
                shxok = true;
            }
        } else {
            System.out.println("cannot create shx file");
        }

        File shpfile = new File("/home/shadoop/shapefile.shp");
        if (shpfile.exists()) {
            try {
                shpfile.delete();
                System.out.println("shpfile existing - file deleted");

            } catch (Exception x) {
                System.out.println("Exception deleting file: " + x.toString());
            }
        }
        if (shpfile.createNewFile()) {
            //file.createNewFile();
            if (shpfile == null) {
                System.out.println("Cannot create shpfile");
                return "0";
            } else {
                FileOutputStream fosshp = new FileOutputStream(shpfile);
                fosshp.write(shpxbytes, (int) (l + 8), (int) (shpxlength - (l + 8)));
                fosshp.flush();
                fosshp.close();
                System.out.println("shp file written - bytes: " + (int) (shpxlength - (l + 8)));
                shpok = true;
            }
        } else {
            System.out.println("cannot create shp file");
        }
        if (shxok && shpok) {
            FileDataStore store1 = FileDataStoreFinder.getDataStore(shpfile);

            if (store1 != null) {

                SimpleFeatureSource featureSource1 = store1.getFeatureSource();
                if (featureSource1 != null) {
                    System.out.println("No. of features in the Shapefile: " + featureSource1.getFeatures().toArray().length);
                    return String.valueOf(featureSource1.getFeatures().toArray().length);
                } else {
                    System.out.println("featuresource1 = null");
                    return "0";
                }

            } else {
                System.out.println("Store1 = null");
                return "0";
            }
        } else {
            return "0";
        }
        //} else {
        //    System.out.println("shpx file dont exist");
        //}

    }//Mapper Ends

    //New Old shpx Mapper Begins
    static String countFeaturesNew_oldshpx(byte[] shpxbytes) throws NoSuchAlgorithmException, IOException {
        //Count of features in this file

        try {

            long shpxlength = (long) shpxbytes.length;
            //The first 8 bytes will provide the length of shx file
            //which is appended to the Shapefile
            ByteBuffer bb = ByteBuffer.wrap(new byte[]{shpxbytes[0], shpxbytes[1],
                shpxbytes[2], shpxbytes[3], shpxbytes[4], shpxbytes[5],
                shpxbytes[6], shpxbytes[7]});

            long l = bb.getLong();
            boolean shxok = false;
            boolean shpok = false;
            File shxfile = new File("/home/shadoop/shapefile.shx");
            if (shxfile.exists()) {
                try {
                    shxfile.delete();
                    System.out.println("shxfile existing - file deleted");

                } catch (Exception x) {
                    System.out.println("Exception deleting shx file: " + x.toString());
                }
            }
            if (shxfile.createNewFile()) {
                //file.createNewFile();
                if (shxfile == null) {
                    System.out.println("Cannot create shxfile");
                    return "0";
                } else {
                    FileOutputStream fosshx = new FileOutputStream(shxfile);
                    fosshx.write(shpxbytes, 8, (int) l);
                    fosshx.flush();
                    fosshx.close();
                    System.out.println("shx file written - bytes: " + (int) l);
                    shxok = true;
                }
            } else {
                System.out.println("cannot create shx file");
            }

            File shpfile = new File("/home/shadoop/shapefile.shp");
            if (shpfile.exists()) {
                try {
                    shpfile.delete();
                    System.out.println("shpfile existing - file deleted");

                } catch (Exception x) {
                    System.out.println("Exception deleting shp file: " + x.toString());
                }
            }
            if (shpfile.createNewFile()) {
                //file.createNewFile();
                if (shpfile == null) {
                    System.out.println("Cannot create shpfile");
                    return "0";
                } else {
                    FileOutputStream fosshp = new FileOutputStream(shpfile);
                    fosshp.write(shpxbytes, (int) (l + 8), (int) (shpxlength - (l + 8)));
                    fosshp.flush();
                    fosshp.close();
                    System.out.println("shp file written - bytes: " + (int) (shpxlength - (l + 8)));
                    shpok = true;
                }
            } else {
                System.out.println("cannot create shp file");
            }

            //both files created begins
            if (shxok && shpok) {

                File newfile = new File("/home/shadoop/shapefile.shp");
                System.out.println("new file path: " + shpfile.getPath());
                //System.out.println("shapefile length: " + shpfile.length);

                FileDataStore store1 = FileDataStoreFinder.getDataStore(newfile);

                if (store1 != null) {

                    SimpleFeatureSource featureSource1 = store1.getFeatureSource();
                    if (featureSource1 != null) {
                                //System.out.println("No. of features in the Shapefile: " + featureSource1.getFeatures().toArray().length);

                        //New Code Starts
                        SimpleFeatureIterator iterator = featureSource1.getFeatures().features();

                        System.out.println("No. of features in the Shapefile: " + featureSource1.getFeatures().toArray().length);

                        StringBuffer strbufretval = new StringBuffer();

                        try {
                            double totalarea = 0;
                            double totalperimeter = 0;

                            while (iterator.hasNext()) {

                                SimpleFeature feature = iterator.next();
                                GeometryFactory geometryfactory = JTSFactoryFinder.getGeometryFactory(null);
                                Geometry geom = (Geometry) feature.getDefaultGeometry();

                                //feature.getType() gives SimpleFeatureTypeImpl railways identified extends lineFeature(the_geom:MultiLineString,osm_id:osm_id,name:name,type:type)
                                //feature.getType().getSuper() gives SimpleFeatureTypeImpl http://www.opengis.net/gml:lineFeature identified extends Feature()
                                //feature.getType().getSuper().getName() gives http://www.opengis.net/gml:lineFeature
                                //feature.getType().getSuper().getName().getLocalPart() gives lineFeature / pointFeature / polygonFeature
                                //which is WHAT WE WANT
                                //Note: The below must be put in a Switch Statement
                                //Point Code Starts
                                if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("pointFeature")) {
                                    Coordinate coord = new Coordinate(geom.getCoordinate().x, geom.getCoordinate().y, geom.getCoordinate().z);
                                    Point point = geometryfactory.createPoint(coord);
                                    String out = "x: " + point.getX() + "; y: " + point.getY();
                                    strbufretval.append("Point:\n");
                                    strbufretval.append("x: ").append(point.getX()).append("; y: ").append(point.getY()).append("\n");
                                    System.out.println("Point:");//+ "; z: "+ point.getZ());
                                    System.out.println(out);//+ "; z: "+ point.getZ());
                                } //Point Code Ends
                                else if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("lineFeature")) {
                                    Coordinate coords[] = geom.getCoordinates();
                                    LineString ls = geometryfactory.createLineString(coords);
                                    StringBuffer empty = new StringBuffer();
                                    empty.append("LS length: ").append(ls.getLength()).append(" ;\n");
                                    for (Coordinate cord : coords) {
                                        empty.append(" X:").append(cord.x).append(", Y:").append(cord.y).append(" ;\n");
                                    }
                                    strbufretval.append("Line:\n");
                                    strbufretval.append(empty.toString()).append("\n");
                                    System.out.println("Line:");
                                    System.out.println(empty.toString());

                                } else if (feature.getType().getSuper().getName().getLocalPart().equalsIgnoreCase("polygonFeature")) {
                                    Coordinate coords[] = geom.getCoordinates();
                                    Polygon pg = geometryfactory.createPolygon(coords);
                                    double thisarea = pg.getArea();
                                    double thisperimeter = pg.getLength();
                                    totalarea += thisarea;
                                    totalperimeter += thisperimeter;
                                    StringBuffer empty = new StringBuffer();
                                    empty.append("PG Area: ").append(thisarea).append(" ;").append(" PG Peri: ").append(thisperimeter).append(" ;\n");

                                    for (Coordinate cord : coords) {
                                        empty = empty.append(" X:").append(cord.x).append(", Y:").append(cord.y).append(" ;\n");
                                    }
                                    strbufretval.append("Polygon:\n");
                                    strbufretval.append(empty.toString()).append("\n");
                                    System.out.println("Polygon:");
                                    System.out.println(empty.toString());

                                }

                            }//while loop ends - we finished through all features of the shapefile
                        } catch (Exception e) {

                        } finally {
                            iterator.close();
                            store1.dispose();

                        }
                        //New Code Ends
                        //return String.valueOf(featureSource1.getFeatures().toArray().length);
                        return String.valueOf(strbufretval.toString());

                    } else {
                        System.out.println("featuresource1 = null");
                        return "-1";
                    }

                } else {
                    System.out.println("Store1 = null");
                    return "-1";
                }

            } else {
                return "-1";
            }
            //both files created ends

        }//try ends
        catch (Exception e) {
            System.out.println("caught an exception");
            return "-1";
        }//catch ends

    }//CountFeaturesNewOld shpx ends

    public static class ShapeCountReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            //Key here is the md5 hash while the values are all the image files that
            // are associated with it. for each md5 value we need to take only
            // one file (the first)
            Text ShapefilePath = null;
            for (Text filePath : values) {
                ShapefilePath = filePath;
                System.out.println("shapefilepath: " + ShapefilePath.toString() + " key: " + key.toString());
                //break;//only the first one
            }
            // In the result file the key will be again the image file path.
            context.write(ShapefilePath, key);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        //This is the line that makes the hadoop run locally
        //conf.set("mapred.job.tracker", "local");
        //Remember the above line...
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        //otherArgs[0] = "/user/shadoop/shapefiles";
        //otherArgs[1] = "/user/shadoop/shapefiles-out4";

        for (int h = 0; h < otherArgs.length; h++) {
            System.err.println(h +" is "+otherArgs[h]);
        }

        if (otherArgs.length <3) {
            //System.err.println(otherArgs.length);
            //System.err.println(otherArgs[0]);
            //System.err.println(otherArgs[1]);
            System.err.println("Usage: ShapeMapRedNew5 <in> <out>");
            System.exit(2);
        }

        // ADDED
        //FileContext fc = FileContext.getFileContext();
        // fc.createSymlink(new Path("/home/shadoop/Downloads/files1"), new Path("/usr/destination-link"), false);
        Job job = new Job(conf, "ShapeMapRedNew5");

        // ADDED for local run
        System.out.println("length is "+otherArgs.length);
        /*StringBuilder sb=new StringBuilder();
        for(int i=1;i<otherArgs.length-1;i++)
        {

            sb.append(otherArgs[i]).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println(sb);
        */
  
        StringBuilder sb=new StringBuilder();
        for(int i=1;i<otherArgs.length-1;i++)
        {

            System.out.println("selected names "+otherArgs[i]);
            sb.append(otherArgs[i]).append(",");

        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println("sb is " +sb);
//        File fl=new File("/home/shadoop/Documents/a.txt");
  //      Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(fl), "utf-8"));
   //writer.write(sb.toString());

        //String ss="places (1).shpx,places (10).shpx";
        FileInputFormat.addInputPaths(job,sb.toString());

        Date dat1 = new Date();
        System.out.println("Started ShapeMapRedNew5 at time: " + dat1.getTime() + "(ms)...");
        job.setJobName(ShapeMapRedNew5.class.getSimpleName());
        job.setJarByClass(ShapeMapRedNew5.class);

        job.setInputFormatClass(ShapeFileInputFormatNew.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapperClass(ShapeFileMapper.class);
        job.setReducerClass(ShapeCountReducer.class);

        //job.setNumReduceTasks(100);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // ShapeFileInputFormatNew.addInputPath(job, new Path(otherArgs[1]));
        TextOutputFormat.setOutputPath(job, new Path(otherArgs[(otherArgs.length-1)]));

        System.out.println("Completed...");
        System.exit(job.waitForCompletion(true) ? 0 : 1);
        Date dat2 = new Date();

        System.out.println("Completed ShapeMapRedNew5 at time: " + dat2.getTime() + "(ms)...");
        System.out.println("Time taken: " + (dat2.getTime() - dat1.getTime()));

    }
}
