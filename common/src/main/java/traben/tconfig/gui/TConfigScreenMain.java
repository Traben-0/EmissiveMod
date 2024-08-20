package traben.tconfig.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import traben.tconfig.TConfig;
import traben.tconfig.TConfigHandler;
import traben.tconfig.gui.entries.TConfigEntry;
import traben.tconfig.gui.entries.TConfigEntryCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class TConfigScreenMain extends TConfigScreen {
    protected final TConfigEntryCategory entries;
    protected final List<ResourceLocation> modIcons;
    protected final Set<TConfigHandler<?>> configHandlers;
    boolean haveInitConfigs = false;

    public TConfigScreenMain(final String title, final Screen parent, Set<TConfigHandler<?>> inputHandlers, List<TConfigEntry> defaultEntries) {
        super(title, parent, true);
        this.entries = new TConfigEntryCategory.Empty().addAll(defaultEntries);
        this.modIcons = new ArrayList<>();
        this.configHandlers = inputHandlers;

        this.resetDefaultValuesRunnable = entries::setValuesToDefault;
        this.undoChangesRunnable = entries::resetValuesToInitial;
    }

    @Override
    protected Component getBackButtonText() {
        return CommonComponents.GUI_DONE;
    }

    /**
     * This method reads the config handlers and adds their entries to the screen
     * It also adds the mod icons to the top right corner of the screen
     * This method is called in the init method, not the actual initializer to not lag out mod menu with a big config load
     * and will only run once
     */
    private void initConfigs() {
        if (haveInitConfigs) return;
        haveInitConfigs = true;

        configHandlers.stream()
                .filter(TConfigHandler::doesGUI)
                .forEach(configHandler -> {
                    TConfig config = configHandler.getConfig();
                    entries.addAll(config.getGUIOptions().getOptions().values());
                    ResourceLocation icon = config.getModIcon();
                    if (icon != null) {
                        modIcons.add(icon);
                    }
                });
    }

    @Override
    public void onClose() {
        if (entries.saveValuesToConfig()) {
            configHandlers.forEach(TConfigHandler::saveToFile);
            Minecraft.getInstance().reloadResourcePacks();
        }
        super.onClose();
    }

    @Override
    protected void init() {

        initConfigs();
        super.init();
        var child = new TConfigEntryListWidget(
                (int) (this.width * 0.3),
                (int) (this.height * 0.7),
                (int) (this.height * 0.15),
                (int) (this.width * 0.6),
                24,
                entries.getOptions().values().toArray(new TConfigEntry[0]));
#if MC >= MC_20_6
        child.setWidgetBackgroundToFullWidth();
#endif
        this.addRenderableWidget(child);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //draw mod icons in the top right corner of the screen
        // from left to right
        if (!modIcons.isEmpty()) {
            int ix = this.width - (modIcons.size() * 34);
            for (ResourceLocation modIcon : modIcons) {
                context.blit(modIcon, ix, 2, 0, 0, 32, 32, 32, 32);
                ix += 34;
            }
        }
    }

}
