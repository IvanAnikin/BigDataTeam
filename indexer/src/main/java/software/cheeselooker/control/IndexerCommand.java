package software.cheeselooker.control;

import software.cheeselooker.exceptions.IndexerException;
import software.cheeselooker.model.Book;
import software.cheeselooker.ports.IndexerReader;
import software.cheeselooker.ports.IndexerStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.nio.charset.StandardCharsets;



public class IndexerCommand implements Command{
    
    private final IMap<Integer, byte[]> bookMap;
    private final IMap<String, Map<Integer, List<Integer>>> indexMap;

    public IndexerCommand(HazelcastInstance hazelcastInstance) {
        this.bookMap = hazelcastInstance.getMap("bookMap");
        this.indexMap = hazelcastInstance.getMap("indexMap");
    }

    @Override
    public void execute() throws IndexerException {
        indexLatestBooks();
        System.out.println("Indexation finished.");
    }

    private void indexLatestBooks() throws IndexerException {

        System.out.println("Waiting for books in the bookMap...");
    
        while (bookMap.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
            }
        }

        System.out.println("Book Map Keys: " + bookMap.keySet());

        for (Map.Entry<Integer, byte[]> entry : bookMap.entrySet()) {

            Integer bookId = entry.getKey();
            System.out.println("Processing book with ID: " + bookId);

            byte[] book = bookMap.get(bookId);
            if (book != null) {

                indexBook(bookId, book);
                bookMap.remove(bookId);
                System.out.println("Done indexing book with ID: " + bookId);
            
            } else {
            
                System.out.println("Book with ID " + bookId + " not found in the map.");
            }
        }

    }

    private void indexBook(Integer bookId, byte[] book) {
        String contentAsString = new String(book, StandardCharsets.UTF_8);
        String[] words = contentAsString.split("\\W+");

        for (int position = 0; position < words.length; position++) {
            String word = words[position].toLowerCase();

            if (!word.isEmpty()) {
                indexWord(bookId, word, position);
            }
        }
    }

    private void indexWord(Integer bookId, String word, int position) {

        Map<Integer, List<Integer>> bookPositions = indexMap.getOrDefault(word, new HashMap<>());
        bookPositions.putIfAbsent(bookId, new ArrayList<>());
        bookPositions.get(bookId).add(position);

        indexMap.put(word, bookPositions);
    }

}
