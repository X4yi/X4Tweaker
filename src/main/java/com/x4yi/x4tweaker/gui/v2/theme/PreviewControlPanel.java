package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.Minecraft;

public class PreviewControlPanel implements GuiComponent {
    private static final int ROW_HEIGHT = 14;
    private static final int PAD = 4;

    private int x, y, width, height;
    private boolean visible = true;
    private int priority = 0;
    private final ThemeBridge theme;
    private final Minecraft mc;

    public boolean showClickGUIPreview = true;
    public boolean showChangelogPreview = true;
    public boolean editAllGUIs = true;
    public int selectedGUIIndex = 0;

    private final String[] guiNames = {"ClickGUI", "Changelog"};
    private int radioEditX;
    private boolean[] hoverStates = new boolean[5];

    public PreviewControlPanel(ThemeBridge theme) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        DrawHelper.drawBorderedRect(x, y, x + width, y + height, 1.0f, theme.getSeparatorColor(), theme.getContentBgColor());

        int curY = y + PAD;

        hoverStates[0] = isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT);
        drawCheckbox(x + PAD, curY, showClickGUIPreview, "ClickGUI Preview", hoverStates[0]);
        curY += ROW_HEIGHT;

        hoverStates[1] = isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT);
        drawCheckbox(x + PAD, curY, showChangelogPreview, "Changelog Preview", hoverStates[1]);
        curY += ROW_HEIGHT + 2;

        DrawHelper.drawRect(x + PAD, curY, x + width - PAD, curY + 1, theme.getSeparatorColor());
        curY += 4;

        mc.fontRenderer.drawStringWithShadow("Editar:", x + PAD, curY + 2, 0xFFCCCCCC);
        radioEditX = x + PAD + mc.fontRenderer.getStringWidth("Editar:") + 4;

        hoverStates[2] = isInside(mouseX, mouseY, radioEditX, curY, 40, ROW_HEIGHT);
        drawRadio(radioEditX, curY, editAllGUIs, "Todas", hoverStates[2]);

        hoverStates[3] = isInside(mouseX, mouseY, radioEditX + 44, curY, 50, ROW_HEIGHT);
        drawRadio(radioEditX + 44, curY, !editAllGUIs, "Por GUI", hoverStates[3]);
        curY += ROW_HEIGHT;

        if (!editAllGUIs) {
            hoverStates[4] = isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT);
            String label = "GUI: " + guiNames[selectedGUIIndex] + " \u25BC";
            int labelW = mc.fontRenderer.getStringWidth(label);
            boolean hover = hoverStates[4];
            DrawHelper.drawRect(x + PAD, curY, x + PAD + labelW + 8, curY + ROW_HEIGHT, hover ? theme.getSurfaceHoverColor() : theme.getSurfaceColor());
            mc.fontRenderer.drawStringWithShadow(label, x + PAD + 4, curY + 3, 0xFFFFFFFF);
        }

        height = curY + PAD - y;
    }

    private void drawCheckbox(int sx, int sy, boolean checked, String label, boolean hover) {
        DrawHelper.drawBorderedRect(sx, sy + 2, sx + 10, sy + 12, 1.0f, theme.getSeparatorColor(), checked ? theme.getEnabledColor() : theme.getToggleSwitchBgColor());
        if (checked) {
            mc.fontRenderer.drawStringWithShadow("\u2713", sx + 2, sy + 3, 0xFFFFFFFF);
        }
        mc.fontRenderer.drawStringWithShadow(label, sx + 14, sy + 3, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private void drawRadio(int sx, int sy, boolean selected, String label, boolean hover) {
        DrawHelper.drawCircleOutline(sx + 5, sy + 7, 5, theme.getSeparatorColor(), 1.0f);
        if (selected) {
            DrawHelper.drawCircle(sx + 5, sy + 7, 3, theme.getEnabledColor());
        }
        mc.fontRenderer.drawStringWithShadow(label, sx + 12, sy + 3, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        int curY = y + PAD;

        if (isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT)) {
            showClickGUIPreview = !showClickGUIPreview;
            return true;
        }
        curY += ROW_HEIGHT;

        if (isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT)) {
            showChangelogPreview = !showChangelogPreview;
            return true;
        }
        curY += ROW_HEIGHT + 6;

        if (isInside(mouseX, mouseY, radioEditX, curY, 40, ROW_HEIGHT)) {
            editAllGUIs = true;
            return true;
        }
        if (isInside(mouseX, mouseY, radioEditX + 44, curY, 50, ROW_HEIGHT)) {
            editAllGUIs = false;
            return true;
        }
        curY += ROW_HEIGHT;

        if (!editAllGUIs && isInside(mouseX, mouseY, x + PAD, curY, width - PAD * 2, ROW_HEIGHT)) {
            selectedGUIIndex = (selectedGUIIndex + 1) % guiNames.length;
            return true;
        }

        return false;
    }

    private boolean isInside(int mx, int my, int sx, int sy, int sw, int sh) {
        return mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sh;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }
    @Override
    public boolean onKey(char typedChar, int keyCode) { return false; }
    @Override
    public void update() {}

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getWidth() { return width; }
    @Override
    public int getHeight() { return height; }
    @Override
    public boolean isVisible() { return visible; }
    @Override
    public void setVisible(boolean visible) { this.visible = visible; }
    @Override
    public int getPriority() { return priority; }
    @Override
    public void setPriority(int priority) { this.priority = priority; }
}
