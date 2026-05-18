package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.setting.Setting;

public interface SettingWidget extends GuiComponent {
    Setting<?> getSetting();
    int getRequiredHeight();
}
