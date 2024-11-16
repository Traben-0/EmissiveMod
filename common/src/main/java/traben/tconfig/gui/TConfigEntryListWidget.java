package traben.tconfig.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.Nullable;
import traben.tconfig.gui.entries.TConfigEntry;

public class TConfigEntryListWidget extends AbstractSelectionList<TConfigEntryListWidget.TConfigEntryForList> {
    public TConfigEntryListWidget(final int width, final int height, final int y, final int x, final int itemHeight,
                                  TConfigEntry... entries) {
        super(Minecraft.getInstance(), width, height, y, #if MC <= MC_20_2 y+height, #endif itemHeight);
        for (TConfigEntry option : entries) {
            if (option == null || option.getWidget(0, 0, 0, 0) == null) continue;
            addEntry(option);
        }
        setX(x);
    }

    #if MC <= MC_20_2
    private int getX() {
        return this.x0;
    }

    private void setX(int x) {
        this.x0 = x;
        this.x1 = x + this.width;
    }

    #endif

    @Override
    public int getRowWidth() {
        return Math.min(width - 14, super.getRowWidth());
    }


    #if MC > MC_21_2
    @Override
    protected int scrollBarX() {
        return getX() == 0 ? super.scrollBarX() : getX() + getRowWidth() + 4;
    }
    #else
    @Override
    protected int getScrollbarPosition() {
        return getX() == 0 ? super.getScrollbarPosition() : getX() + getRowWidth() + 4;
    }
    #endif




    @Override
#if MC > MC_20_2
    protected void updateWidgetNarration(final NarrationElementOutput builder) {}
#else
    public void updateNarration(final NarrationElementOutput narrationElementOutput) {}
#endif

#if MC > MC_21_2

    @Override
    protected boolean isValidClickButton(final int i) {
        return true;
    }

#elif MC > MC_20_1
    @Override
    protected boolean isValidMouseClick(final int button) {
        return true;
    }
#endif


    @Override
    public void setSelected(@Nullable final TConfigEntryListWidget.TConfigEntryForList entry) {

    }



#if MC >= MC_20_6
    protected boolean fullWidthBackgroundEvenIfSmaller = false;

    public void setWidgetBackgroundToFullWidth() {
        this.fullWidthBackgroundEvenIfSmaller = true;
    }



    @Override
    protected void renderListBackground(final GuiGraphics context) {
        if(fullWidthBackgroundEvenIfSmaller){
            int x = getX();
            int width = getWidth();
            setX(0);
            assert Minecraft.getInstance().screen != null;
            setWidth(Minecraft.getInstance().screen.width);
            super.renderListBackground(context);
            setX(x);
            setWidth(width);
        } else {
            super.renderListBackground(context);
        }
    }

    @Override
    protected void renderListSeparators(final GuiGraphics context) {
        if (fullWidthBackgroundEvenIfSmaller) {
            int x = getX();
            int width = getWidth();
            setX(0);
            assert Minecraft.getInstance().screen != null;
            setWidth(Minecraft.getInstance().screen.width);
            super.renderListSeparators(context);
            setX(x);
            setWidth(width);
        } else {
            super.renderListSeparators(context);
        }
    }
#endif

    public abstract static class TConfigEntryForList extends Entry<TConfigEntryForList> {


        protected @Nullable AbstractWidget lastWidgetRendered = null;

        @Override
        public void render(final GuiGraphics context, final int index, final int y, final int x, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean hovered, final float tickDelta) {
            lastWidgetRendered = getWidget(x, y, entryWidth, entryHeight);
            if (lastWidgetRendered != null) lastWidgetRendered.render(context, mouseX, mouseY, tickDelta);

        }

        public abstract AbstractWidget getWidget(int x, int y, int width, int height);


        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (lastWidgetRendered == null || !lastWidgetRendered.isMouseOver(mouseX, mouseY)) return false;
            return lastWidgetRendered.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
            if (lastWidgetRendered == null || !lastWidgetRendered.isMouseOver(mouseX, mouseY)) return false;
            return lastWidgetRendered.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public void setFocused(final boolean focused) {
            if (lastWidgetRendered == null) return;
            lastWidgetRendered.setFocused(focused);
        }

        @Override
        public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
            if (lastWidgetRendered == null || !lastWidgetRendered.isMouseOver(mouseX, mouseY)) return false;
            return lastWidgetRendered.mouseReleased(mouseX, mouseY, button);
        }
    }
}
