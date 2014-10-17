package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */

/**
 * This Ranker makes a full scan over all the documents in the index. It is the
 * instructors' implementation of the Ranker in HW1.
 * 
 * @author fdiaz
 * @author congyu
 */
class RankerFavorite extends Ranker {

  public RankerFavorite(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {    
    Vector<ScoredDocument> all = new Vector<ScoredDocument>();
    
    
    Document i=_indexer.nextDoc(query, -1);
    
    while(i!=null)
    {
    	System.out.println(i._docid);
    	
    	all.add(scoreDocument(query, i));
    	
    	System.out.println("Next Called Doc"+i._docid);
    	i=_indexer.nextDoc(query,i._docid);
    	
    }
    

	
    
    
    Collections.sort(all, Collections.reverseOrder());
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    for (int j = 0; j < all.size() && j < numResults; ++j) {
      results.add(all.get(j));
    }
    return results;
  }

  private ScoredDocument scoreDocument(Query query, Document i) {
    // Process the raw query into tokens.
    query.processQuery();

    // Get the document tokens.
    Document doc = i;
    
    String docTokens = ((DocumentIndexed) doc).getTitle();

    
    Vector<String> dToken=new Vector<String>(Arrays.asList(docTokens.split(" ")));
    
    // Score the document. Here we have provided a very simple ranking model,
    // where a document is scored 1.0 if it gets hit by at least one query term.
    double score = 0.0;
    for (String docToken : dToken) {
      for (String queryToken : query._tokens) {
        if (docToken.equals(queryToken)) {
          score += 1.0;
          
        }
      }
      if (score > 0.0) {
        break;
      }
    }
    return new ScoredDocument(doc, score);
  }
}

