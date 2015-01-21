import java.io.*;
import java.util.*;

public class Main {
    private boolean IsWordModel;
    private boolean CombineLength;

    private double Mean;
    private double Variance;

    public Main(boolean isWordModel, boolean combineLength) {
        IsWordModel = isWordModel;
        CombineLength = combineLength;
    }

    private List<String> getText(String fileName) throws IOException{
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), "cp1251");
        BufferedReader bfReader = new BufferedReader(inputStreamReader);
        ArrayList<String> file = new ArrayList<String>();

        String line = bfReader.readLine();
        while (line != null) {
            file.add(line);
            line = bfReader.readLine();
        }
        bfReader.close();
        return file;
    }

    public void train(String fileName) {
        List< Pair<Integer> > values = new ArrayList< Pair<Integer> >();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), "cp1251");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] sentences = line.split("\\|\\|\\|");
                values.add(ProcessingTools.CountLengths(sentences[0], sentences[1], IsWordModel, CombineLength));
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Mean = ProcessingTools.Mean(values);
        Variance = ProcessingTools.Variance(values);
    }

    private void getCost(TableCell[][] table, int[] enLengths, int[] ruLengths, int i, int j) {
        table[i][j] = new TableCell();
        if (i + j == 0) {
            table[i][j].enShift = 0;
            table[i][j].ruShift = 0;
            table[i][j].cost = 0;
            return;
        }

        table[i][j].cost = Double.MAX_VALUE;
        for (int di = 0; di < 3; ++di) {
            if (i < di) {
                break;
            }
            int enSentLen = Math.max(enLengths[i] - enLengths[i-di], 1);

            for (int dj = 0; dj < 3; ++dj) {
                if (j < dj) {
                    break;
                }
                int ruSentLen = ruLengths[j] - ruLengths[j-dj];
                double delta = (ruSentLen - enSentLen * Mean) / Math.sqrt(Variance * enSentLen);
                double cost = 2 * (1 - ProcessingTools.LaplasianFoo(Math.abs(delta))) * ProcessingTools.PENALTY[di][dj];
                cost = - Math.log(cost);

                if (table[i-di][j-dj].cost + cost  < table[i][j].cost) {
                    table[i][j].cost = table[i-di][j-dj].cost + cost;
                    table[i][j].enShift = di;
                    table[i][j].ruShift = dj;
                }
            }
        }
    }

    private List<String> alignChunks(List<String> enChunk, List<String> ruChunk) {
        int[] enLengths = new int[enChunk.size() + 1];
        int[] ruLengths = new int[ruChunk.size() + 1];
        enLengths[0] = 0;
        for (int i = 1; i < enLengths.length; ++i) {
            enLengths[i] = enLengths[i-1] + getLength(enChunk.get(i-1), "en");
        }
        ruLengths[0] = 0;
        for (int i = 1; i < ruLengths.length; ++i) {
            ruLengths[i] = ruLengths[i-1] + getLength(ruChunk.get(i-1), "ru");
        }

        TableCell[][] alignTable = new TableCell[enLengths.length][ruLengths.length];
        for (int i = 0; i < enLengths.length; ++i) {
            for (int j = 0; j < ruLengths.length; ++j) {
                getCost(alignTable, enLengths, ruLengths, i, j);
            }
        }

        int enPos = enLengths.length - 1;
        int ruPos = ruLengths.length - 1;
        List<String> alignedPairs = new ArrayList<String>();
        while (enPos > 0 || ruPos > 0) {
            String enSentence = "";
            String ruSentence = "";
            for (int i = enPos - alignTable[enPos][ruPos].enShift; i < enPos; ++i) {
                enSentence = enSentence + enChunk.get(i) + " ";
            }
            for (int i = ruPos - alignTable[enPos][ruPos].ruShift; i < ruPos; ++i) {
                ruSentence = ruSentence + ruChunk.get(i) + " ";
            }

            alignedPairs.add(enSentence + "||| " + ruSentence);
            int dEn = alignTable[enPos][ruPos].enShift;
            int dRu = alignTable[enPos][ruPos].ruShift;
            enPos -= dEn;
            ruPos -= dRu;
        }
        Collections.reverse(alignedPairs);
        return alignedPairs;
    }

    public void align(String enFileName, String ruFileName, String outputFileName) {
        try {
            List<String> enFile = getText(enFileName);
            List<String> ruFile = getText(ruFileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFileName), "cp1251");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            int enIndex = 0;
            int ruIndex = 0;
            while (enIndex < enFile.size() || ruIndex < ruFile.size()) {
                List<String> enChunk = new ArrayList<String>();
                enChunk.add(enFile.get(enIndex));
                List<String> ruChunk = new ArrayList<String>();
                ruChunk.add(ruFile.get(ruIndex));

                while (!ProcessingTools.IsChunkEnd(enFile.get(enIndex++))) {
                    enChunk.add(enFile.get(enIndex));
                }
                enChunk.set(enChunk.size() - 1, enChunk.get(enChunk.size() - 1).replace("\\", ""));
                while (!ProcessingTools.IsChunkEnd(ruFile.get(ruIndex++))) {
                    ruChunk.add(ruFile.get(ruIndex));
                }
                ruChunk.set(ruChunk.size() - 1, ruChunk.get(ruChunk.size() - 1).replace("\\", ""));

                System.out.println(enChunk.get(0));
                List<String> alignedSentences = alignChunks(enChunk, ruChunk);
                for (String line : alignedSentences) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getLength(String sentence, String language) {
        if (language.equals("ru")) {
            return ProcessingTools.CountLengths("", sentence, IsWordModel, CombineLength).second;
        }
        else if (language.equals("en")) {
            return ProcessingTools.CountLengths(sentence, "", IsWordModel, CombineLength).first;
        }
        return 0;
    }

    public void getChunks(String file, String language) {
        try {
            List<String> sentences = getText(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "cp1251");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            List<Integer> lengths = new ArrayList<Integer>();
            for (int i = 0; i < sentences.size(); ++i) {
                int length = getLength(sentences.get(i), language);
                lengths.add(length);
            }

            int index = 0;
            int cumulativeSize = 0;
            while (index + 1 < lengths.size()) {
                bufferedWriter.write(sentences.get(index));
                cumulativeSize += lengths.get(index);
                ++index;

                if (ProcessingTools.IsChunkReady(sentences.get(index), index, cumulativeSize, language)) {
                    bufferedWriter.write("\\");
                    cumulativeSize = 0;
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.write(sentences.get(index) + "\\");

            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void splitText(String textFile, String sntFile) {
        try {
            List<String> text = getText(textFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(sntFile), "cp1251");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            String sentence = "";
            for (int i = 0; i + 1 < text.size(); ++i) {
                if (text.get(i).isEmpty()) {
                    continue;
                }

                String[] words = text.get(i).trim().split("[ \t\n]+", 0);

                boolean endParagraph = false;
                if (text.get(i + 1).isEmpty() || (text.get(i + 1).startsWith(" ") && !text.get(i + 1).startsWith("  Генерал-губернатор"))) {
                    endParagraph = true;
                }

                int tokenNo = 0;
                for (; tokenNo + 1 < words.length; ++tokenNo) {
                    sentence += words[tokenNo];
                    if (words[tokenNo].matches(".*[\\.\\?!]+[\\)\"]*")) {
                        if  (words[tokenNo + 1].matches("\"[\\.,;:\\)].*")) {
                            sentence += " ";
                            continue;
                        }

                        if (words[tokenNo + 1].equals("\"")) {
                            sentence += " " + words[++tokenNo];
                        }
                        if (sentence.length() > 1) {
                            bufferedWriter.write(sentence);
                            bufferedWriter.newLine();
                        }
                        sentence = "";
                    } else {
                        sentence += " ";
                    }
                }

                if (tokenNo < words.length) {
                    sentence += words[tokenNo];
                    if (endParagraph || words[tokenNo].matches(".*[\\.\\?!]+[\\)\"]*")) {
                        if  (text.get(i + 1).matches("\"[\\.,;:\\)].*")) {
                            sentence += " ";
                            continue;
                        }

                        if (sentence.length() > 1) {
                            bufferedWriter.write(sentence);
                            bufferedWriter.newLine();
                        }
                        sentence = "";
                    } else {
                        sentence += " ";
                    }
                }
            }

            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main(true, false);
        main.train("chapter4.etalon");
        main.splitText("en.txt", "en.snt");
        main.splitText("ru.txt", "ru.snt");
        main.getChunks("en.snt", "en");
        main.getChunks("ru.snt", "ru");
        main.align("en.snt", "ru.snt", "aligns.txt");
    }
}