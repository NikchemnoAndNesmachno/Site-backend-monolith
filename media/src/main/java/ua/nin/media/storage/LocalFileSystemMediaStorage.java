package ua.nin.media.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.nin.media.config.MediaProperties;
import ua.nin.media.exception.exceptions.InvalidStorageKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileSystemMediaStorage implements MediaStorage {

    private final MediaProperties properties;

    private Path root() {
        return Path.of(properties.getStorage().getLocal().getRoot()).toAbsolutePath().normalize();
    }

    @Override
    public void save(String storageKey, InputStream data) throws IOException {
        Path root = root();
        Path target = safeResolve(root, storageKey);

        Files.createDirectories(target.getParent());

        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream open(String storageKey) throws IOException {
        Path root = root();
        Path target = safeResolve(root, storageKey);
        return Files.newInputStream(target, StandardOpenOption.READ);
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Path root = root();
        Path target = safeResolve(root, storageKey);
        Files.deleteIfExists(target);
    }

    @Override
    public void move(String fromKey, String toKey) throws IOException {
        Path root = root();
        Path from = safeResolve(root, fromKey);
        Path to = safeResolve(root, toKey);

        Files.createDirectories(to.getParent());
        Files.move(from, to, ATOMIC_MOVE, REPLACE_EXISTING);
    }

    private static Path safeResolve(Path root, String storageKey) {
        Path p = root.resolve(storageKey).normalize();
        if (!p.startsWith(root)) {
            log.error("Invalid path: {}", p);
            throw new InvalidStorageKeyException("Invalid storageKey (path traversal): " + storageKey);
        }
        return p;
    }
}
