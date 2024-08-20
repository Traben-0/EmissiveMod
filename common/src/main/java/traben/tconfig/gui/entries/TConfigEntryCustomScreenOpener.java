package traben.tconfig.gui.entries;

import com.demonwav.mcdev.annotations.Translatable;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class TConfigEntryCustomScreenOpener extends TConfigEntry {


    private final Supplier<Screen> screenSupplier;
    private final Supplier<Boolean> savedSupplier;
    private final Runnable setValuesDefault;
    private final Runnable resetValuesToInitial;
    private final boolean screenIsSingleton;
    private final Button button;
    private Screen screen = null;

    public TConfigEntryCustomScreenOpener(@Translatable final String text, @Translatable final String tooltip, Supplier<Screen> screenSupplier, Supplier<Boolean> savedSupplier, Runnable setValuesDefault, Runnable resetValuesToInitial, boolean screenIsSingleton) {
        super(text, tooltip);
        this.screenSupplier = screenSupplier;
        this.savedSupplier = savedSupplier;
        this.screenIsSingleton = screenIsSingleton;
        this.setValuesDefault = setValuesDefault;
        this.resetValuesToInitial = resetValuesToInitial;
        button = Button.builder(getText(), (button) -> Minecraft.getInstance().setScreen(getScreen()))
                .bounds(0, 0, 0, 0).tooltip(getTooltip()).build();
    }

    @SuppressWarnings("unused")
    public TConfigEntryCustomScreenOpener(@Translatable final String text, Supplier<Screen> screenSupplier, Supplier<Boolean> savedSupplier, Runnable setValuesDefault, Runnable resetValuesToInitial, boolean screenIsSingleton) {
        this(text, null, screenSupplier, savedSupplier, setValuesDefault, resetValuesToInitial, screenIsSingleton);
    }

    public TConfigEntryCustomScreenOpener(@Translatable final String text, @Translatable final String tooltip, Supplier<Screen> screenSupplier, boolean screenIsSingleton) {
        this(text, tooltip, screenSupplier, () -> false, () -> {}, () -> {}, screenIsSingleton);
    }

    @SuppressWarnings("unused")
    public TConfigEntryCustomScreenOpener(@Translatable final String text, Supplier<Screen> screenSupplier, boolean screenIsSingleton) {
        this(text, null, screenSupplier, screenIsSingleton);
    }

    //don't need to init screen each time
    private Screen getScreen() {
        if (!screenIsSingleton) return screenSupplier.get();
        if (screen == null) {
            screen = screenSupplier.get();
        }
        return screen;
    }

    @Override
    public AbstractWidget getWidget(final int x, final int y, final int width, final int height) {
        #if MC > MC_20_2
        button.setRectangle(width, height, x, y);
        #else
        button.setX(x);
        button.setY(y);
        button.setWidth(width);
        button.height = height;
        #endif
        return button;
    }

    @Override
    boolean saveValuesToConfig() {
        return savedSupplier.get();
    }

    @Override
    void setValuesToDefault() {
        setValuesDefault.run();
    }

    @Override
    void resetValuesToInitial() {
        resetValuesToInitial.run();
    }

    @Override
    boolean hasChangedFromInitial() {
        return false;
    }
}
