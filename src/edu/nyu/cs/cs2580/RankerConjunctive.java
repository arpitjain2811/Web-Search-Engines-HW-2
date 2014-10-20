package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;



import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * Instructors' code for illustration purpose. Non-tested code.
 * 
 * @author congyu
 */
public class RankerConjunctive extends Ranker {

  public RankerConjunctive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
  
	  Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
  
    int docid = -1;
    
    
    QueryPhrase qp=new QueryPhrase(query._raw);
    qp.processQuery();
    
    System.out.println(qp.phrase.size());
    
    Document i = _indexer.nextDoc(query, -1);
    
    System.out.println("Next Called Doc "+i._docid);
    
    Double pos;
    int j;
  
    
    while(i != null){
    	
    	
    	System.out.println(qp.phrase.size());
        if(qp.phrase.size()>0)
        {
      	 
  		  for(j=0;j<qp.phrase.size();j++)
  		  {
  			 pos=_indexer.NextPhrase(qp.phrase.get(j), i._docid, -1);
  			 System.out.println(pos);
  			 
  			 if(pos==Double.POSITIVE_INFINITY)
  			 {
  				 
  				 break;
  			 }
  			  
  		  }
  		  
  		  if(j==qp.phrase.size())
  		  {
  			  
  			rankQueue.add(scoreDocument(query, i));
  			  
  		  }
  	  }
        
        else
        {
        	
        	rankQueue.add(scoreDocument(query, i));
        	
        }
    	
     
      if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
      docid = i._docid;
      
      i = _indexer.nextDoc(query,i._docid);
    }

    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
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
	    
	   // System.out.println(queryToken + ' ' + tf + ' ' + df + ' ' + idf + ' ' + score);
	}
	score = Math.log(score);
    }   
    return score;
  }

}
