package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.lang.StringBuilder;
import java.io.File;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.ByteArrayInputStream;


public class ReadCorpus implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4143027004911898369L;

	public String createFileInput(final File filename) throws IOException {
	StringBuilder out = new StringBuilder();
	out.append(filename.getName());
	out.append('\t');
	String html = readFile(filename);
	Document doc = Jsoup.parse(html);
	String body = doc.body().text();
	body = cleanAndStem(body);
	out.append(body);
	out.append('\t');
	out.append('0');
	return out.toString();
    }
    
    private String readFile(File filename) throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(filename));
	StringBuilder everything = new StringBuilder();
	String line = null;
	while ((line = reader.readLine()) != null) {
	    everything.append(line);
	}
	return everything.toString();
    }    

    private String cleanAndStem(String body) throws IOException {
	body = body.replaceAll("-", " ");
	body = body.replaceAll("\\s+", " ");
	String charsToDel = "`~!@#$%^&*()_+=[]\\{}|;':\",./<>?";
	body = body.replaceAll("[" + Pattern.quote(charsToDel)  + "]", "");

	StringBuilder out = new StringBuilder();
	char[] w = new char[501];
	Stemmer porter = new Stemmer();
	InputStream in = new ByteArrayInputStream( body.getBytes() );
	
	while(true) {
	    int ch = in.read();
	    if (Character.isLetter((char) ch)) {
		int j = 0;
		while(true) {
		    ch = Character.toLowerCase((char) ch);
		    w[j] = (char) ch;
		    if (j < 500) j++;
		    ch = in.read();
		    if (!Character.isLetter((char) ch)) {

			for (int c = 0; c < j; c++) porter.add(w[c]);
			
			porter.stem();
			out.append(porter.toString());
			
			break;
		    }
		}
	    }
	    if (ch < 0) break;
	    out.append((char)ch);
	}
	
	return out.toString();
    }
    

    
}