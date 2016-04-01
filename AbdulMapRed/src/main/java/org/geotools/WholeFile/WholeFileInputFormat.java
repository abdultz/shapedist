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

package org.geotools.WholeFile;

import java.io.IOException;

import org.geotools.WholeFile.WholeFileRecordReader;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class WholeFileInputFormat extends FileInputFormat<Text, BytesWritable> {
    @Override
    protected boolean isSplitable(FileSystem fs, Path filename) {
        return false;
    }

    @Override
    public RecordReader<Text, BytesWritable> getRecordReader(
      InputSplit split, JobConf job, Reporter reporter) throws IOException {
        return new WholeFileRecordReader((FileSplit) split, job);
    }
}