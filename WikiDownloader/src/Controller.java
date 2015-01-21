import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import javax.print.attribute.standard.PrinterName;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 19.04.13
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public class Controller {
    private static String FOLDER = "D:\\YandexDisk\\SHAD\\Information retrieval\\HW2\\CrawlData";

    private static void printNGramms(HashMap<String, Integer> words, int N) {
        HashMap<String, Integer> nGramms = new HashMap<String, Integer>();
        Set<String> wordKeySet = words.keySet();
        for (Iterator<String> it = wordKeySet.iterator(); it.hasNext();) {
            String word = it.next();
            for (int i = 0; i + N <= word.length(); ++i) {
                String gramm = word.substring(i, i+N);
                int oldValue = nGramms.containsKey(gramm) ? nGramms.get(gramm) : 0;
                nGramms.put(gramm, oldValue + 1);
            }
        }

        Set<String> keySet = nGramms.keySet();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("data\\" + N + "gramms.txt"));
            BufferedWriter bfWriter = new BufferedWriter(outputStreamWriter);
            for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
                String word = it.next();
                bfWriter.write(word + "\t" + nGramms.get(word));
                bfWriter.newLine();
            }
            bfWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(FOLDER);
        config.setPolitenessDelay(40);
        config.setFollowRedirects(true);
        config.setResumableCrawling(false);
        config.setMaxPagesToFetch(100);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);

        crawlController.addSeed("http://ru.wikipedia.org/wiki/%D0%92%D0%B8%D0%BA%D0%B8%D0%BF%D0%B5%D0%B4%D0%B8%D1%8F:%D0%A5%D0%BE%D1%80%D0%BE%D1%88%D0%B8%D0%B5_%D1%81%D1%82%D0%B0%D1%82%D1%8C%D0%B8");

        crawlController.start(WikiCrawler.class, 1);

        HashMap<String, Integer> words = new HashMap<String, Integer>();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream("data\\words.txt"));
            BufferedReader bfReader = new BufferedReader(inputStreamReader);

            String line = bfReader.readLine();
            while (line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                if (!tokenizer.hasMoreTokens()) {
                    continue;
                }
                String curWord = tokenizer.nextToken();
                if (!tokenizer.hasMoreTokens()) {
                    continue;
                }
                String num = tokenizer.nextToken();
                words.put(curWord, Integer.parseInt(num));
                line = bfReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 2; i <=5; ++i) {
            printNGramms(words, i);
        }
    }
}
