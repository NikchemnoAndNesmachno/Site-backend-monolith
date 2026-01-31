package ua.nin.media.storage;

import java.io.IOException;
import java.io.InputStream;

public interface MediaStorage {

    void save(String storageKey, InputStream data) throws IOException;

    InputStream open(String storageKey) throws IOException;

    void delete(String storageKey) throws IOException;

    void move(String fromKey, String toKey) throws IOException;
}
