package ru.eltech.dapeshkov.classifier;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public class BayesClassifierTest {
    public static void main(String[] args) {
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

// Two examples to learn from.
        String[] positiveText = "I love sunny days".split("\\s");
        String[] negativeText = "I hate rain".split("\\s");

        JSONProcessor.Train[] arr = null;

        //reads train data
        try (InputStream in = Processing.class.getResourceAsStream("/train1.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

// Learn by classifying examples.
// New categories can be added on the fly, when they are first used.
// A classification consists of a category and a list of features
// that resulted in the classification in that category.
        for (JSONProcessor.Train a : arr) {
            bayes.learn(a.getSentiment(), Arrays.asList(Processing.parse(a.getText(), 1)));
        }

// Here are two unknown sentences to classify.
        try (InputStream in = Processing.class.getResourceAsStream("/train2.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;

        for (JSONProcessor.Train o : arr) {
            String sentiment = ((BayesClassifier<String, String>) bayes).classify(Arrays.asList(Processing.parse(o.getText(), 1))).getCategory();
            if (sentiment.equals(o.getSentiment()))
                i++;
        }

        System.out.println(((double) i / arr.length) * 100);

        bayes.setMemoryCapacity(500);
    }
}