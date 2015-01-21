import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 19.04.13
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class WikiCrawler extends WebCrawler {
    private HashMap<String, Integer> dictionary;

    public final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    public final static Pattern WIKI_PATTERN = Pattern.compile("http://ru\\.wikipedia\\.org/wiki/.*");

    public WikiCrawler() {
        dictionary = new HashMap<String, Integer>();
    }

    private String stemm(String word) {
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }
        else if (word.endsWith("ing")) {
            return word.substring(0, word.length() - 3);
        }
        else if (word.endsWith("ed")) {
            return word.substring(0, word.length() - 2);
        }
        else if (word.endsWith("es")) {
            return word.substring(0, word.length() - 2);
        }
        else if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        else {
            return word;
        }
    }

    private void GetWords(String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, "\t\n .,:;!?()\"\'-_\\<>{}[]+|&/=+?1234567890");
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            //word = stemm(word);
            int oldValue = dictionary.containsKey(word) ? dictionary.get(word) : 0;
            dictionary.put(word, oldValue + 1);
        }
    }

    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && WIKI_PATTERN.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData parseData = (HtmlParseData) page.getParseData();
            String text = parseData.getText();
            GetWords(text);
        }
    }

    @Override
    public Object getMyLocalData() {
        return dictionary;
    }

    @Override
    public void onBeforeExit() {
        Set<String> keySet = dictionary.keySet();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("data\\words.txt"));
            BufferedWriter bfWriter = new BufferedWriter(outputStreamWriter);
            for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
                String word = it.next();
                bfWriter.write(word + "\t" + dictionary.get(word));
                bfWriter.newLine();
            }
            bfWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}