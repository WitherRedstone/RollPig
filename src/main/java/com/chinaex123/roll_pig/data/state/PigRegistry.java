package com.chinaex123.roll_pig.data.state;

import com.chinaex123.roll_pig.RollPig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class PigRegistry {

    public static final String DATA_DIR = "data/roll_pig/pigsty";
    public static final String IMAGE_DIR = "assets/roll_pig/textures/pigsty/image";

    private static volatile List<Pig> cache;

    private PigRegistry() {
    }

    public static List<Pig> all() {
        List<Pig> pigs = cache;
        if (pigs == null) {
            synchronized (PigRegistry.class) {
                pigs = cache;
                if (pigs == null) {
                    pigs = load();
                    cache = pigs;
                }
            }
        }
        return pigs;
    }

    public static void reload() {
        synchronized (PigRegistry.class) {
            cache = null;
        }
    }

    public static ResourceLocation imageLocation(String pigId) {
        String safe = pigId.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "pigsty/image/" + safe);
    }

    public static InputStream openImage(String pigId) {
        return PigRegistry.class.getResourceAsStream("/" + IMAGE_DIR + "/" + pigId + ".png");
    }

    private static List<Pig> load() {
        List<String> files = listJsonFiles();
        Collections.sort(files);
        List<Pig> pigs = new ArrayList<>();
        for (String file : files) {
            String fallbackId = file.substring(0, file.length() - ".json".length());
            try (InputStream stream = PigRegistry.class.getResourceAsStream("/" + DATA_DIR + "/" + file)) {
                if (stream == null) {
                    continue;
                }
                JsonElement root = JsonParser.parseString(readString(stream));
                JsonArray entries;
                if (root.isJsonArray()) {
                    entries = root.getAsJsonArray();
                } else if (root.isJsonObject()) {
                    entries = new JsonArray();
                    entries.add(root);
                } else {
                    RollPig.LOGGER.warn("猪猪资料格式无法识别: {}", file);
                    continue;
                }
                for (JsonElement element : entries) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject object = element.getAsJsonObject();
                    String id = optString(object, "id", fallbackId);
                    String name = optString(object, "name", id);
                    String description = optString(object, "description", "");
                    String analysis = optString(object, "analysis", "");
                    pigs.add(new Pig(id, name, description, analysis));
                }
            } catch (Exception exception) {
                RollPig.LOGGER.error("读取猪猪资料失败: {}", file, exception);
            }
        }
        if (pigs.isEmpty()) {
            RollPig.LOGGER.warn("没有在 {} 里找到任何猪猪", DATA_DIR);
        } else {
            RollPig.LOGGER.info("猪圈里共 {} 只猪猪", pigs.size());
        }
        return List.copyOf(pigs);
    }

    private static List<String> listJsonFiles() {
        List<String> result = new ArrayList<>();
        URL url = PigRegistry.class.getResource("/" + DATA_DIR);
        if (url == null) {
            RollPig.LOGGER.warn("找不到猪猪资料目录: {}", DATA_DIR);
            return result;
        }
        if (listFromPath(url, result) || listFromJar(url, result) || listFromCodeSource(result)) {
            return result;
        }
        RollPig.LOGGER.warn("无法列出猪猪资料目录（protocol={}, url={}）", url.getProtocol(), url);
        return result;
    }

    private static boolean listFromPath(URL url, List<String> result) {
        try {
            Path dir = toNioPath(url.toURI());
            if (dir == null || !Files.isDirectory(dir)) {
                return false;
            }
            try (Stream<Path> stream = Files.list(dir)) {
                stream.map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(".json"))
                        .forEach(result::add);
            }
            return !result.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Path toNioPath(URI uri) throws IOException {
        try {
            return Path.of(uri);
        } catch (FileSystemNotFoundException notFound) {
            try {
                return FileSystems.newFileSystem(uri, Map.of()).provider().getPath(uri);
            } catch (FileSystemAlreadyExistsException exists) {
                return FileSystems.getFileSystem(uri).provider().getPath(uri);
            }
        }
    }

    private static boolean listFromJar(URL url, List<String> result) {
        if (!"jar".equals(url.getProtocol())) {
            return false;
        }
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            connection.setUseCaches(false);
            try (JarFile jar = new JarFile(Path.of(connection.getJarFileURL().toURI()).toFile())) {
                collectFromJar(jar, result);
            }
            return !result.isEmpty();
        } catch (Exception exception) {
            RollPig.LOGGER.warn("枚举猪猪资料目录失败（jar）", exception);
            return false;
        }
    }

    private static boolean listFromCodeSource(List<String> result) {
        try {
            URL location = PigRegistry.class.getProtectionDomain().getCodeSource().getLocation();
            Path path = Path.of(location.toURI());
            if (Files.isDirectory(path)) {
                Path dir = path.resolve(DATA_DIR);
                if (Files.isDirectory(dir)) {
                    try (Stream<Path> stream = Files.list(dir)) {
                        stream.map(p -> p.getFileName().toString())
                                .filter(name -> name.endsWith(".json"))
                                .forEach(result::add);
                    }
                }
            } else if (Files.isRegularFile(path)) {
                try (JarFile jar = new JarFile(path.toFile())) {
                    collectFromJar(jar, result);
                }
            }
            return !result.isEmpty();
        } catch (Exception exception) {
            RollPig.LOGGER.warn("枚举猪猪资料目录失败（code source）", exception);
            return false;
        }
    }

    private static void collectFromJar(JarFile jar, List<String> result) {
        String prefix = DATA_DIR + "/";
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement().getName();
            if (!entry.startsWith(prefix) || !entry.endsWith(".json")) {
                continue;
            }
            String simple = entry.substring(prefix.length());
            if (simple.isEmpty() || simple.contains("/")) {
                continue;
            }
            result.add(simple);
        }
    }

    private static String optString(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        if (element != null && element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return fallback;
    }

    private static String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
