package org.objectweb.jac.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.List;

/**
 * Configuration file parser interface
 */
public interface InputStreamParser {
   /**
    * Parse a configuration stream.
    * @param input the stream to parse
    * @param filePath corresponding filename
    * @param targetClass name of aspect component class
    * @param blockKeywords additional block keywords to factorize
    * configuration methods
    */
   List parse(InputStream input, String filePath, 
              String targetClass, Set blockKeywords) 
      throws IOException;
}
