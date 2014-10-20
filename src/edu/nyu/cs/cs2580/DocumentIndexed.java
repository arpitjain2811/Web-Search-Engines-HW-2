package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.util.Vector;
import java.util.HashMap;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document implements Serializable {
    private static final long serialVersionUID = 9184892508124423115L;
    
    private static HashMap <Integer, Integer> _df = new HashMap<Integer,Integer>();
    private HashMap <Integer, Integer> _doc_tf = new HashMap<Integer,Integer>();
    private HashMap <Integer, Integer> _doc_tfidf = new HashMap<Integer,Integer>();
    
    public DocumentIndexed(int docid) {
	super(docid);
    }

    public void updateDocTermFreq(int idx) {
	if (_df.containsKey(idx)) {
	    int old = _df.get(idx);
	    _df.put(idx, old + 1);
	} else {
	    _df.put(idx, 1);
	}

	return;
    }

    public void updateTermFreq(int idx) {
	if (_doc_tf.containsKey(idx)) {
	    int old = _doc_tf.get(idx);
	    _doc_tf.put(idx, old + 1);
	} else {
	    _doc_tf.put(idx, 1);
	}

	return;
    }

    public void createTFIDF(int num_docs) {
	double total = 0.0;
      
	// Calculate tf*idf 
	for( int key : _doc_tf.keySet() )
	    {
		int tf = _doc_tf.get( key );
		int df = _df.get( key );
		      
		double idf = ( 1 + Math.log( (double) num_docs/df ) / Math.log(2) );
		_doc_tfidf.put( key, (int)(idf * tf*100) );
		total += idf*idf * tf*tf;

	    }
      
	//Normalize
	for( Integer key : _doc_tf.keySet() )
	    {
		_doc_tfidf.put( key, (int)((_doc_tfidf.get( key ) / Math.sqrt(total))*100) );
	    }
	_doc_tf = null;
	
	return;
    }

    public void removeDF(){
	_df = null;
	return;
    }

    public Double getTFIDF(Integer idx) {
	return _doc_tfidf.containsKey(idx) ? _doc_tfidf.get(idx) : 0.0;
    }


}


