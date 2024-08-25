package traben.entity_texture_features;

import net.minecraft.world.level.block.entity.BlockEntity;
import traben.entity_texture_features.features.ETFRenderContext;

public class ETFException extends RuntimeException {
    public ETFException(String message) {
        super(amendMessage(message));
    }

    private static String amendMessage(String message) {
        var entity = ETFRenderContext.getCurrentEntity();
        return message + """
                
                ----------------------
                ETF context:
                 - Entity = %s
                 - EMF installed = %s
                ----------------------
                """.formatted(entity == null ? "null" : entity.etf$isBlockEntity() ? ((BlockEntity)entity).getType() : entity.etf$getType(),
                ETFVersionDifferenceManager.isThisModLoaded("entity_model_features"));
    }
}
