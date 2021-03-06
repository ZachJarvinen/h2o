package water.api;

import java.io.File;
import water.util.FileIntegrityChecker;

public class ImportFiles2 extends ImportFiles {
  static final int API_WEAVER=1; // This file has auto-gen'd doc & json fields
  static public DocGen.FieldDoc[] DOC_FIELDS; // Initialized from Auto-Gen code.

  // This Request supports the HTML 'GET' command, and this is the help text
  // for GET.
  static final String DOC_GET = 
    "Map a file from the local host filesystem into H2O memory.  Data is "+
    "loaded lazily, when the Key is read (usually in a Parse2 command, to build " +
    "a Frame key).  (Warning: Every host in the cluster must have this file visible locally!)";

  @Override FileIntegrityChecker load(File path) {
    return FileIntegrityChecker.check(path, true);
  }
  // Auto-link to Parse2
  @Override String parse() { return "Parse2.query"; }
}
