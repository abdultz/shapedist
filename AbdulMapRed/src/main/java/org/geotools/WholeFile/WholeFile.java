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

import org.geotools.WholeFile.WholeFileInputFormat;
import cascading.scheme.Scheme;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import java.io.IOException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;

public class WholeFile extends Scheme {
    public WholeFile( Fields fields ) {
        super(fields);
    }

    @Override
    public void sourceInit( Tap tap, JobConf conf ) {
        conf.setInputFormat( WholeFileInputFormat.class );
    }
    
    @Override
    public void sinkInit(Tap tap, JobConf conf) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Tuple source( Object key, Object value )
    {
        Tuple tuple = new Tuple();
        tuple.add(key.toString());
        tuple.add(value);
        return tuple;
    }
    
    @Override
    public void sink(TupleEntry te, OutputCollector oc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
} 
