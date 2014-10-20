package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public Vector<String> _tokens = new Vector<String>();
  public ReadCorpus Cleaner = new ReadCorpus();
  public String _raw;

  public Query(String query) {
    _query = query;
    _raw=query;
  }
  
  public String getQuery()
    {
	return _query;
    }
    
  public void processQuery() {
    if (_query == null) {
      return;
    }
    _query=_query.replace('+', ' ');
    // **** clean and stem query ****
    try {
	_query = Cleaner.cleanAndStem(_query);
    }
    catch (IOException e) {
	System.err.println("Could not clean query: " + e.getMessage());
    }
    // **************
    Scanner s = new Scanner(_query);
    while (s.hasNext()) {
      _tokens.add(s.next());
    }
    s.close();
  }
}
