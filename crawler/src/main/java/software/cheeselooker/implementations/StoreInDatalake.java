package software.cheeselooker.implementations;

import software.cheeselooker.exceptions.CrawlerException;
import software.cheeselooker.ports.StoreInDatalakeInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class StoreInDatalake implements StoreInDatalakeInterface {

    private final IMap<Integer, byte[]> bookMap;
    private final IMap<Integer, String> metadataMap;
    private final AtomicInteger customIdCounter;

    public StoreInDatalake(HazelcastInstance hazelcastInstance) {
        this.bookMap = hazelcastInstance.getMap("bookMap");
        this.metadataMap = hazelcastInstance.getMap("metadataMap");
        this.customIdCounter = new AtomicInteger(loadLastCustomId() + 1);
    }

    @Override
    public int saveBook(InputStream bookStream, String title, String downloadDirectory) throws CrawlerException {
        int customId = customIdCounter.getAndIncrement();
        try {
            byte[] bookData = readStreamToByteArray(bookStream);
            bookMap.put(customId, bookData);
        } catch (IOException e) {
            throw new CrawlerException("Failed to save book in Hazelcast: " + e.getMessage(), e);
        }
        return customId;
    }

    @Override
    public void saveMetadata(int customId, int gutenbergId, String title, String author, String url) throws CrawlerException {
        String metadataEntry = customId + "," + gutenbergId + "," + title + "," + author + "," + url;
        metadataMap.put(customId, metadataEntry);
    }

    private int loadLastCustomId() {
        return metadataMap.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    private byte[] readStreamToByteArray(InputStream stream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(data, 0, 1024)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}
