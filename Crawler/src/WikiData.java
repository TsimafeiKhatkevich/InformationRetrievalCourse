import edu.uci.ics.crawler4j.url.WebURL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 11.04.13
 * Time: 2:21
 * To change this template use File | Settings | File Templates.
 */
class DocStat {
    public String url = "";
    public int docSize = 0;
    public int depth = 0;
}

public class WikiData {
    private HashMap<Integer, List<Integer> > graph;
    private HashMap<Integer, DocStat> docsInfo;

    public WikiData() {
        graph = new HashMap<Integer, List<Integer>>();
        docsInfo = new HashMap<Integer, DocStat>();
    }

    public void addNewVertex(int docId, List<WebURL> links) {
        List<Integer> linksIDs = new ArrayList<Integer>();
        for (WebURL link : links) {
            String href = link.getURL().toLowerCase();
            if (!WikiCrawler.FILTERS.matcher(href).matches() && WikiCrawler.WIKI_PATTERN.matcher(href).matches()) {
                linksIDs.add(link.getDocid());
            }
        }

        graph.put(docId, linksIDs);
    }

    public void addDocInfo(WebURL webURL, int docSize) {
        DocStat stat = new DocStat();
        stat.depth = webURL.getDepth();
        stat.url = webURL.getURL();
        stat.docSize = docSize;

        docsInfo.put(webURL.getDocid(), stat);
    }

    public List<Integer> getLinks(int docId) {
        return graph.get(docId);
    }

    public DocStat getDocInfo(int docId) {
        return docsInfo.get(docId);
    }

    public Set<Integer> getDocIds() {
        return docsInfo.keySet();
    }

    public int getTotalLinks() {
        return docsInfo.size();
    }

    public void clearStat() {
        graph.clear();
        docsInfo.clear();
    }
}
