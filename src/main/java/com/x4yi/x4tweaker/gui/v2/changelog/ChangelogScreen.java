package com.x4yi.x4tweaker.gui.v2.changelog;

import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.gui.v2.clickgui.ClickGUI;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangelogScreen extends GuiScreen {
    private final ClickGUI parent;
    private final MarkdownParser parser = new MarkdownParser();
    private final List<MarkdownParser.ParsedLine> lines = new ArrayList<MarkdownParser.ParsedLine>();

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

    private int x1, y1, x2, y2;
    private int btnX, btnY, btnW, btnH;
    private int langBtnX, langBtnW;
    private int closeBtnX, closeBtnY;
    private int contentX, contentY, contentW, contentH;

    public ChangelogScreen(ClickGUI parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        x1 = 20; y1 = 20; x2 = this.width - 20; y2 = this.height - 20;
        btnX = x1 + 70; btnY = y1 + 5; btnW = 90; btnH = 12;
        langBtnX = btnX + btnW + 5; langBtnW = 30;
        closeBtnX = x2 - 18; closeBtnY = y1 + 5;
        contentX = x1 + 10; contentY = y1 + 28;
        contentW = (x2 - x1) - 20;
        contentH = (y2 - y1) - 38;
        loadAsync();
        loadTagsAsync();
    }

    private void loadTagsAsync() {
        if (tagsLoaded) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> tags = ChangelogService.fetchReleases();
                synchronized (availableTags) {
                    availableTags.clear();
                    availableTags.addAll(tags);
                    if (!availableTags.contains(currentTag)) availableTags.add(0, currentTag);
                }
                tagsLoaded = true;
            }
        }, "x4tweaker-tags-loader").start();
    }

    private void loadAsync() {
        state = "loading";
        lines.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChangelogService.Result result = ChangelogService.fetchForTag(currentTag);
                if (!result.success) {
                    errorMessage = result.message == null ? "Unknown error" : result.message;
                    state = "error";
                    return;
                }
                usingCache = "cached".equalsIgnoreCase(result.message);
                rawChangelog = result.changelog;
                reparse();
            }
        }, "x4tweaker-changelog-loader").start();
    }

    private void reparse() {
        parser.setLangFilter(currentLang);
        List<MarkdownParser.ParsedLine> parsed = parser.parse(rawChangelog);
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
        DrawHelper.drawBorderedRect(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 1.0f, 0xFF303030, 0x00000000);
        DrawHelper.drawRect(x1, y1, x2, y2, 0xE0151515);
        DrawHelper.drawGradientRectH(x1, y1, x2, y1 + 22, 0xFF2A2A2A, 0xFF383838);

        mc.fontRenderer.drawStringWithShadow("Changelog", x1 + 8, y1 + 7, 0xFFFFFFFF);

        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        DrawHelper.drawRect(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF444444 : 0xFF333333);
        mc.fontRenderer.drawStringWithShadow(currentTag + " \u25BC", btnX + 4, btnY + 2, 0xFFFFFFFF);

        boolean langHover = mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= btnY && mouseY <= btnY + btnH;
        DrawHelper.drawRect(langBtnX, btnY, langBtnX + langBtnW, btnY + btnH, langHover ? 0xFF444444 : 0xFF333333);
        mc.fontRenderer.drawStringWithShadow(currentLang, langBtnX + 8, btnY + 2, 0xFFFFFFFF);

        boolean closeHover = mouseX >= closeBtnX && mouseX <= closeBtnX + 14 && mouseY >= closeBtnY && mouseY <= closeBtnY + 14;
        mc.fontRenderer.drawStringWithShadow("\u2190", closeBtnX, closeBtnY + 2, closeHover ? 0xFFFF6666 : 0xFFCCCCCC);

        int cy = contentY;
        int ch = contentH;
        DrawHelper.drawRect(contentX - 2, cy - 2, contentX + contentW + 2, cy + ch + 2, 0x33000000);

        if ("loading".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Loading changelog from GitHub...", contentX, cy + 6, 0xFFAAAAAA);
        } else if ("error".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Could not load changelog.", contentX, cy + 6, 0xFFFF6666);
            mc.fontRenderer.drawStringWithShadow(errorMessage, contentX, cy + 18, 0xFFBBBBBB);
        } else if ("empty".equals(state)) {
            mc.fontRenderer.drawStringWithShadow("Release body is empty.", contentX, cy + 6, 0xFFAAAAAA);
        } else {
            if (usingCache) {
                mc.fontRenderer.drawStringWithShadow("Showing cached changelog", contentX, cy + 6, 0xFFFFCC66);
                cy += 12; ch -= 12;
            }
            drawParsedLines(contentX, cy, contentW, ch);
        }

        if (dropdownOpen) {
            int dropX = btnX;
            int dropY = btnY + btnH;
            int dropW = btnW;
            int dropH = Math.min(100, availableTags.size() * 12);
            DrawHelper.drawRect(dropX, dropY, dropX + dropW, dropY + dropH, 0xFF222222);
            DrawHelper.drawBorderedRect(dropX, dropY, dropX + dropW, dropY + dropH, 1.0f, 0xFF555555, 0x00000000);
            int startIdx = dropdownScroll;
            for (int i = 0; i < dropH / 12 && i + startIdx < availableTags.size(); i++) {
                int itemY = dropY + i * 12;
                String tag = availableTags.get(i + startIdx);
                boolean itemHover = mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= itemY && mouseY <= itemY + 12;
                if (itemHover) DrawHelper.drawRect(dropX, itemY, dropX + dropW, itemY + 12, 0xFF444444);
                mc.fontRenderer.drawStringWithShadow(tag, dropX + 4, itemY + 2, tag.equals(currentTag) ? 0xFF66FF66 : 0xFFDDDDDD);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawParsedLines(int x, int y, int w, int h) {
        int drawY = y - scrollOffset;
        int totalHeight = 0;
        List<MarkdownParser.ParsedLine> snapshot;
        synchronized (lines) { snapshot = new ArrayList<MarkdownParser.ParsedLine>(lines); }
        for (int i = 0; i < snapshot.size(); i++) {
            MarkdownParser.ParsedLine line = snapshot.get(i);
            List<String> wrapped = mc.fontRenderer.listFormattedStringToWidth(line.text, w - line.indent - line.indentExtra - 4);
            int wrappedHeight = wrapped.size() * line.height;
            if (drawY + wrappedHeight >= y && drawY <= y + h + wrappedHeight) {
                int tempY = drawY;
                for (String s : wrapped) {
                    if (tempY >= y - line.height && tempY <= y + h) {
                        mc.fontRenderer.drawStringWithShadow(s, x + line.indent + line.indentExtra, tempY, line.color);
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
        if (dropdownOpen) {
            int dropX = btnX, dropY = btnY + btnH, dropW = btnW, dropH = Math.min(100, availableTags.size() * 12);
            if (mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + dropH) {
                int clickedIdx = dropdownScroll + (mouseY - dropY) / 12;
                if (clickedIdx >= 0 && clickedIdx < availableTags.size()) {
                    currentTag = availableTags.get(clickedIdx);
                    dropdownOpen = false;
                    loadAsync();
                }
                return;
            } else { dropdownOpen = false; }
        } else if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH && mouseButton == 0) {
            dropdownOpen = true; return;
        } else if (mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= btnY && mouseY <= btnY + btnH && mouseButton == 0) {
            currentLang = currentLang.equals("ES") ? "EN" : "ES"; reparse(); return;
        }
        if (mouseButton == 0 && mouseX >= closeBtnX && mouseX <= closeBtnX + 14 && mouseY >= closeBtnY && mouseY <= closeBtnY + 14) {
            mc.displayGuiScreen(parent); return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
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
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
}
