package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;


import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable {

	 private static final long serialVersionUID = 1626440145434710491L;
	    
	    private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
	    private Map<String, Vector<Integer>> _decoded = new HashMap<String, Vector<Integer>>();
	    private HashMap<Integer, Integer> elias = new HashMap<Integer,Integer>();
	    private HashMap<Integer, BitSet > _postings=new HashMap<Integer,BitSet>();
	    private Vector<Document> _documents=new Vector<Document>();
	    
	    private Map<Integer, Integer> _termCorpusFrequency =
		new HashMap<Integer, Integer>();
	    private Map<Integer, Integer> _termDocFrequency =
		new HashMap<Integer, Integer>();
		private Map<String, Integer> cache = new HashMap<String, Integer>();
		private Integer c_t = -1;
	    private HashMap<Integer,Vector<Integer>> _term_position = new HashMap<Integer,Vector<Integer>>();
	    private HashMap<Integer,Vector<Integer>> _skip_pointer=new HashMap<Integer,Vector<Integer>>();
	    private HashMap<Integer,Vector<Integer>> _term_list=new HashMap<Integer,Vector<Integer>>();
	    
	
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {

      ReadCorpus DocReader = new ReadCorpus();
      
      String corpusDir = _options._corpusPrefix;
      System.out.println("Constructing index documents in: " + corpusDir);

      final File Dir = new File(corpusDir);
      int n_doc = 0;
      for (final File fileEntry : Dir.listFiles()) {
	  if ( !fileEntry.isDirectory() ) {
	      
	      // dont read hidden files
	      if(fileEntry.isHidden())
		  continue;
	    
	      // special case for testing with corpus.tsv
	      if (fileEntry.getName().endsWith("corpus.tsv") ) {
		  System.out.println(fileEntry.getName());
		  BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
		  try {
		      String line = null;
		      while ((line = reader.readLine()) != null) {
			  System.out.println("Document" + n_doc);
			  line = DocReader.createFileInput(line);
			  processDocument(line);
			  _term_position.clear();
			  
			  n_doc++;
		      }
		  } 
		  finally {
		      reader.close();
		  }
	      }
	      else {
		  System.out.println(n_doc + "   " + fileEntry.getName());
		  
		  n_doc++;
		  String nextDoc = DocReader.createFileInput(fileEntry);
		  processDocument(nextDoc);
		  
		  _term_position.clear();
		  
	      }
	  } 
      }

      DocReader = null;
	
      /*
	String corpusFile = _options._corpusPrefix + "/corpus.tsv";
	System.out.println("Construct index from: " + corpusFile);
	
	int n_doc = 0;
	BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
	try {
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		System.out.println("Document"+n_doc);
		processDocument(line);
		_term_position.clear();
		
		n_doc++;
	      }
	} finally {
	    reader.close();
	}
      */
	System.out.println(
			   "Indexed " + Integer.toString(_numDocs) + " docs with " +
			   Long.toString(_totalTermFrequency) + " terms.");
	
	String indexFile = _options._indexPrefix + "/corpus.idx";
	System.out.println("Store index to: " + indexFile);
	ObjectOutputStream writer =
	    new ObjectOutputStream(new FileOutputStream(indexFile));
	
	// temporary vectors
	Vector<Integer> list = new Vector<Integer>();
	Vector<Integer> skip = new Vector<Integer>();
	Vector<Integer> posting = new Vector<Integer>();
	
	BitSet bits=new BitSet();
	
	System.out.println(_dictionary.size());
	
	for(int i : _dictionary.values()) {
	    // get the term position list and skip pointer
	    list =_term_list.get(i);
	    skip = _skip_pointer.get(i);
	    
	    // create final posting for term
	    posting = update_skip(skip,skip.size());
	    posting.addAll(list);
	    
	    bits=elias_encode(posting,i);

	    _postings.put(i, bits);
	    
	    
	    bits=new BitSet();
	    
	}
	
	
	
	_term_list=null;
	_skip_pointer=null;
	_term_position=null;
	
	try{
		
	
	writer.writeObject(this);
	writer.close();
	}
	catch(Exception e)
	{
		System.out.println(e.toString());
	}
	
	
    }
    
    private BitSet elias_encode(Vector<Integer> posting, int i2) {
	// TODO Auto-generated method stub
    	
    	BitSet b=new BitSet();
    	int symbol;
    	int d;
    	int r;
    	int idx=0;
    	
    	for(int i=0;i<posting.size();i++)
    	{
    		
    		symbol=posting.get(i);
    		symbol++;
    		
    		d= (int) (Math.log(symbol)/Math.log(2));
    		
    		r= (int) (symbol - Math.pow(2, d));
    		
    		
//    		System.out.println(idx);
//    		System.out.println(b.size());
//    		System.out.println("D: " +d);
//    		System.out.println("R: " +r);
    		
    		//System.out.println("Symbol: " +symbol);
    		
    		b.set(idx, idx+d);
    		
    		idx=idx+d;
    		
    		
    		
    		b.set(idx, false);
    		
    		idx=idx+1;
    		
    		
    		
    		
    		
    		for(int j=d-1;j>=0;j--)
    		{
    			
    			if(r-Math.pow(2, j)>=0)
    			{
    				b.set(idx);
    				idx=idx+1;
    				r=(int) (r-Math.pow(2, j));
    			}
    			else
    			{
    				b.set(idx,false);
    				
    				idx=idx+1;
    				
    			}
    		}
    	}
    	
   	elias.put(i2, idx);
    	
	return b;
}



	private Vector<Integer> update_skip(Vector<Integer> skip, int size) {
	// TODO Auto-generated method stub
	
	// add skip list size to each index per document
	for(int i = 0; i < skip.size(); i++) {
	    if(i % 2 != 0) {
		skip.set(i, skip.get(i) + size);
	    }
	}
	return skip;
    }
    
    private void processDocument(String content) {
	// TODO Auto-generated method stub
	
	Scanner s = new Scanner(content).useDelimiter("\t");
	Set<Integer> uniqueTerms = new HashSet<Integer>();

	// pass the title 
	String title = s.next();
	//readTermVector(title, uniqueTerms);
	
	// pass the body
	readTermVector(s.next(), uniqueTerms);
	
	// get number of views
	int numViews = Integer.parseInt(s.next());
	//System.out.println(numViews);

	String url = s.next();
	
	s.close();
	
	// create the document
	DocumentIndexed doc = new DocumentIndexed(_documents.size());
	doc.setTitle(title);
	doc.setNumViews(numViews);
	doc.setUrl(url);
	System.out.println(url);

	// add the document
	_documents.add(doc); 
	_numDocs++;
	
	// create postings lists and skip pointers
	Vector<Integer> positions=new Vector<Integer>();
	Vector<Integer> list=new Vector<Integer>();
	Vector<Integer> skip=new Vector<Integer>();
	for (Integer idx : uniqueTerms) {
	    // increase number of docs this term appears in
	    _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);

	    // get the vectors
	    skip = _skip_pointer.get(idx);
	    list = _term_list.get(idx);
	    positions = _term_position.get(idx);
	    
	   // System.out.println(_dictionary.get(idx));
	  //  positions=delta_encode(positions,idx);
	    
	    // add document ID
	    list.add(_documents.size()-1);
	    // add number of occurrences
	    list.add(positions.size());
	    // add all the positions in the document
	    
	    list.addAll(positions);
	    
	    // add document ID
	    skip.add(_documents.size()-1);
	    // add how far to skip to the last element of this documents list
	    skip.add(list.size()-1);
	    
	    // set it
	    _skip_pointer.put(idx, skip);
	    _term_list.put(idx, list);
	    
	}
	
    }
  
    private Vector<Integer> delta_encode(Vector<Integer> positions,int idx) {
		// TODO Auto-generated method stub
		
    	int last=0;
    	int current;
    	
    	for (int i = 0; i < positions.size(); i++)
        {
             current = positions.get(i);
             
            if( positions.get(i)-last<0)
            {
            	System.out.println(positions.get(i)+ " "+ last + " "+ positions);
            	
            	for(String l:_dictionary.keySet())
            	{
            		if(_dictionary.get(l).equals(idx))
            			System.out.println(l);
            	}
            }	
            positions.set(i, positions.get(i)-last);
            last = current;
        }
    
    	return positions;
	}

	private void readTermVector(String content, Set <Integer> uniques) {
	Scanner s = new Scanner(content);  // Uses white space by default.
	int pos = -1;
	Vector<Integer> positions = new Vector<Integer>();
	while (s.hasNext()) {
		
	    String token = s.next();
	    pos++;
	    int idx = -1;
	    
	    // get index from the dictionary or add it
	    if (!_dictionary.containsKey(token)) {
		idx = _dictionary.size();
		_dictionary.put(token, idx);
		
	        _termCorpusFrequency.put(idx, 0);
	        _termDocFrequency.put(idx, 0);
	
		// create these things for new word
		_skip_pointer.put(idx, new Vector<Integer>());
		_term_list.put(idx, new Vector<Integer>());
		_term_position.put(idx, new Vector<Integer>());
		_postings.put(idx,new BitSet());
	
	    } else {
		idx = _dictionary.get(token);
	    }
	    
	    // make sure term is in term_position
	    if (!_term_position.containsKey(idx)) {
		_term_position.put(idx, new Vector<Integer>() );
	    }


	    // add position of the term
	    positions = _term_position.get(idx);
	    positions.add(pos);
	    _term_position.put(idx, positions);

	    // add term to the unique set
	    uniques.add(idx);
	    
	    // update stats
	    _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
	    ++_totalTermFrequency;

	    
	}
	return;
    }
    
  
    @Override
    public void loadIndex() throws IOException, ClassNotFoundException {
	
	String indexFile = _options._indexPrefix + "/corpus.idx";
	System.out.println("Load index from: " + indexFile);

	// read in the index file
	ObjectInputStream reader =
	    new ObjectInputStream(new FileInputStream(indexFile));
	IndexerInvertedCompressed loaded = 
	    (IndexerInvertedCompressed) reader.readObject();
	
	this._documents = loaded._documents;
	this._numDocs = _documents.size();
	
	//	Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
	    for (Integer freq : loaded._termCorpusFrequency.values())
		this._totalTermFrequency += freq;

	   
	    
	    
	this._postings = loaded._postings;
	this._dictionary = loaded._dictionary;
	this._termCorpusFrequency = loaded._termCorpusFrequency;
	this._termDocFrequency = loaded._termDocFrequency;
	this.elias=loaded.elias;
	reader.close();
	
	System.out.println(Integer.toString(_numDocs) + " documents loaded " +
			   "with " + Long.toString(_totalTermFrequency) + " terms!"); 
	
    }

    
    @Override
    public Document getDoc(int docid) {
	return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
    }
    
  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
  
 private Double next(String t, Integer current) {
	
	// get the postings list
	
	
	
	Vector<Integer> Pt=_decoded.get(t);
	

	
	
	// get index of last doc
	int lt = get_lt(Pt); 
	// done if already returned the last one
	if(lt == -1 || Pt.get(lt) <= current)
	    return Double.POSITIVE_INFINITY;
	
	// first time return the first doc
	if(Pt.get(0) > current) {
	    c_t = 0;
	    return 1.0 * Pt.get(c_t);
	}
	
	// go back 
	if(c_t > 0 && Pt.get(c_t-2) <= current)
	    c_t = 0;
	
	// go find next doc
	while (Pt.get(c_t) <= current && c_t < lt)
	    c_t = c_t+2;
	
	// return the docid
	return 1.0*Pt.get(c_t);
    }
    
    @Override
