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

  public RankerFavorite(Options options, CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(QueryPhrase query, int numResults) {    
    Vector<ScoredDocument> all = new Vector<ScoredDocument>();
    

    
    
    Document i = _indexer.nextDoc(query, -1);
    
    System.out.println("Next Called Doc "+i._docid);
    
    Double pos;
    int j;
    while(i != null) {
      System.out.println("Next Called Doc "+i._docid);
      
      System.out.println(query.phrase.size());
      if(query.phrase.size()>0)
      {
    	 
		  for(j=0;j<query.phrase.size();j++)
		  {
			 pos=_indexer.NextPhrase(query.phrase.get(j), i._docid, -1);
			 System.out.println(pos);
			 
			 if(pos==Double.POSITIVE_INFINITY)
			 {
				 
				 break;
			 }
			  
		  }
		  
		  if(j==query.phrase.size())
		  {
			  
			  all.add(scoreDocument(query, i));
			  
		  }
	  }
      
      else
      {
    	  
    	  all.add(scoreDocument(query, i));
    	  
    	  
      }
      
      //NExt phrase pos = -
      // check if returned position is valid
      	//if invaid then call next doc
      	//else if valid then score that document and then call next doc
      
      
      i = _indexer.nextDoc(query,i._docid);
      
	  
    }
    
    Collections.sort(all, Collections.reverseOrder());
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    
    for (int j1 = 0; j1 < all.size() && j1 < numResults; ++j1) {
      results.add(all.get(j1));
    }
    return results;
  }

  private ScoredDocument scoreDocument(Query query, Document document) {

    double title_score = runquery_title(query, document);
    double cosine_score = runquery_cosine(query, document);

    double score = title_score + cosine_score;

    return new ScoredDocument(document, score);
  }

  private double runquery_title(Query query, Document doc) {
    String title = ((DocumentIndexed) doc).getTitle();
    Vector<String> titleTokens = new Vector<String>( Arrays.asList(title.split(" ")) );    

    double size = (double) query._tokens.size();
    titleTokens.retainAll(query._tokens); 
    double score = titleTokens.size() / size;

    return score;
  }

  private double runquery_cosine(Query query, Document doc) {
    double score = 0.0;

    if (_options._indexerType.equals("inverted-doconly")) {
	for (String queryToken : query._tokens) {
	    int idx = ((IndexerInvertedDoconly) _indexer).getTerm(queryToken);
	    if (idx >= 0 ) 
		score += ((DocumentIndexed) doc).getTFIDF(idx);
	}
    } else {
	// total number of docs, from indexer
	int num_docs = _indexer.numDocs();
	for (String queryToken : query._tokens){
	    
	    // number of occurrences of this term, from postings list
	    int tf = _indexer.documentTermFrequency(queryToken, Integer.toString(doc._docid) );
	    // number of docs word is in, from indexer
	    int df = _indexer.corpusDocFrequencyByTerm(queryToken);
	    
	    double idf = ( 1 + Math.log( (double) num_docs/df ) / Math.log(2) );
	    score += tf * idf;
	    
	    System.out.println(queryToken + ' ' + tf + ' ' + df + ' ' + idf + ' ' + score);
	}
	score = Math.log(score);
    }   
    return score;
  }
}


