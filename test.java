import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.Files
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) throws IOException 
    {
	String html = readFile(args[0], StandardCharsets.UTF_8);
	Document doc = Jsoup.parse(html);
	String text = doc.body().text();
	System.out.println(text);
    }
    
    static String readFile(String path, Charset encoding) throws IOException 
    {
	byte[] encoded = Files.readAllBytes(Paths.get(path));
	return new String(encoded, encoding);
    }
}