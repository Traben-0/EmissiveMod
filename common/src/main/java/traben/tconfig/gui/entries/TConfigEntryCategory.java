package traben.tconfig.gui.entries;

import com.demonwav.mcdev.annotations.Translatable;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import traben.tconfig.gui.TConfigScreenList;

import java.util.Collection;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class TConfigEntryCategory extends TConfigEntry {

    private final Object2ObjectLinkedOpenHashMap<String, TConfigEntry> options = new Object2ObjectLinkedOpenHashMap<>();
    private final String translationKey;
    protected boolean fullWidthBackgroundEvenIfSmaller = false;
    private TConfigScreenList screen = null;
    private Tooltip emptyTooltip = Tooltip.create(Component.translatable("config.entity_features.empty"));
    private TConfigScreenList.Align align = TConfigScreenList.Align.CENTER;
    private TConfigScreenList.Renderable renderFeature = null;

    public TConfigEntryCategory(@Translatable final String text, @Translatable final String tooltip) {
        super(text, tooltip);
        translationKey = text;
    }

    public TConfigEntryCategory(@Translatable final String text) {
        super(text, null);
        translationKey = text;
    }

    @SuppressWarnings("unused")
    public void setAlign(final TConfigScreenList.Align align) {
        this.align = align;
    }

    public Object2ObjectLinkedOpenHashMap<String, TConfigEntry> getOptions() {
        return options;
    }

    //don't need to init screen each time
    public TConfigScreenList getScreen() {
        if (screen == null) {
            screen = new TConfigScreenList(translationKey, Minecraft.getInstance().screen, options.values().toArray(new TConfigEntry[0]), this::setValuesToDefault, this::resetValuesToInitial, align);
            screen.setRenderFeature(renderFeature);
            if (fullWidthBackgroundEvenIfSmaller) {
                screen.setWidgetBackgroundToFullWidth();
            }
        }
        return screen;
    }

    @Override
    public AbstractWidget getWidget(final int x, final int y, final int width, final int height) {
        return new CategoryButton(x, y, width, height, getText(),
                (button) -> Minecraft.getInstance().setScreen(getScreen()));
    }

    @Override
    public boolean saveValuesToConfig() {
        boolean found = false;
        for (TConfigEntry option : options.values()) {
            found |= option.saveValuesToConfig();
        }
        return found;
    }

    @SuppressWarnings("unused")
    public void setWidgetBackgroundToFullWidth() {
        this.fullWidthBackgroundEvenIfSmaller = true;
    }

    @Override
    public void setValuesToDefault() {
        for (TConfigEntry option : options.values()) {
            option.setValuesToDefault();
        }
    }

    @Override
    public void resetValuesToInitial() {
        for (TConfigEntry option : options.values()) {
            option.resetValuesToInitial();
        }
    }

    public TConfigEntryCategory add(final TConfigEntry... option) {
        for (TConfigEntry tConfigEntry : option) {
            add(tConfigEntry);
        }
        return this;
    }

    public TConfigEntryCategory addAll(final Collection<TConfigEntry> option) {
        if (option != null) {
            option.forEach(this::add);
        }
        return this;
    }

    public TConfigEntryCategory add(final TConfigEntry option) {
        if (option == null) return this;
        if (option instanceof TConfigEntryCategory category) return addOrMerge(category);
        options.put(option.getText().getString(), option);
        return this;
    }

    private TConfigEntryCategory addOrMerge(final TConfigEntryCategory category) {
        String categoryKey = category.getText().getString();
        if (options.containsKey(categoryKey) && options.get(categoryKey) instanceof TConfigEntryCategory existingCategory) {
            category.options.values().forEach(existingCategory::add);
        } else {
            options.put(categoryKey, category);
        }
        return this;
    }

    @Override
    boolean hasChangedFromInitial() {
        return options.values().stream().anyMatch(TConfigEntry::hasChangedFromInitial);
    }

    public TConfigEntryCategory setEmptyTooltip(@NotNull @Translatable final String emptyTooltipKey) {
        this.emptyTooltip = Tooltip.create(Component.translatable(emptyTooltipKey));
        return this;
    }

    @SuppressWarnings("unused")
    public void setRenderFeature(final TConfigScreenList.Renderable renderFeature) {
        this.renderFeature = renderFeature;
    }

    public static class Empty extends TConfigEntryCategory {
        public Empty() {
            //noinspection NoTranslation
            super("", null);
        }

        @Override
        public AbstractWidget getWidget(int x, int y, int width, int height) {
            return null;
        }


    }

    private class CategoryButton extends Button {

        protected CategoryButton(final int x, final int y, final int width, final int height, final Component message, final OnPress onPress) {
            super(x, y, width, height, message, onPress, Supplier::get);
            active = !options.isEmpty();
            if (!active) {
                setTooltip(emptyTooltip);
            }
        }

        @Override
        public @NotNull Component getMessage() {
            return hasChangedFromInitial() ? Component.nullToEmpty(CHANGED_COLOR + super.getMessage().getString()) : super.getMessage();
        }
    }

}
