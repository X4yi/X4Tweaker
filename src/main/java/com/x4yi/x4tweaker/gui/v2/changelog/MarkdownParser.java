package com.x4yi.x4tweaker.gui.v2.changelog;

import java.util.ArrayList;
import java.util.List;

public class MarkdownParser {
    private String langFilter = "ES";

    public void setLangFilter(String lang) {
        this.langFilter = lang;
    }

    public List<ParsedLine> parse(String markdown) {
        List<ParsedLine> out = new ArrayList<ParsedLine>();
        if (markdown == null || markdown.isEmpty()) return out;

        boolean inCodeBlock = false;
        String currentBlockLang = "ANY";
        String[] rows = markdown.replace("\r", "").split("\n");

        for (int i = 0; i < rows.length; i++) {
            String raw = rows[i];
            String trimmed = raw.trim();
            if (trimmed.equalsIgnoreCase("[EN]")) { currentBlockLang = "EN"; continue; }
            if (trimmed.equalsIgnoreCase("[ES]")) { currentBlockLang = "ES"; continue; }
            if (trimmed.equalsIgnoreCase("[/EN]") || trimmed.equalsIgnoreCase("[/ES]")) { currentBlockLang = "ANY"; continue; }
            if (!currentBlockLang.equals("ANY") && !currentBlockLang.equalsIgnoreCase(langFilter)) continue;
            if (raw.startsWith("```")) { inCodeBlock = !inCodeBlock; continue; }

            if (inCodeBlock) {
                out.add(new ParsedLine(sanitizeInline(raw), 0xFF8BE9FD, 0, 8, 10));
                continue;
            }

            if (trimmed.isEmpty()) {
                out.add(new ParsedLine("", 0xFFFFFFFF, 0, 0, 8));
                continue;
            }

            if (raw.startsWith("### ")) {
                out.add(new ParsedLine(sanitizeInline(raw.substring(4)), 0xFFAAFFAA, 0, 0, 11));
            } else if (raw.startsWith("## ")) {
                out.add(new ParsedLine(sanitizeInline(raw.substring(3)), 0xFF88FF88, 0, 0, 11));
            } else if (raw.startsWith("# ")) {
                out.add(new ParsedLine(sanitizeInline(raw.substring(2)), 0xFF66FF66, 0, 0, 12));
            } else if (raw.startsWith("- ") || raw.startsWith("* ")) {
                out.add(new ParsedLine("\u2022 " + sanitizeInline(raw.substring(2)), 0xFFE0E0E0, 8, 0, 10));
            } else if (raw.matches("^[0-9]+\\.\\s+.*")) {
                out.add(new ParsedLine(sanitizeInline(raw), 0xFFE0E0E0, 8, 0, 10));
            } else {
                out.add(new ParsedLine(sanitizeInline(raw), 0xFFD0D0D0, 0, 0, 10));
            }
        }
        return out;
    }

    private String sanitizeInline(String line) {
        if (line == null || line.isEmpty()) return "";
        String out = line;
        out = out.replace("**", "\u00A7l").replace("__", "\u00A7l");
        out = out.replace("`", "\u00A77");
        out = out.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "\u00A7b$1\u00A7r");
        return out;
    }

    public static class ParsedLine {
        public final String text;
        public final int color;
        public final int indent;
        public final int indentExtra;
        public final int height;

        public ParsedLine(String text, int color, int indent, int indentExtra, int height) {
            this.text = text;
            this.color = color;
            this.indent = indent;
            this.indentExtra = indentExtra;
            this.height = height;
        }
    }
}
