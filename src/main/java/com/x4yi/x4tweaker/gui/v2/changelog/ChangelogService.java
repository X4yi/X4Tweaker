package com.x4yi.x4tweaker.gui.v2.changelog;

import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChangelogService {
    private static final String API_BASE = "https://api.github.com/repos/X4yi/X4Tweaker/releases";
    private static final String CACHE_DIR = "x4tweaker/changelog";

    public static Result fetchForTag(String tag) {
        try {
            URL url = new URL(API_BASE + "/tags/" + tag);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            if (conn.getResponseCode() == 200) {
                String body = readStream(conn.getInputStream());
                com.google.gson.JsonObject json = new JsonParser().parse(body).getAsJsonObject();
                String changelog = json.has("body") ? json.get("body").getAsString() : "";
                cacheForTag(tag, changelog);
                return new Result(true, "online", changelog);
            }
        } catch (Exception e) {
            String cached = readCached(tag);
            if (cached != null) return new Result(true, "cached", cached);
            return new Result(false, e.getMessage(), "");
        }
        String cached = readCached(tag);
        if (cached != null) return new Result(true, "cached", cached);
        return new Result(false, "Not found", "");
    }

    public static List<String> fetchReleases() {
        List<String> tags = new ArrayList<String>();
        try {
            URL url = new URL(API_BASE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            if (conn.getResponseCode() == 200) {
                String body = readStream(conn.getInputStream());
                com.google.gson.JsonArray arr = new JsonParser().parse(body).getAsJsonArray();
                for (int i = 0; i < arr.size(); i++) {
                    String tag = arr.get(i).getAsJsonObject().get("tag_name").getAsString();
                    tags.add(tag);
                }
            }
        } catch (Exception ignored) {}
        return tags;
    }

    private static void cacheForTag(String tag, String content) {
        try {
            File cacheDir = new File(Minecraft.getMinecraft().mcDataDir, CACHE_DIR);
            cacheDir.mkdirs();
            File f = new File(cacheDir, tag + ".md");
            FileWriter w = new FileWriter(f);
            w.write(content);
            w.close();
        } catch (Exception ignored) {}
    }

    private static String readCached(String tag) {
        try {
            File f = new File(Minecraft.getMinecraft().mcDataDir, CACHE_DIR + "/" + tag + ".md");
            if (f.exists()) {
                BufferedReader r = new BufferedReader(new FileReader(f));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append("\n");
                r.close();
                return sb.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String readStream(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append("\n");
        r.close();
        return sb.toString();
    }

    public static class Result {
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
