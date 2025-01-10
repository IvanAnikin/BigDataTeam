
package software.cheeselooker.control;

import software.cheeselooker.exceptions.CrawlerException;
import software.cheeselooker.ports.ReaderFromWebInterface;
import software.cheeselooker.ports.StoreInDatalakeInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;


public class CrawlerCommand implements Command {
    
    private final ReaderFromWebInterface reader;
    private final StoreInDatalakeInterface store;
    private final IMap<Integer, String> metadataMap;
    private final IMap<Integer, byte[]> bookMap;

    public CrawlerCommand(ReaderFromWebInterface reader, StoreInDatalakeInterface store, HazelcastInstance hazelcastInstance) {
        this.reader = reader;
        this.store = store;
        this.metadataMap = hazelcastInstance.getMap("metadataMap");
        this.bookMap = hazelcastInstance.getMap("bookMap");
    }

    @Override
    public void download() {
        int lastId = obtainLastId();
        int successfulDownloads = 0;
        downloadLastBooks(successfulDownloads, lastId);
    }

    private void downloadLastBooks(int successfulDownloads, int lastId) {
        while (successfulDownloads < 3) {
            int nextId = lastId + 1;
            lastId++;

            if (!metadataMap.containsKey(nextId)) {
                try {
                    String[] titleAndAuthor = reader.getTitleAndAuthor(nextId);
                    if (titleAndAuthor != null) {
                        try (InputStream bookStream = reader.downloadBookStream(nextId)) {
                            if (bookStream != null) {
                                saveBook(bookStream, titleAndAuthor, nextId);
                                successfulDownloads++;
                                System.out.println("Successfully downloaded book ID " + nextId);
                            } else {
                                System.out.println("Book not found: " + nextId);
                            }
                        }
                    } else {
                        System.out.println("Failed to retrieve title and author for book ID " + nextId);
                    }
                } catch (Exception e) {
                    System.err.println("Error downloading book ID " + nextId + ": " + e.getMessage());
                }
            } else {
                System.out.println("Metadata already exists for book ID " + nextId);
            }
        }
    }

    private void saveBook(InputStream bookStream, String[] titleAndAuthor, int nextId) throws IOException {
        byte[] bookContent = bookStream.readAllBytes();
        bookMap.put(nextId, bookContent);

        String metadata = String.format("%d,%s,%s", nextId, titleAndAuthor[0], titleAndAuthor[1]);
        metadataMap.put(nextId, metadata);
    }

    private int obtainLastId() {
        return metadataMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
}
