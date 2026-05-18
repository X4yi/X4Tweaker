package com.x4yi.x4tweaker.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private static UpdateChecker instance;

    private static final String API_URL = "https://api.github.com/repos/X4yi/X4Tweaker/releases";
    private static final int TIMEOUT_MS = 5000;

    public enum State { IDLE, CHECKING, UP_TO_DATE, UPDATE_AVAILABLE, ERROR }

    private volatile State state = State.IDLE;
    private volatile String latestVersion = null;
    private volatile String latestUrl = null;
    private volatile boolean isPrerelease = false;
    private volatile String latestTagName = null;

    private UpdateChecker() {}

    public static UpdateChecker getInstance() {
        if (instance == null) {
            synchronized (UpdateChecker.class) {
                if (instance == null) {
                    instance = new UpdateChecker();
                }
            }
        }
        return instance;
    }

    public void check() {
        if (state == State.CHECKING) return;
        state = State.CHECKING;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    conn.setRequestProperty("User-Agent", "X4Tweaker-UpdateChecker");
                    conn.setConnectTimeout(TIMEOUT_MS);
                    conn.setReadTimeout(TIMEOUT_MS);

                    if (conn.getResponseCode() != 200) {
                        state = State.ERROR;
                        return;
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JsonArray releases = new JsonParser().parse(sb.toString()).getAsJsonArray();
                    if (releases.size() == 0) {
                        state = State.ERROR;
                        return;
                    }

                    JsonObject latest = releases.get(0).getAsJsonObject();
                    String tag = latest.has("tag_name") ? latest.get("tag_name").getAsString() : null;
                    String htmlUrl = latest.has("html_url") ? latest.get("html_url").getAsString() : null;
                    boolean prerelease = latest.has("prerelease") && latest.get("prerelease").getAsBoolean();

                    if (tag == null) {
                        state = State.ERROR;
                        return;
                    }

                    latestTagName = tag;
                    latestUrl = htmlUrl;
                    isPrerelease = prerelease;

                    if (isNewerVersion(tag)) {
                        latestVersion = tag;
                        state = State.UPDATE_AVAILABLE;
                    } else {
                        state = State.UP_TO_DATE;
                    }
                } catch (Exception e) {
                    state = State.ERROR;
                }
            }
        }, "X4Tweaker-UpdateChecker");
        t.setDaemon(true);
        t.start();
    }

    private boolean isNewerVersion(String remoteTag) {
        String current = com.x4yi.x4tweaker.X4Tweaker.VERSION;
        if (current.equals(remoteTag)) return false;

        int[] currentParsed = parseVersion(current);
        int[] remoteParsed = parseVersion(remoteTag);

        if (currentParsed == null || remoteParsed == null) return false;

        for (int i = 0; i < 3; i++) {
            if (remoteParsed[i] > currentParsed[i]) return true;
            if (remoteParsed[i] < currentParsed[i]) return false;
        }

        int currentBeta = currentParsed[3];
        int remoteBeta = remoteParsed[3];

        if (currentBeta == 0 && remoteBeta > 0) {
            int[] nextCurrent = parseVersion(nextPatchVersion(current));
            if (nextCurrent != null) {
                for (int i = 0; i < 3; i++) {
                    if (remoteParsed[i] > nextCurrent[i]) return true;
                    if (remoteParsed[i] < nextCurrent[i]) return false;
                }
            }
            return remoteBeta > 0;
        }

        if (remoteBeta > currentBeta) return true;

        return false;
    }

    private int[] parseVersion(String tag) {
        String clean = tag.startsWith("r") ? tag.substring(1) : tag;
        int beta = 0;
        int bIdx = clean.indexOf('b');
        if (bIdx > 0) {
            try {
                beta = Integer.parseInt(clean.substring(bIdx + 1));
                clean = clean.substring(0, bIdx);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String[] parts = clean.split("\\.");
        if (parts.length != 3) return null;

        try {
            return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                beta
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String nextPatchVersion(String tag) {
        int[] v = parseVersion(tag);
        if (v == null) return tag;
        return "r" + v[0] + "." + v[1] + "." + (v[2] + 1);
    }

    public State getState() { return state; }
    public String getLatestVersion() { return latestVersion; }
    public String getLatestUrl() { return latestUrl; }
    public boolean isPrerelease() { return isPrerelease; }
    public String getLatestTagName() { return latestTagName; }
}
