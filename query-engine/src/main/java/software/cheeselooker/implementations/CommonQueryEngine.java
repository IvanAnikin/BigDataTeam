package software.cheeselooker.implementations;

import software.cheeselooker.exceptions.QueryEngineException;
import software.cheeselooker.model.Book;
import software.cheeselooker.model.QueryEngine;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.nio.charset.StandardCharsets;



public class CommonQueryEngine implements QueryEngine {
    private final IMap<Integer, String> metadataMap;
    private final IMap<Integer, byte[]> bookMap;
    private final IMap<String, Map<Integer, List<Integer>>> indexMap;

    public CommonQueryEngine(HazelcastInstance hazelcastInstance) {
        this.metadataMap = hazelcastInstance.getMap("metadataMap");
        this.bookMap = hazelcastInstance.getMap("bookMap");
        this.indexMap = hazelcastInstance.getMap("indexMap");
    }

    @Override
    public List<Map<String, Object>> query(String[] words) throws QueryEngineException {
        List<Map<String, Object>> results = new ArrayList<>();
        Set<Integer> commonBooks = null;
        
        System.out.println("indexMap keySet '" + indexMap.keySet() + "'");

        for (String word : words) {

            System.out.println("Processing word '" + word + "'");

            Map<Integer, List<Integer>> wordOccurrences = indexMap.get(word);
            

            if (wordOccurrences == null || wordOccurrences.isEmpty()) {
                System.out.println("wordOccurrences empty");   
                return Collections.emptyList();
            }
            System.out.println("wordOccurrences size: '" + wordOccurrences.size() + "'");
            System.out.println("wordOccurrences key set: '" + wordOccurrences.keySet() + "'");

            commonBooks = getCommonBooks(commonBooks, wordOccurrences);
        }

        if (commonBooks == null || commonBooks.isEmpty()) {
            return Collections.emptyList();
        }

        getResults(words, commonBooks, results);

        return results;
    }


    private static Set<Integer> getCommonBooks(Set<Integer> commonBooks, Map<Integer, List<Integer>> wordOccurrences) {
        if (commonBooks == null) {
            commonBooks = new HashSet<>(wordOccurrences.keySet());
        } else {
            commonBooks.retainAll(wordOccurrences.keySet());
        }
        return commonBooks;
    }

    private void getResults(String[] words, Set<Integer> commonBooks, List<Map<String, Object>> results) throws QueryEngineException {
        
        for (Integer bookId : commonBooks) {

            System.out.println("Processing common book '" + bookId + "'");

            String metadata = metadataMap.get(bookId);
            if (metadata == null) {
                System.out.println("Metadata for book ID '" + bookId + "' not found.");
                continue;
            }else{
                System.out.println("Metadata for book ID '" + bookId + "' : " + metadata);
            }

            byte[] bookContent = bookMap.get(bookId);
            if (bookContent == null) {
                System.out.println("Book content for ID '" + bookId + "' not found.");
                continue;
            }

            Map<String, Object> extractedData = new ParagraphExtractor().findParagraphs(bookContent, words);
            List<String> paragraphs = (List<String>) extractedData.get("paragraphs");
            int occurrences = (int) extractedData.get("occurrences");
            addResultsInfo(paragraphs, metadata, occurrences, results);
        }
    }

    private static void addResultsInfo(List<String> paragraphs, String metadata, int occurrences, List<Map<String, Object>> results) {
        if (!paragraphs.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("book_id", metadata);
            result.put("book_name", metadata);
            result.put("author_name", metadata);
            result.put("URL", metadata);
            result.put("paragraphs", paragraphs);
            result.put("total_occurrences", occurrences);
            results.add(result);
        }
    }

    public static class ParagraphExtractor {

        public Map<String, Object> findParagraphs(byte[] bookContent, String[] searchWords) throws QueryEngineException {
            Map<String, Object> result = new HashMap<>();
            List<String> relevantParagraphs = new ArrayList<>();
            int totalOccurrences = 0;

            Map<String, Pattern> wordPatterns = createWordPatterns(searchWords);

            String contentAsString = new String(bookContent, StandardCharsets.UTF_8);
            String[] paragraphs = splitTextIntoParagraphs(contentAsString);

            for (String paragraph : paragraphs) {
                totalOccurrences += processParagraph(paragraph, wordPatterns, relevantParagraphs);
            }

            result.put("paragraphs", relevantParagraphs);
            result.put("occurrences", totalOccurrences);
            return result;
        }

        private Map<String, Pattern> createWordPatterns(String[] searchWords) {
            Map<String, Pattern> wordPatterns = new HashMap<>();
            for (String word : searchWords) {
                wordPatterns.put(word, Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE));
            }
            return wordPatterns;
        }

        private String[] splitTextIntoParagraphs(String text) {
            return text.split("\\n\\n");
        }

        private String readFileContent(BufferedReader reader) throws IOException {
            StringBuilder textBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
            return textBuilder.toString();
        }

        private int processParagraph(String paragraph, Map<String, Pattern> wordPatterns, List<String> relevantParagraphs) {
            boolean paragraphAdded = false;
            int occurrencesInParagraph = 0;

            for (Pattern pattern : wordPatterns.values()) {
                Matcher matcher = pattern.matcher(paragraph);
                if (matcher.find()) {
                    occurrencesInParagraph += countAndHighlightOccurrences(matcher, relevantParagraphs, paragraphAdded);
                    paragraphAdded = true;
                }
            }
            return occurrencesInParagraph;
        }

        private int countAndHighlightOccurrences(Matcher matcher, List<String> relevantParagraphs, boolean paragraphAdded) {
            int occurrences = 0;
            StringBuilder highlightedBuffer = new StringBuilder();

            do {
                occurrences++;
                highlightMatch(matcher, highlightedBuffer);
            } while (matcher.find());

            matcher.appendTail(highlightedBuffer);
            if (!paragraphAdded) {
                relevantParagraphs.add(highlightedBuffer.toString().trim());
            }
            return occurrences;
        }

        private void highlightMatch(Matcher matcher, StringBuilder highlightedBuffer) {
            matcher.appendReplacement(highlightedBuffer, "\033[34m" + matcher.group() + "\033[0m");
        }


    }

}
