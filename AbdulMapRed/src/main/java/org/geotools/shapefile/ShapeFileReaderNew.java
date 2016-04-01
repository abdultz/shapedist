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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * This RecordReader implementation hands individual Shapefiles to the Mapper.
 * The "key" is the file name, the "value" is the file contents.
 */
public class ShapeFileReaderNew
        extends RecordReader<Text, BytesWritable> {

    /**
     * InputStream used to read the Shapefile from the FileSystem
     */
    private FSDataInputStream fsin;

    /**
     * Configuration
     */
    private Configuration conf;

    /**
     * Shapefile Variable
     */
    private FileInputStream file;

    /**
     * Current Shapefile name
     */
    private Text currentKey;

    /**
     * Shapefile contents
     */
    private BytesWritable currentValue;

    /**
     * Used to indicate progress
     */
    private boolean isFinished = false;

    /**
     * Path of the Shapefile on HDFS
     */
    private Path path;
    
    /**
     * Has the shapefile been processed?
     */
    private boolean processed = false;

    /**
     * Initialize and open the Shapefile from the FileSystem
     */
    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
            throws IOException, InterruptedException {
        System.out.println("Reader Initialized");
        FileSplit split = (FileSplit) inputSplit;
        conf = taskAttemptContext.getConfiguration();
        path = split.getPath();
        FileSystem fs = path.getFileSystem(conf);
        System.out.println("File Opened: "+path.toUri().toString());
        // Open the stream
        fsin = fs.open(path);
        if (fsin.getFileDescriptor() != null) {
            file = new FileInputStream(fsin.getFileDescriptor());
        }
    }

    /**
     * This is where the magic happens, Shapefile is readied for the Mapper. The
     * contents of Shapefile is held *in memory* in a BytesWritable object.
     */
    @Override
    public boolean nextKeyValue()
            throws IOException, InterruptedException {
        InputStream istr = null;
        
        if (!processed) {
            try {

                istr = FileSystem.get(conf).open(path);
                currentKey = new Text(path.toString());
                
                // Read the file contents
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] temp = new byte[8192];
                while (true) {
                    int bytesRead = 0;
                    try {
                        bytesRead = istr.read(temp, 0, 8192);
                    } catch (EOFException e) {

                    }
                    if (bytesRead > 0) {
                        bos.write(temp, 0, bytesRead);
                    } else {
                        break;
                    }
                }
                istr.close();

                // Shapefile contents
                currentValue = new BytesWritable(bos.toByteArray());
                System.out.println("FileBytes: "+ bos.size() +" provided to mapper");
                System.out.println("Next Key Value COMPLETED");
                processed = true;
                return true;
                //True if there are more files to process
            } catch (IOException e) {
                System.out.println("IOException here...");
            } catch (Exception e) {
                System.out.println("Exception here...");
            }
            //This file appears to be generating Exception... Skipping...
            processed = true;
            return true;
        }
        //else return false
        return false;
        
        
    }

//    {
//              
//        // Filename
//        currentKey = new Text(path.toString());
//        
//        // Read the file contents
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] temp = new byte[8192];
//        while ( true )
//        {
//            int bytesRead = 0;
//            try
//            {
//                bytesRead = file.read( temp, 0, 8192 );
//            }
//            catch ( EOFException e )
//            {
//               
//            }
//            if ( bytesRead > 0 )
//                bos.write( temp, 0, bytesRead );
//            else
//                break;
//        }
//        file.close();
//        
//        // Uncompressed contents
//        currentValue = new BytesWritable( bos.toByteArray() );
//        return true;
//    }
    /**
     * Rather than calculating progress, we just keep it simple
     */
    @Override
    public float getProgress()
            throws IOException, InterruptedException {
        return isFinished ? 1 : 0;
    }

    /**
     * Returns the current key (name of the zipped file)
     */
    @Override
    public Text getCurrentKey()
            throws IOException, InterruptedException {
        return currentKey;
    }

    /**
     * Returns the current value (contents of the zipped file)
     */
    @Override
    public BytesWritable getCurrentValue()
            throws IOException, InterruptedException {
        return currentValue;
    }

    /**
     * Close quietly, ignoring any exceptions
     */
    @Override
    public void close()
            throws IOException {
        try {
            file.close();
        } catch (Exception ignore) {
        }
        try {
            fsin.close();
        } catch (Exception ignore) {
        }
    }
}
