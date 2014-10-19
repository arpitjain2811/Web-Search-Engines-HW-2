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
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {    
    Vector<ScoredDocument> all = new Vector<ScoredDocument>();
    
    Document i = _indexer.nextDoc(query, -1);
    
    while(i != null) {
      System.out.println("Next Called Doc "+i._docid);

      all.add(scoreDocument(query, i));
	    i = _indexer.nextDoc(query,i._docid);
    }
    
    Collections.sort(all, Collections.reverseOrder());
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    for (int j = 0; j < all.size() && j < numResults; ++j) {
      results.add(all.get(j));
    }
    return results;
  }

  private ScoredDocument scoreDocument(Query query, Document document) {

    double title_score = runquery_title(query, document);
    double cosine_score = runquery_cosine(query, document)

    double score = title_score + cosine_score;

    return new ScoredDocument(doc, score);
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
    // total number of docs, from indexer
    int num_docs = _indexer.numDocs();

    for (String queryToken : query._tokens){

      // number of occurrences of this term, from postings list
      int tf = _indexer.documentTermFrequency(queryToken, Integer.toString(doc._docid) );
      // number of docs word is in, from indexer
      int df = _indexer.corpusTermFrequency(queryToken);

      double idf = ( 1 + Math.log( (double) num_docs/df ) / Math.log(2) );
      score += tf * idf;
    }

    return score;
  }

}