public Document nextDoc(Query query, int docid) {
	
    
	Vector<Double> docids = new Vector<Double>(query._tokens.size());
	
	
	for(int i=0;i<query._tokens.size();i++)
	{ 
		
		if(!_decoded.containsKey(query._tokens.get(i)))
		{

		 
		_decoded.put(query._tokens.get(i), Elias_decode(_postings.get(_dictionary.get(query._tokens.get(i))), query._tokens.get(i)));
		
		}	
	}
  
	
	// get next doc for each term in query
	for(int i = 0; i < query._tokens.size(); i++) {
		
		String token = query._tokens.get(i);
		c_t = cache.containsKey(token) ? cache.get(token) : -1;
		
	    docids.add(i, next(query._tokens.get(i), docid) );

   		cache.put(token, c_t);

	    if(docids.get(i) == Double.POSITIVE_INFINITY)
		return null;
	}
	
	// found the next one
	if(Collections.max(docids) == Collections.min(docids)) 
	    return _documents.get(docids.get(0).intValue());
   
	// not a match, run again
	return nextDoc(query, Collections.max(docids).intValue()-1);
    
    }
    
    // return index that of last doc in skip pointer list
    private int get_lt(Vector<Integer> pt) {
	// TODO Auto-generated method stub
	int sz = pt.size();
	int i = 0;
	
	// return -1 if no doc present
	  if(pt.size()==0)
	      return -1;
	  
	  while(true) {
	      // check if the skip pointer goes to end of posting list
	      if(pt.get(i+1) == sz-1)
		  return i;
	      // go to next dox
	      i=i+2;
	  }
    }
    

