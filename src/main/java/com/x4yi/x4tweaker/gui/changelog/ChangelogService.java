package com.x4yi.x4tweaker.gui.changelog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class ChangelogService {
    private static final String RELEASE_URL = "https://api.github.com/repos/X4yi/X4Tweaker/releases/tags/";

    private ChangelogService() {}

    public static Result fetchForTag(String tag) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RELEASE_URL + tag);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "X4Tweaker-Client");

            int code = conn.getResponseCode();
            InputStream input = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            if (input == null) return new Result(false, "No response from GitHub", "");

            String body = readAll(input);
            if (code < 200 || code >= 300) {
                return new Result(false, "GitHub HTTP " + code, body);
            }

            JsonObject json = new JsonParser().parse(body).getAsJsonObject();
            String changelog = json.has("body") && !json.get("body").isJsonNull() ? json.get("body").getAsString() : "";
            saveCache(tag, changelog);
            return new Result(true, "ok", changelog == null ? "" : changelog);
        } catch (Exception e) {
            String cached = loadCache(tag);
            if (cached != null) {
                return new Result(true, "cached", cached);
            }
            return new Result(false, e.getMessage(), "");
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readAll(InputStream input) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static void saveCache(String tag, String body) {
        try {
            File file = getCacheFile(tag);
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {}
    }

    private static String loadCache(String tag) {
        try {
            File file = getCacheFile(tag);
            if (!file.exists()) return null;
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static File getCacheFile(String tag) {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "x4tweaker/changelog");
        return new File(configDir, tag + ".md");
    }

    public static final class Result {
        public final boolean success;
        public final String message;
        public final String changelog;

        public Result(boolean success, String message, String changelog) {
            this.success = success;
            this.message = message;
            this.changelog = changelog;
        }
    }
}
