package com.x4yi.x4tweaker.gui.changelog;

import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.gui.ClickGUI;
import com.x4yi.x4tweaker.utils.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangelogScreen extends GuiScreen {
    private final ClickGUI parent;
    private final List<Line> lines = new ArrayList<Line>();

    private volatile String state = "loading";
    private volatile String errorMessage = "";
    private volatile boolean usingCache = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private String currentTag = X4Tweaker.VERSION;
    private String currentLang = "ES";
    private String rawChangelog = "";

    private boolean dropdownOpen = false;
    private final List<String> availableTags = new ArrayList<String>();
    private int dropdownScroll = 0;
    private boolean tagsLoaded = false;

    public ChangelogScreen(ClickGUI parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        loadAsync();
        loadTagsAsync();
    }

    private void loadTagsAsync() {
        if (tagsLoaded) return;
        new Thread(() -> {
            List<String> tags = ChangelogService.fetchReleases();
            synchronized (availableTags) {
                availableTags.clear();
                availableTags.addAll(tags);
                if (!availableTags.contains(currentTag)) {
                    availableTags.add(0, currentTag);
                }
            }
            tagsLoaded = true;
        }, "x4tweaker-tags-loader").start();
    }

    private void loadAsync() {
        state = "loading";
        lines.clear();
        new Thread(() -> {
            ChangelogService.Result result = ChangelogService.fetchForTag(currentTag);
            if (!result.success) {
                errorMessage = result.message == null ? "Unknown error" : result.message;
                state = "error";
                return;
            }
            usingCache = "cached".equalsIgnoreCase(result.message);
            rawChangelog = result.changelog;
            reparse();
        }, "x4tweaker-changelog-loader").start();
    }

    private void reparse() {
        List<Line> parsed = parseMarkdown(rawChangelog);
        synchronized (lines) {
            lines.clear();
            lines.addAll(parsed);
        }
        state = parsed.isEmpty() ? "empty" : "ready";
        scrollOffset = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int x1 = 20;
        int y1 = 20;
        int x2 = this.width - 20;
        int y2 = this.height - 20;

        RenderUtils.drawBorderedRect(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 1.0f, 0xFF303030, 0x00000000);
        RenderUtils.drawRect(x1, y1, x2, y2, 0xE0151515);
        RenderUtils.drawGradientRectHorizontal(x1, y1, x2, y1 + 22, 0xFF2A2A2A, 0xFF383838);

        mc.fontRenderer.drawStringWithShadow("Changelog", x1 + 8, y1 + 7, 0xFFFFFFFF);

        int btnX = x1 + 70;
        int btnY = y1 + 5;
        int btnW = 90;
        int btnH = 12;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        RenderUtils.drawRect(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF444444 : 0xFF333333);
        mc.fontRenderer.drawStringWithShadow(currentTag + " \u25BC", btnX + 4, btnY + 2, 0xFFFFFFFF);

        int langBtnX = btnX + btnW + 5;
        int langBtnY = btnY;
        int langBtnW = 30;
        boolean langHover = mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= langBtnY && mouseY <= langBtnY + btnH;
        RenderUtils.drawRect(langBtnX, langBtnY, langBtnX + langBtnW, langBtnY + btnH, langHover ? 0xFF444444 : 0xFF333333);
        mc.fontRenderer.drawStringWithShadow(currentLang, langBtnX + 8, langBtnY + 2, 0xFFFFFFFF);

        boolean closeHover = mouseX >= x2 - 20 && mouseX <= x2 - 6 && mouseY >= y1 + 5 && mouseY <= y1 + 19;
        mc.fontRenderer.drawStringWithShadow("\u2190", x2 - 18, y1 + 7, closeHover ? 0xFFFF6666 : 0xFFCCCCCC);

        int contentX = x1 + 10;
        int contentY = y1 + 28;
        int contentW = (x2 - x1) - 20;
        int contentH = (y2 - y1) - 38;

        RenderUtils.drawRect(contentX - 2, contentY - 2, contentX + contentW + 2, contentY + contentH + 2, 0x33000000);

        if ("loading".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Loading changelog from GitHub...", contentX, contentY + 6, 0xFFAAAAAA);
        } else if ("error".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Could not load changelog.", contentX, contentY + 6, 0xFFFF6666);
            mc.fontRenderer.drawStringWithShadow(errorMessage, contentX, contentY + 18, 0xFFBBBBBB);
        } else if ("empty".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Release body is empty.", contentX, contentY + 6, 0xFFAAAAAA);
        } else {
            if (usingCache) {
                mc.fontRenderer.drawStringWithShadow("Showing cached changelog", contentX, contentY + 6, 0xFFFFCC66);
                contentY += 12;
                contentH -= 12;
            }
            drawParsedLines(contentX, contentY, contentW, contentH);
        }

        if (dropdownOpen) {
            int dropX = btnX;
            int dropY = btnY + btnH;
            int dropW = btnW;
            int dropH = Math.min(100, availableTags.size() * 12);
            RenderUtils.drawRect(dropX, dropY, dropX + dropW, dropY + dropH, 0xFF222222);
            RenderUtils.drawBorderedRect(dropX, dropY, dropX + dropW, dropY + dropH, 1.0f, 0xFF555555, 0x00000000);

            int startIdx = dropdownScroll;
            for (int i = 0; i < dropH / 12 && i + startIdx < availableTags.size(); i++) {
                int itemY = dropY + i * 12;
                String tag = availableTags.get(i + startIdx);
                boolean itemHover = mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= itemY && mouseY <= itemY + 12;
                if (itemHover) {
                    RenderUtils.drawRect(dropX, itemY, dropX + dropW, itemY + 12, 0xFF444444);
                }
                mc.fontRenderer.drawStringWithShadow(tag, dropX + 4, itemY + 2, tag.equals(currentTag) ? 0xFF66FF66 : 0xFFDDDDDD);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawParsedLines(int x, int y, int w, int h) {
        int drawY = y - scrollOffset;
        int totalHeight = 0;

        List<Line> snapshot;
        synchronized (lines) {
            snapshot = new ArrayList<Line>(lines);
        }

        for (int i = 0; i < snapshot.size(); i++) {
            Line line = snapshot.get(i);
            List<String> wrapped = mc.fontRenderer.listFormattedStringToWidth(line.text, w - line.indent - 4);
            int wrappedHeight = wrapped.size() * line.height;

            if (drawY + wrappedHeight >= y && drawY <= y + h + wrappedHeight) {
                int tempY = drawY;
                for (String s : wrapped) {
                    if (tempY >= y - line.height && tempY <= y + h) {
                        mc.fontRenderer.drawStringWithShadow(s, x + line.indent, tempY, line.color);
                    }
                    tempY += line.height;
                }
            }
            drawY += wrappedHeight;
            totalHeight += wrappedHeight;
        }

        maxScroll = Math.max(0, totalHeight - h);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int x1 = 20;
        int y1 = 20;
        int x2 = this.width - 20;
        int btnX = x1 + 70;
        int btnY = y1 + 5;
        int btnW = 90;
        int btnH = 12;
        int langBtnX = btnX + btnW + 5;
        int langBtnW = 30;

        if (dropdownOpen) {
            int dropX = btnX;
            int dropY = btnY + btnH;
            int dropW = btnW;
            int dropH = Math.min(100, availableTags.size() * 12);
            if (mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + dropH) {
                int clickedIdx = dropdownScroll + (mouseY - dropY) / 12;
                if (clickedIdx >= 0 && clickedIdx < availableTags.size()) {
                    currentTag = availableTags.get(clickedIdx);
                    dropdownOpen = false;
                    loadAsync();
                }
                return;
            } else {
                dropdownOpen = false;
            }
        } else if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH && mouseButton == 0) {
            dropdownOpen = true;
            return;
        } else if (mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= btnY && mouseY <= btnY + btnH && mouseButton == 0) {
            currentLang = currentLang.equals("ES") ? "EN" : "ES";
            reparse();
            return;
        }

        if (mouseButton == 0 && mouseX >= x2 - 20 && mouseX <= x2 - 6 && mouseY >= y1 + 5 && mouseY <= y1 + 19) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        if (dropdownOpen) {
            int maxDropScroll = Math.max(0, availableTags.size() - 100 / 12);
            if (wheel < 0) dropdownScroll = Math.min(maxDropScroll, dropdownScroll + 1);
            else dropdownScroll = Math.max(0, dropdownScroll - 1);
            return;
        }

        if (wheel < 0) scrollOffset = Math.min(maxScroll, scrollOffset + 14);
        else scrollOffset = Math.max(0, scrollOffset - 14);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private List<Line> parseMarkdown(String markdown) {
        List<Line> out = new ArrayList<Line>();
        if (markdown == null || markdown.isEmpty()) return out;

        boolean inCodeBlock = false;
        String[] rows = markdown.replace("\r", "").split("\n");
        String currentBlockLang = "ANY";

        for (int i = 0; i < rows.length; i++) {
            String raw = rows[i];
            String trimmed = raw.trim();
            if (trimmed.equalsIgnoreCase("[EN]")) { currentBlockLang = "EN"; continue; }
            if (trimmed.equalsIgnoreCase("[ES]")) { currentBlockLang = "ES"; continue; }
            if (trimmed.equalsIgnoreCase("[/EN]") || trimmed.equalsIgnoreCase("[/ES]")) { currentBlockLang = "ANY"; continue; }

            if (!currentBlockLang.equals("ANY") && !currentBlockLang.equalsIgnoreCase(currentLang)) {
                continue;
            }

            if (raw.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }

            if (inCodeBlock) {
                out.add(new Line(sanitizeInline(raw), 0xFF8BE9FD, 8, 10));
                continue;
            }

            String line = raw == null ? "" : raw;
            if (line.trim().isEmpty()) {
                out.add(new Line("", 0xFFFFFFFF, 0, 8));
                continue;
            }

            if (line.startsWith("### ")) {
                out.add(new Line(sanitizeInline(line.substring(4)), 0xFFAAFFAA, 0, 11));
            } else if (line.startsWith("## ")) {
                out.add(new Line(sanitizeInline(line.substring(3)), 0xFF88FF88, 0, 11));
            } else if (line.startsWith("# ")) {
                out.add(new Line(sanitizeInline(line.substring(2)), 0xFF66FF66, 0, 12));
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                out.add(new Line("• " + sanitizeInline(line.substring(2)), 0xFFE0E0E0, 8, 10));
            } else if (line.matches("^[0-9]+\\.\\s+.*")) {
                out.add(new Line(sanitizeInline(line), 0xFFE0E0E0, 8, 10));
            } else {
                out.add(new Line(sanitizeInline(line), 0xFFD0D0D0, 0, 10));
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

    private static final class Line {
        final String text;
        final int color;
        final int indent;
        final int height;

        Line(String text, int color, int indent, int height) {
            this.text = text;
            this.color = color;
            this.indent = indent;
            this.height = height;
        }
    }
}
