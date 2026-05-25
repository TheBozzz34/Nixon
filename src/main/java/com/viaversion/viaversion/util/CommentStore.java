package com.viaversion.viaversion.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 8 compatible replacement for ViaVersion's CommentStore.
 * The newer implementation uses String.lines(), which is unavailable on Java 8.
 */
public class CommentStore {

    @SuppressWarnings("unused")
    private final char separator;
    @SuppressWarnings("unused")
    private final int indents;

    private List<String> mainHeader = Collections.emptyList();
    private final Map<String, List<String>> headers = new LinkedHashMap<>();

    public CommentStore(final char separator, final int indents) {
        this.separator = separator;
        this.indents = indents;
    }

    public void mainHeader(final String... headerLines) {
        this.mainHeader = headerLines == null ? Collections.<String>emptyList() : Arrays.asList(headerLines);
    }

    public List<String> mainHeader() {
        return this.mainHeader;
    }

    public void header(final String key, final String... headerLines) {
        if (key == null) {
            return;
        }
        this.headers.put(key, headerLines == null ? Collections.<String>emptyList() : Arrays.asList(headerLines));
    }

    public List<String> header(final String key) {
        return this.headers.get(key);
    }

    public void storeComments(final java.io.InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return;
        }

        // We intentionally do not parse comment metadata here.
        // ViaVersion can run without comment reconstruction, and this keeps Java 8 compatibility.
        inputStream.close();
    }

    public void writeComments(final String yamlContents, final File file) throws IOException {
        if (file == null) {
            return;
        }

        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        final byte[] data = (yamlContents == null ? "" : yamlContents).getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}
