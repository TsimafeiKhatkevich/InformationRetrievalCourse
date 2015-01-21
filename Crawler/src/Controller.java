import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 09.04.13
 * Time: 20:40
 * To change this template use File | Settings | File Templates.
 */

public class Controller {
    private static int NUM_THREADS = 7;
    private static String FOLDER = "D:\\YandexDisk\\SHAD\\Information retrieval\\CrawlData";

    public static void main(String[] args) throws Exception {
        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(FOLDER);
        config.setPolitenessDelay(40);
        config.setFollowRedirects(true);
        config.setResumableCrawling(false);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);

        crawlController.addSeed("http://simple.wikipedia.org/wiki/Main_Page");

        crawlController.start(WikiCrawler.class, NUM_THREADS);
    }
}
