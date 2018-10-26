/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import org.apache.log4j.Logger;


/**
 * A Pipe is a thread which reads some data from a Reader and writes
 * it to a Writer line by line until EOF is reached in the Reader.
 */
public class Pipe extends Thread
{
    static Logger logger = Logger.getLogger("util.pipe");

    /**
     * Creates a new pipe.
     * @param input the input of the pipe
     * @param output the ouput of a pipe
     */
    public Pipe(Reader input, Writer output) {
        this.input = new BufferedReader(input);
        this.output = new PrintWriter(output);
    }

    /**
    * Creates a new pipe.
    * @param input the input of the pipe
    * @param output the ouput of a pipe
    */
    public Pipe(InputStream input, OutputStream output) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new PrintWriter(new OutputStreamWriter(output));
    }

    BufferedReader input;
    PrintWriter output;

    public void run() {
        try {
            String line;
            while ((line=input.readLine())!=null) {
                System.out.println(line);
                // this does not work, why ?
                //output.println(line);
            }
        } catch(Exception e) {
            logger.error("Caught exception in Pipe thread: "+e);
        }
    }
}
