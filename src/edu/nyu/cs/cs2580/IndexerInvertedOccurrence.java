package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
public class IndexerInvertedOccurrence extends Indexer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1626440145434710491L;
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
//	private Vector<String> _terms = new Vector<String>();
	private HashMap<Integer, Vector<Integer> > _postings=new HashMap<Integer,Vector<Integer>>();
	private Vector<Document> _documents=new Vector<Document>();
	
	private Map<Integer, Integer> _termCorpusFrequency =
		      new HashMap<Integer, Integer>();
	private Map<Integer, Integer> _termDocFrequency =
		      new HashMap<Integer, Integer>();
	private Integer c_t=-1;
	private HashMap<Integer,Vector<Integer>> _term_position = new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> _skip_pointer=new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> _term_list=new HashMap<Integer,Vector<Integer>>();
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  public void constructIndex() throws IOException {
		 
	  String corpusFile = _options._corpusPrefix + "/corpus.tsv";
	  System.out.println("Construct index from: " + corpusFile);

	  int n_doc=0;
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
	    System.out.println(
	        "Indexed " + Integer.toString(_numDocs) + " docs with " +
	        Long.toString(_totalTermFrequency) + " terms.");

	    String indexFile = _options._indexPrefix + "/corpus.idx";
	    System.out.println("Store index to: " + indexFile);
	    ObjectOutputStream writer =
	        new ObjectOutputStream(new FileOutputStream(indexFile));
	    
	    Vector<Integer> list=new Vector<Integer>();
	    Vector<Integer> skip=new Vector<Integer>();
	    Vector<Integer> posting=new Vector<Integer>();
	    
	    
	    System.out.print("Dic");
	    for(int i: _dictionary.values())
	    {
	    	list=_term_list.get(i);
	    	skip=_skip_pointer.get(i);
	    	
	    	posting= update_skip(skip,skip.size());
	    	posting.addAll(list);
	    	_postings.put(i, posting);
	    	
	    }
	    
	    
	    System.out.print("Dic");
	    _term_list=null;
	    _skip_pointer=null;
	    _term_position=null;
	   
	    writer.writeObject(this);
	    writer.close();
	  
  }

  private Vector<Integer> update_skip(Vector<Integer> skip, int size) {
	// TODO Auto-generated method stub
	  
	  for(int i=0;i<skip.size();i++)
	  {
		  if(i%2!=0)
		  {
			  skip.set(i, skip.get(i)+size);
		  }
	  }
	  
	return skip;
}

private void processDocument(String content) {
	// TODO Auto-generated method stub
	  
	  Scanner s = new Scanner(content).useDelimiter("\t");

	  
	    String title = s.next();
	    Vector<Integer> titleTokens = new Vector<Integer>();
	    
	    readTermVector(title, titleTokens);

	    Vector<Integer> bodyTokens = new Vector<Integer>();
	    
	    readTermVector(s.next(), bodyTokens);
	    
	    
	    int numViews = Integer.parseInt(s.next());
	    s.close();

	    
		DocumentIndexed doc = new DocumentIndexed(_documents.size());
	    
		
		doc.setTitle(title);
	    doc.setNumViews(numViews);
	    
	    _documents.add(doc); 
	  
	    _numDocs++;
	    
	    Set<Integer> uniqueTerms = new HashSet<Integer>();
	    
	    updateStatistics(titleTokens, uniqueTerms);
	    
	    updateStatistics(bodyTokens, uniqueTerms);
	    
	   
	    Vector<Integer> positions=new Vector<Integer>();
	    Vector<Integer> list=new Vector<Integer>();
	    Vector<Integer> skip=new Vector<Integer>();
	    
	    
	    for (Integer idx : uniqueTerms) {
	      _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);

	      
	      
	      skip=_skip_pointer.get(idx);
	      
	      list=_term_list.get(idx);
	      
	      positions=_term_position.get(idx);
	      

	      list.add(_documents.size()-1);
	      list.add(positions.size());
	      

	      
	      list.addAll(positions);
	      
	      skip.add(_documents.size()-1);
	      skip.add(list.size()-1);
	      
	      _skip_pointer.put(idx, skip);
	      _term_list.put(idx, list);
	      
	    
	      
	    }
	    
	    
	
}
  
  private void readTermVector(String content, Vector<Integer> tokens) {
	    Scanner s = new Scanner(content);  // Uses white space by default.
	    int pos=0;
	    Vector<Integer> positions=new Vector<Integer>();
	    while (s.hasNext()) {
	       
	      String token = s.next();
	      int idx = -1;
	   
	      
	      
	      if (_dictionary.containsKey(token)) {
	        idx = _dictionary.get(token);
	        
	        
	        
	        if(!_term_position.containsKey(idx))
	        {
	        	_term_position.put(idx, new Vector<Integer>());
		        
	        }
	        
	        positions=_term_position.get(idx);
	        positions.add(pos);
	        
	        _term_position.put(idx, positions);
	        
	        
	      } else {
	    	 
	        idx =_dictionary.size();
//	        _terms.add(token);
	        if(idx==132)
	        	System.out.println(token);
	        _dictionary.put(token, idx);
	        
	        _termCorpusFrequency.put(idx, 0);
	        _termDocFrequency.put(idx, 0);
	        
	        
	        
	        
	        
	        _skip_pointer.put(idx, new Vector<Integer>());
	        _term_list.put(idx, new Vector<Integer>());
	        _term_position.put(idx, new Vector<Integer>());
	        positions=_term_position.get(idx);
	        positions.add(pos);
	        _term_position.put(idx, positions);
	        
	        _postings.put(idx,new Vector<Integer>());
	      }
	      tokens.add(idx);
	      pos++;
	     
	    }
	    return;
	  }
	  
  
