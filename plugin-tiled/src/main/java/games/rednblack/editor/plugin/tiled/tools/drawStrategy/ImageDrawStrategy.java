package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;

public class ImageDrawStrategy extends BasicDrawStrategy {
    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

    public ImageDrawStrategy(TiledPlugin plugin) {
        super(plugin);
    }

    @Override
    public void drawTile(float x, float y, int row, int column) {
        Entity underneathTile = tiledPlugin.getPluginEntityWithParams(row, column);
        if (underneathTile != null) {
            updateTile(underneathTile);
            return;
        }

        IFactory itemFactory = tiledPlugin.getAPI().getItemFactory();
        temp.set(x, y);

        TileVO vo = tiledPlugin.getSelectedTile();
        if (itemFactory.createSimpleImage(vo.regionName, vo.index, temp)) {
            Entity imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
    }

    @Override
    public void updateTile(Entity entity) {
        if (!checkValidTile(entity)) return;

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        if (textureRegionComponent != null && textureRegionComponent.regionName != null) {
            // there is already other tile under this one
            TileVO vo = tiledPlugin.getSelectedTile();
            if (!textureRegionComponent.regionName.equals(vo.regionName)) {
                String region = vo.regionName;
                replaceRegionCommandBuilder.begin(entity);
                TextureRegion textureRegion = tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region, textureRegionComponent.index);
                replaceRegionCommandBuilder.setRegion(textureRegion);
                replaceRegionCommandBuilder.setRegionName(region);
                replaceRegionCommandBuilder.execute(tiledPlugin.facade);
            }
        }
    }
}
