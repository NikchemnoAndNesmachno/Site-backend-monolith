package ua.nin.media.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.nin.media.config.MediaProperties;
import ua.nin.media.exception.exceptions.InvalidStorageKeyException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileSystemMediaStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void saveOpenMoveDelete_flow() throws Exception {
        MediaProperties properties = new MediaProperties();
        properties.getStorage().getLocal().setRoot(tempDir.toString());
        LocalFileSystemMediaStorage storage = new LocalFileSystemMediaStorage(properties);

        String tmpKey = "tmp/media/test.txt";
        storage.save(tmpKey, new ByteArrayInputStream("data".getBytes()));

        try (InputStream in = storage.open(tmpKey)) {
            assertEquals('d', in.read());
        }

        String finalKey = "media/final.txt";
        storage.move(tmpKey, finalKey);
        assertTrue(Files.exists(tempDir.resolve(finalKey)));

        storage.delete(finalKey);
        assertFalse(Files.exists(tempDir.resolve(finalKey)));
    }

    @Test
    void safeResolve_blocksTraversal() {
        MediaProperties properties = new MediaProperties();
        properties.getStorage().getLocal().setRoot(tempDir.toString());
        LocalFileSystemMediaStorage storage = new LocalFileSystemMediaStorage(properties);

        assertThrows(InvalidStorageKeyException.class, () -> storage.open("../secret.txt"));
    }
}
