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

    public ChangelogScreen(ClickGUI parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        loadAsync();
    }

    private void loadAsync() {
        state = "loading";
        lines.clear();
        new Thread(() -> {
            ChangelogService.Result result = ChangelogService.fetchForTag(X4Tweaker.VERSION);
            if (!result.success) {
                errorMessage = result.message == null ? "Unknown error" : result.message;
                state = "error";
                return;
            }
            usingCache = "cached".equalsIgnoreCase(result.message);

            List<Line> parsed = parseMarkdown(result.changelog);
            synchronized (lines) {
                lines.clear();
                lines.addAll(parsed);
            }
            state = parsed.isEmpty() ? "empty" : "ready";
        }, "x4tweaker-changelog-loader").start();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int x1 = 20;
        int y1 = 20;
        int x2 = this.width - 20;
        int y2 = this.height - 20;

        RenderUtils.dibujarRectBordeado(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 1.0f, 0xFF303030, 0x00000000);
        RenderUtils.dibujarRect(x1, y1, x2, y2, 0xE0151515);
        RenderUtils.dibujarRectGradienteHorizontal(x1, y1, x2, y1 + 22, 0xFF2A2A2A, 0xFF383838);

        mc.fontRenderer.drawStringWithShadow("Changelog " + X4Tweaker.VERSION, x1 + 8, y1 + 7, 0xFFFFFFFF);

        boolean closeHover = mouseX >= x2 - 20 && mouseX <= x2 - 6 && mouseY >= y1 + 5 && mouseY <= y1 + 19;
        mc.fontRenderer.drawStringWithShadow("\u2190", x2 - 18, y1 + 7, closeHover ? 0xFFFF6666 : 0xFFCCCCCC);

        int contentX = x1 + 10;
        int contentY = y1 + 28;
        int contentW = (x2 - x1) - 20;
        int contentH = (y2 - y1) - 38;

        RenderUtils.dibujarRect(contentX - 2, contentY - 2, contentX + contentW + 2, contentY + contentH + 2, 0x33000000);

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
            if (drawY + line.height >= y && drawY <= y + h) {
                mc.fontRenderer.drawStringWithShadow(line.text, x + line.indent, drawY, line.color);
            }
            drawY += line.height;
            totalHeight += line.height;
        }

        maxScroll = Math.max(0, totalHeight - h);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int x2 = this.width - 20;
        int y1 = 20;
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
        for (int i = 0; i < rows.length; i++) {
            String raw = rows[i];
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
        out = out.replace("**", "");
        out = out.replace("`", "");
        out = out.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1 ($2)");
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