private int get_doc_start(Vector<Integer> pt, int docid) {
	
	int lt = get_lt(pt);
	if (lt == -1) 
	    return -1;
	
	int cur_doc = -1;
	int i = 0;
	// find the doc id in skip pointer list
	while (i <= lt) {
	    cur_doc = pt.get(i);
	    // did not find, continue
	    if (cur_doc != docid) {
		i += 2;
	    } else {
		// if it was the first doc
		// skip over ptr_indx, docid, num_occ
		if (i == 0)
		    return lt + 4;
		else 
		    // go to prev doc ptr, jump, then skip docid, num_occ
		    return pt.get(i - 1) + 3;
	    }
	}
	return -1;
    }
    
    private int get_doc_end(Vector<Integer> pt, int docid) {
	
	int lt = get_lt(pt);
	if (lt == -1) {
	    return -1;
	}
	
	int cur_doc = -1;
	int i = 0;
	// find the doc id in skip pointer list
	while (i <= lt) {
	    cur_doc = pt.get(i);
	    // did not find, continue
	    if (cur_doc != docid) {
		i += 2;
	    } else {
		// found, return end position of the occurrences list for that doc
		return pt.get(i+1);
	    }
	}
	
	// you shouldnt ever get here
	return -1;
    }


    @Override
public double NextPhrase(Query query, int docid, int pos) {

    //	System.out.println(query._tokens);
	// doing what the psuedo code says to do
	Document doc = nextDoc(query, docid-1);
	int doc_verify = doc._docid;
	if(doc_verify!=docid) 
	    return Double.POSITIVE_INFINITY;
	
	// get the position of each query term in doc
	Vector<Double> pos_vec = new Vector<Double>(query._tokens.size());
	for(int i = 0; i<query._tokens.size(); i++) {
	    
	    //
	    Double it = next_pos(query._tokens.get(i), docid, pos);
	    pos_vec.add(i, it);
	   // System.out.println(query._tokens.get(i) + " "+ pos_vec.get(i));
	    if(pos_vec.get(i) == Double.POSITIVE_INFINITY)
		return Double.POSITIVE_INFINITY;
	}
	
	int incr = 1;
	for(int j = 0; j < pos_vec.size() - 1; j++) {
	    if(pos_vec.get(j)+1 == pos_vec.get(j+1))
		incr++;
	}
	
	if(incr == pos_vec.size())
		return pos_vec.get(0);
	
	int next_p=Collections.max(pos_vec).intValue();
	
	return NextPhrase(query, docid,next_p ); 
	
    }
    
