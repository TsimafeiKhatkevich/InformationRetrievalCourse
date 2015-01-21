import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 09.04.13
 * Time: 20:43
 * To change this template use File | Settings | File Templates.
 */
public class WikiCrawler extends WebCrawler {
    private WikiData wikiData;
    private int fileCounter = 0;

    public final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public final static Pattern WIKI_PATTERN = Pattern.compile("http://simple\\.wikipedia\\.org/wiki/"
            + "([^:]*|Wikipedia:.*|Category:.*)");

    public WikiCrawler() {
        wikiData = new WikiData();
    }

    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && WIKI_PATTERN.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        WebURL webURL = page.getWebURL();
        wikiData.addDocInfo(webURL, page.getContentData().length);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData parseData = (HtmlParseData) page.getParseData();
            List<WebURL> links = parseData.getOutgoingUrls();
            wikiData.addNewVertex(webURL.getDocid(), links);
        }

        if (wikiData.getTotalLinks() % 100 == 0) {
            System.out.println("thread: " + getMyId()  + " ,pages: " + wikiData.getTotalLinks());
        }

        if (wikiData.getTotalLinks() % 4000 == 0) {
            dumpData(getMyId());
            wikiData.clearStat();
        }
    }

    @Override
    public void onBeforeExit() {
        dumpData(getMyId());
        wikiData.clearStat();
    }

    public void dumpData(int threadId) {
        Set<Integer> docIds = wikiData.getDocIds();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("data\\urls_chunk" + threadId + "_" + fileCounter));
            BufferedWriter bwUrls = new BufferedWriter(outputStreamWriter);
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream("data\\stats_chunk" + threadId + "_" + fileCounter));
            BufferedWriter bwStats = new BufferedWriter(outputStreamWriter);
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream("data\\graph_chunk" + threadId + "_" + fileCounter++));
            BufferedWriter bwGraph = new BufferedWriter(outputStreamWriter);

            for (Integer doc : docIds) {
                DocStat docStat = wikiData.getDocInfo(doc);
                bwUrls.write(doc + " " + docStat.url);
                bwUrls.newLine();
                bwStats.write(doc + " " + docStat.docSize + " " + docStat.depth);
                bwStats.newLine();

                List<Integer> outLinks = wikiData.getLinks(doc);
                for (Integer link : outLinks) {
                    bwGraph.write(link + " ");
                }
                bwGraph.newLine();
            }

            bwGraph.close();
            bwStats.close();
            bwUrls.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}