private void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques) {
   for (int idx : tokens) {
     uniques.add(idx);
     _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
     ++_totalTermFrequency;
   }
 }
  
@Override
  public void loadIndex() throws IOException, ClassNotFoundException {
	  
	String indexFile = _options._indexPrefix + "/corpus.idx";
    System.out.println("Load index from: " + indexFile);

    ObjectInputStream reader =
        new ObjectInputStream(new FileInputStream(indexFile));
    IndexerInvertedOccurrence loaded = (IndexerInvertedOccurrence) reader.readObject();

    this._documents = loaded._documents;
    // Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
    this._numDocs = _documents.size();
    for (Integer freq : loaded._termCorpusFrequency.values()) {
      this._totalTermFrequency += freq;
    }
    this._postings=loaded._postings;
    this._dictionary = loaded._dictionary;
//    this._terms = loaded._terms;
    this._termCorpusFrequency = loaded._termCorpusFrequency;
    this._termDocFrequency = loaded._termDocFrequency;
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
  
  private Double next(String t,Integer current)
  {
	Vector<Integer> Pt=new Vector<Integer>();
	
	
	
	int low,high,jump;
	
	Pt=_postings.get(_dictionary.get(t));
	int lt= get_lt(Pt);
	
	
	
	if(lt==-1 || Pt.get(lt)<=current)
		return Double.POSITIVE_INFINITY;
	
	if(Pt.get(0)>current)
	{
		c_t=0;
		return 1.0*Pt.get(c_t);
	}
	
	if(c_t>0 && Pt.get(c_t-1)<=current)
	{
		c_t=0;
	}
	
	while (Pt.get(c_t)<=current)
	{
		c_t=c_t+2;
	}
	
	  
	  return 1.0*Pt.get(c_t);
  }
  
  
  private int get_lt(Vector<Integer> pt) {
	// TODO Auto-generated method stub
	  int sz=pt.size();
	  int i=0;
	  
	  if(pt.size()==0)
	  {
		  return -1;
	  }
	  
	  while(true)
	  {
		  
		 if(pt.get(i+1)==sz-1)
		 {
			 return i;
		 }
		 
		 i=i+2;
	  }
}


 
@Override
  public Document nextDoc(Query query, int docid) {
   
	Vector<Double> docids = new Vector<Double>(query._tokens.size());
	
	for(int i=0;i<query._tokens.size();i++)
	{
		docids.add(i, next(query._tokens.get(i),docid ));
		c_t=-1;
		if(docids.get(i)==Double.POSITIVE_INFINITY)
		{
			return null;
		}
	}
	
	if(Collections.max(docids)==Collections.min(docids))
	{
		
		return _documents.get(docids.get(0).intValue());
	}
	
	return nextDoc(query, Collections.max(docids).intValue()-1);
	
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
  public int documentTermFrequency(String term, String url) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }
}