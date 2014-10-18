package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
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
public class IndexerInvertedDoconly extends Indexer implements Serializable {


	private static final long serialVersionUID = 2698138733115785548L;

    private ReadCorpus DocReader = new ReadCorpus();
    	
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
//	private Vector<String> _terms = new Vector<String>();
	private HashMap<Integer, Vector<Integer> > _postings=new HashMap<Integer,Vector<Integer>>();
	private Vector<Document> _documents=new Vector<Document>();
	
	private Map<Integer, Integer> _termCorpusFrequency =
		      new HashMap<Integer, Integer>();
	private Map<Integer, Integer> _termDocFrequency =
		      new HashMap<Integer, Integer>();
	private Integer c_t=-1;
	
	
  public IndexerInvertedDoconly(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {

      String corpusDir = _options._corpusPrefix;
      System.out.println("Constructing index documents in: " + corpusDir);

      final File Dir = new File(corpusDir);
      int n_doc=0;
      for (final File fileEntry : Dir.listFiles()) {
	  if ( !fileEntry.isDirectory() ){
	      
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
			  
			  
			  n_doc++;
		      }
		  } 
		  finally {
		      reader.close();
		  }
	      }
	      else {
		  System.out.println(fileEntry.getName());
		  String nextDoc = DocReader.createFileInput(fileEntry);
		  processDocument(nextDoc);
	      }
	  } 
      }

      /*
 	  String corpusFile = _options._corpusPrefix + "/corpus.tsv";
	  System.out.println("Construct index from: " + corpusFile);

	    BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
	    try {
	      String line = null;
	      while ((line = reader.readLine()) != null) {
	    	
	        processDocument(line);
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
	    writer.writeObject(this);
	    writer.close();
	  
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
	    
	    Vector<Integer> list;
	    
	    for (Integer idx : uniqueTerms) {
	      _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);

	      
	      list=_postings.get(idx);
	      list.add(_documents.size()-1);
	      
	      
	 
	      _postings.put(idx, list);
	      

	    }
	
}
  
  private void readTermVector(String content, Vector<Integer> tokens) {
	    Scanner s = new Scanner(content);  // Uses white space by default.
	    while (s.hasNext()) {
	      String token = s.next();
	      int idx = -1;
	   
	      if (_dictionary.containsKey(token)) {
	        idx = _dictionary.get(token);
	      } else {
	    	 
	        idx =_dictionary.size();
//	        _terms.add(token);
	        _dictionary.put(token, idx);
	        
	        _termCorpusFrequency.put(idx, 0);
	        _termDocFrequency.put(idx, 0);
	        _postings.put(idx,new Vector<Integer>());
	      }
	      tokens.add(idx);

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
    IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

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
	
	int lt=Pt.size()-1;
	
	if(lt==-1 || Pt.get(lt)<=current)
		return Double.POSITIVE_INFINITY;
	
	if(Pt.get(0)>current)
	{
		c_t=0;
		return 1.0*Pt.get(c_t);
	}
	
	if(c_t>0 && Pt.get(c_t-1)<=current)
	{
		low=c_t-1;
	}
	else
	{
		low=0;
	}
	
	jump=1;
	
	high=low+jump;
	
	while (high<lt && Pt.get(high)<=current)
	{
		low=high;
		jump=2*jump;
		high=low+jump;
	}
	
	if(high>lt)
	{
		high=lt;
	}
	
	
	c_t=Binary_Search(t,low,high,current,Pt);
	  
	  return 1.0*Pt.get(c_t);
  }
  
  
  private Integer Binary_Search(String t, int low, int high, Integer current, Vector<Integer> Pt) {
	// TODO Auto-generated method stub
	  int mid;
	  
	  while(high-low>1)
	  {
		  mid=(int) Math.floor((low + high) / 2);
		  if(Pt.get(mid) <= current)
		  {
			  low = mid;
		  }
		  else
		  {
			  high = mid;
		  }
		  
	  }
	return high;
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
  public double NextPhrase(Query query, int docid, int pos) {
	SearchEngine.Check(false, "Not implemented!");
	return 0.0;
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
