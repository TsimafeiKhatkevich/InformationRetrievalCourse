import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tsimkha
 * Date: 04.04.13
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */

class Pair<ValueType> {
    public ValueType first;
    public ValueType second;

    Pair(ValueType first, ValueType second) {
        this.first = first;
        this.second = second;
    }
}

class TableCell {
    public int enShift;
    public int ruShift;
    public double cost;
}

public class ProcessingTools {
    public final static double[][] PENALTY = {
            {0,     0.001,  0},
            {0.001, 0.982,  0.004},
            {0,     0.01,  0.004}
    };

    public final static int EN_CHUNK_LEN = 4000;
    public final static int RU_CHUNK_LEN = 4000;

    public static Pair<Integer> CountLengths(String original, String translate, boolean isWordModel, boolean combineLength) {
        String[] wordsOriginal = original.split("[^a-zA-Z0-9]+", 0);
        String[] wordsTranslate = translate.split("[^а-яА-Я0-9]+", 0);

        if (isWordModel) {
            return new Pair<Integer>(wordsOriginal.length, wordsTranslate.length);
        }

        int lenOriginal = 0;
        int lenTranslate = 0;
        for (int wordNo = 0; wordNo < wordsOriginal.length; ++wordNo) {
            lenOriginal += wordsOriginal[wordNo].length();
        }
        for (int wordNo = 0; wordNo < wordsTranslate.length; ++wordNo) {
            lenTranslate += wordsTranslate[wordNo].length();
        }

        if (combineLength) {
            lenOriginal = (int) Math.sqrt(lenOriginal) * 4 + wordsOriginal.length;
            lenTranslate = (int) Math.sqrt(lenTranslate) * 4 + wordsTranslate.length;
        }

        return new Pair<Integer>(lenOriginal, lenTranslate);
    }

    public static double Mean(List< Pair<Integer> > lengthPairs) {
        double sumSecond = 0;
        double sumFirst = 0;

        for (int i = 0; i < lengthPairs.size(); ++i) {
            sumFirst += lengthPairs.get(i).first;
            sumSecond += lengthPairs.get(i).second;
        }
        return sumSecond / sumFirst;
    }

    public static double Variance(List< Pair<Integer> > lengthPairs) {
        double mean = Mean(lengthPairs);
        double variation = 0;
        for (int i = 0; i < lengthPairs.size(); ++i) {
            variation += Math.pow(lengthPairs.get(i).second.doubleValue() / lengthPairs.get(i).first - mean, 2);
        }
        return variation / lengthPairs.size();
    }

    public static double LaplasianFoo(double delta) {
        double integrated = 0;
        final double begin = -30;
        final double end = Math.min(30, delta);
        final int nSteps = 6000;

        double step = (end - begin) / nSteps;
        for (double point = begin; point < end; point += step) {
            integrated += Math.exp(- point * point / 2) * step;
        }

        return Math.min(integrated / Math.sqrt(2 * Math.PI), 1 - 1e-14);
    }

    public static boolean IsChunkEnd(String sentence) {
        return sentence.endsWith("\\");
    }

    public static boolean IsChunkReady(String sentence, int index, int cumulativeSize, String language) {
        //To be expand later
        boolean isReady = false;
        if (language.equals("ru")) {
            isReady = sentence.startsWith("ОБЪЯСНЕНИЕ") || sentence.equals("ГЛАВА I");
//            isReady |= cumulativeSize > RU_CHUNK_LEN;
        }
        else if (language.equals("en")) {
            isReady = sentence.startsWith("EXPLANATORY") || sentence.equals("Chapter 1");
//            isReady |= cumulativeSize > EN_CHUNK_LEN;
        }

        isReady |= sentence.startsWith("@@");
        return isReady;
    }
}