private Double next_pos(String token, int docid, int pos) {
	// TODO Auto-generated method stub
	
	Vector<Integer> Pt=_decoded.get(token);
	
	// end of occurrence list for doc
	int indx_end = get_doc_end(Pt, docid);
	
	
	
	// if cur position is at or past the last occurence, no more possible phrases
	if( indx_end == -1 || Pt.get(indx_end) < pos)
	    return Double.POSITIVE_INFINITY;
	    
	// get the index of the first position
	int indx_start = get_doc_start(Pt, docid);
	
	// first time called return the first occurrence
	if (Pt.get(indx_start) > pos)
	    return 1.0 * Pt.get(indx_start);
	
	// iterate through position list until you pass current position
	int i = indx_start;
	for(; Pt.get(i) < pos; i++);
	
	// return that next position
	return 1.0 * Pt.get(i);
	
    }
    
 private Vector<Integer> Elias_decode(BitSet b, String t) {
	// TODO Auto-generated method stub
	 
	 int start=0;
	 int end=0;
	 int first_zero;
	 int d;
	 int rem = 0;
	 int k;
	 
	 Vector<Integer> pt=new Vector<Integer>();
	 BitSet r;
	
	 
	
	while(end<elias.get(_dictionary.get(t))-1)
	{
		rem=0;
		first_zero=b.nextClearBit(start);
   		
		end=first_zero+first_zero-start;
		
   		d=(int) Math.pow(2, first_zero-start);
   		
   		if(d==1)
   			rem=0;
   		
   		else
   		{
   			
   		r=new BitSet();
	   	r=b.get(first_zero+1, end+1);
	   	
	   	int r_size=end-first_zero;
	   	
	   	
   		for(int n=0;n<r_size;n++)
   		{
   			int myInt = (r.get(n)) ? 1 : 0;
   		
   			rem=(int) (rem+ Math.pow(2, (r_size-1)-n) * myInt);
   		}
   		}
   		k=rem+d;
   		k--;
   		pt.add(k);
   		start=end+1;
		
	}
	
	
	return pt;
}


  

@Override
  public int corpusDocFrequencyByTerm(String term) {
	  return _dictionary.containsKey(term) ?
		        _termDocFrequency.get(_dictionary.get(term)) : 0;
  }

  @Override
  public int corpusTermFrequency(String term) {
	  return _dictionary.containsKey(term) ?
		        _termCorpusFrequency.get(_dictionary.get(term)) : 0;
  }

  @Override
  public int documentTermFrequency(String term, String did) {
  	int docid = Integer.parseInt(did);
  	if (_dictionary.containsKey(term)) {
  		Vector<Integer> Pt = _decoded.get(term);
	  	// index for positions of term in the doc
		int positions_indx = get_doc_start(Pt, docid);
		return positions_indx != -1 ? Pt.get(positions_indx - 1) : 0;
  	}
    return 0;
  }


}
