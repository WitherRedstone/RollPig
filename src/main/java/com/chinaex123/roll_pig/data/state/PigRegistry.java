package com.chinaex123.roll_pig.data.state;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.util.Pig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class PigRegistry {

    public static final String DATA_DIR = "data/roll_pig/pigsty";
    public static final String IMAGE_DIR = "assets/roll_pig/textures/pigsty/image";
    public static final String CUSTOM_PATH = "roll_pig";

    private static final String GUIDE_HEADER_ZH = "Roll Pig —— 自定义猪猪目录";
    private static final String GUIDE_HEADER_EN = "Roll Pig - Custom Pig Directory";

    private static final String GUIDE_ZH = """
            Roll Pig —— 自定义猪猪目录
            ==========================

            pigsty/   一只猪一个 .json，字段：id / name / description / analysis
            image/    图片，文件名必须和 json 里的 id 一致；必须是真正的 PNG

            例：
              pigsty/my-pig.json
              image/my-pig.png

            改完要重启游戏（单人）或重启服务器（联机）。
            客户端还需要同样的 pigsty/*.json 和 image/*.png 才能看到图和文案。

            详细步骤见模组 README 的「自定义猪猪」章节。
            这个说明文件是自动生成的，删掉下次启动还会再写一份。
            """;

    private static final String GUIDE_EN = """
            Roll Pig - Custom Pig Directory
            ===============================

            pigsty/   one .json per pig; fields: id / name / description / analysis
            image/    the picture; file name must match the id in the json; must be a real PNG

            Example:
              pigsty/my-pig.json
              image/my-pig.png

            Restart the game (single player) or the server (multiplayer) after editing.
            The client also needs the same pigsty/*.json and image/*.png to see the text and the picture.

            See the "Custom pigs" section of the mod README for the full walkthrough.
            This file is generated automatically; delete it and it is recreated on the next launch.
            """;

    /** 中文在上、英文在下，一份文件两种语言都能看。 */
    private static final String GUIDE = GUIDE_ZH + "\n--------------------------------------------------------------------------------\n\n" + GUIDE_EN;

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
        return ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "pigsty/image/" + sanitize(pigId));
    }

    public static InputStream openImage(String pigId) {
        InputStream builtin = PigRegistry.class.getResourceAsStream("/" + IMAGE_DIR + "/" + pigId + ".png");
        if (builtin != null) {
            return builtin;
        }
        Path custom = customImageFile(pigId);
        try {
            return Files.isRegularFile(custom) ? Files.newInputStream(custom) : null;
        } catch (IOException exception) {
            RollPig.LOGGER.warn("无法读取自定义猪猪图片: {}", custom, exception);
            return null;
        }
    }

    /** 自定义猪猪图片应该放在哪里（报错时用来告诉玩家去哪个路径找）。 */
    public static Path customImageFile(String pigId) {
        return customImageDir().resolve(sanitize(pigId) + ".png");
    }

    private static String sanitize(String pigId) {
        return pigId.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
    }

    public static Path customRoot() {
        return FMLPaths.CONFIGDIR.get().resolve(CUSTOM_PATH);
    }

    public static Path customDataDir() {
        return customRoot().resolve("pigsty");
    }

    public static Path customImageDir() {
        return customRoot().resolve("image");
    }

    public static void ensureCustomDirs() {
        Path root = customRoot();
        try {
            boolean created = false;
            for (Path dir : List.of(customDataDir(), customImageDir())) {
                if (!Files.isDirectory(dir)) {
                    Files.createDirectories(dir);
                    created = true;
                }
            }
            Path guide = root.resolve("README.txt");
            String existing = Files.isRegularFile(guide) ? Files.readString(guide, StandardCharsets.UTF_8) : null;
            if (existing == null || (isGeneratedGuide(existing) && !existing.equals(GUIDE))) {
                Files.writeString(guide, GUIDE, StandardCharsets.UTF_8);
            }
            if (created) {
                RollPig.LOGGER.info("已创建自定义猪猪目录: {}（文案放 pigsty，图片放 image）", root);
            }
        } catch (IOException exception) {
            RollPig.LOGGER.warn("无法创建自定义猪猪目录: {}", root, exception);
        }
    }

    private static boolean isGeneratedGuide(String content) {
        return content.startsWith(GUIDE_HEADER_ZH) || content.startsWith(GUIDE_HEADER_EN);
    }

    private static List<Pig> load() {
        ensureCustomDirs();
        Map<String, Pig> pigs = new LinkedHashMap<>();
        List<String> files = listJsonFiles();
        Collections.sort(files);
        for (String file : files) {
            try (InputStream stream = PigRegistry.class.getResourceAsStream("/" + DATA_DIR + "/" + file)) {
                if (stream == null) {
                    continue;
                }
                for (Pig pig : readPigs(file, stream)) {
                    pigs.put(pig.id(), pig);
                }
            } catch (Exception exception) {
                RollPig.LOGGER.error("读取猪猪资料失败: {}", file, exception);
            }
        }

        int custom = 0;
        for (Path path : listCustomJsonFiles()) {
            try (InputStream stream = Files.newInputStream(path)) {
                for (Pig pig : readPigs(path.getFileName().toString(), stream)) {
                    if (pigs.put(pig.id(), pig) == null) {
                        custom++;
                    }
                }
            } catch (Exception exception) {
                RollPig.LOGGER.error("读取自定义猪猪资料失败: {}", path, exception);
            }
        }

        if (pigs.isEmpty()) {
            RollPig.LOGGER.warn("没有在 {} 里找到任何猪猪", DATA_DIR);
        } else {
            RollPig.LOGGER.info("猪圈里共 {} 只猪猪（其中自定义 {} 只）", pigs.size(), custom);
        }
        return List.copyOf(pigs.values());
    }

    private static List<Path> listCustomJsonFiles() {
        Path dir = customDataDir();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            RollPig.LOGGER.warn("无法列出自定义猪猪目录: {}", dir, exception);
            return List.of();
        }
    }

    private static List<Pig> readPigs(String file, InputStream stream) throws IOException {
        List<Pig> result = new ArrayList<>();
        String fallbackId = file.endsWith(".json") ? file.substring(0, file.length() - ".json".length()) : file;
        JsonElement root = JsonParser.parseString(readString(stream));
        JsonArray entries;
        if (root.isJsonArray()) {
            entries = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            entries = new JsonArray();
            entries.add(root);
        } else {
            RollPig.LOGGER.warn("猪猪资料格式无法识别: {}", file);
            return result;
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
            result.add(new Pig(id, name, description, analysis));
        }
        return result;
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
        return out.toString(StandardCharsets.UTF_8).replace("\uFEFF", "");
    }
}
