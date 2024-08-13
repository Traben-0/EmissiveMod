package traben.tconfig.gui.entries;

import com.demonwav.mcdev.annotations.Translatable;
import traben.entity_texture_features.ETF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class TConfigEntryText extends TConfigEntry {

    protected final StringWidget widget;

    public TConfigEntryText(@Translatable final String text, TextAlignment alignment) {
        super(text, null);
        widget = new StringWidget(getText(), Minecraft.getInstance().font);
        alignment.align(widget);
    }

    public TConfigEntryText(@Translatable final String text) {
        this(text, TextAlignment.CENTER);
    }

    @SuppressWarnings("unused")
    public static Collection<TConfigEntry> fromLongOrMultilineTranslation(@Translatable String translationKey, int width) {
        return fromLongOrMultilineTranslation(translationKey, width, TextAlignment.CENTER);
    }

    public static List<TConfigEntry> fromLongOrMultilineTranslation(@Translatable String translationKey, int width, TextAlignment alignment) {
        var translated = ETF.getTextFromTranslation(translationKey);
        var lines = Minecraft.getInstance().font.getSplitter().splitLines(translated, width, Style.EMPTY);
        List<TConfigEntry> list = new ArrayList<>();
        String lastLine = null;

        for (FormattedText line : lines) {
            if (lastLine != null) {
                list.add(new TwoLines(lastLine, line.getString(), alignment));
                lastLine = null;
            } else {
                lastLine = line.getString();
            }

        }
        if (lastLine != null) {
            //noinspection NoTranslation
            list.add(new TwoLines(lastLine, "", alignment));
        }
        return list;
    }

    @Override
    public AbstractWidget getWidget(final int x, final int y, final int width, final int height) {
        #if MC > MC_20_2
        widget.setRectangle(width, height, x, y);
        #else
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);
        widget.height = height;
        #endif
        return widget;
    }

    @Override
    boolean hasChangedFromInitial() {
        return false;
    }

    @Override
    boolean saveValuesToConfig() {
        return false;
    }

    @Override
    void setValuesToDefault() {

    }

    @Override
    void resetValuesToInitial() {

    }

    public enum TextAlignment {
        LEFT, CENTER, RIGHT;

        private void align(StringWidget widget) {
            switch (this) {
                case LEFT:
                    widget.alignLeft();
                    break;
                case CENTER:
                    widget.alignCenter();
                    break;
                case RIGHT:
                    widget.alignRight();
                    break;
            }
        }
    }

    public static class TwoLines extends TConfigEntryText {

        protected final StringWidget widget2;

        @SuppressWarnings("unused")
        public TwoLines(@Translatable final String text1, @Translatable final String text2) {
            this(text1, text2, TextAlignment.CENTER);
        }

        public TwoLines(@Translatable final String text1, @Translatable final String text2, TextAlignment alignment) {
            super(text1, alignment);
            widget2 = new StringWidget(ETF.getTextFromTranslation(text2), Minecraft.getInstance().font);
            alignment.align(widget2);
            if (!widget2.getMessage().getString().contains("§"))
                widget2.setColor(0xCCCCCC);//off-white for better visual separation
        }

        @Override
        public AbstractWidget getWidget(final int x, final int y, final int width, final int height) {

            #if MC > MC_20_2
            widget.setRectangle(width, height / 2, x, y);
            widget2.setRectangle(width, height / 2, x, y + height / 2 + 2);
            #else
            widget.setX(x);
            widget.setY(y);
            widget.height = height / 2;
            widget.setWidth(width);

            widget2.setX(x);
            widget2.setY(y + height / 2 + 2);
            widget2.height = height / 2;
            widget2.setWidth(width);
            #endif
            return widget;
        }

        @Override
        public void render(final GuiGraphics context, final int index, final int y, final int x, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean hovered, final float tickDelta) {
            lastWidgetRendered = getWidget(x, y, entryWidth, entryHeight);
            widget.render(context, mouseX, mouseY, tickDelta);
            widget2.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
