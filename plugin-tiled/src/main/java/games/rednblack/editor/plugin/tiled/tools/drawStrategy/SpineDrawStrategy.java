package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.badlogic.ashley.core.Entity;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceSpineCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;
import games.rednblack.h2d.extention.spine.ResourceRetrieverAttachmentLoader;

public class SpineDrawStrategy extends BasicDrawStrategy {

    private final ReplaceSpineCommandBuilder replaceSpineCommandBuilder = new ReplaceSpineCommandBuilder();

    public SpineDrawStrategy(TiledPlugin plugin) {
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
        if (itemFactory.createSpineAnimation(vo.regionName, temp)) {
            Entity imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
    }

    @Override
    public void updateTile(Entity entity) {
        if (!checkValidTile(entity)) return;

        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class);
        TileVO vo = tiledPlugin.getSelectedTile();
        if (!spineDataComponent.animationName.equals(vo.regionName)) {
            replaceSpineCommandBuilder.begin(entity);
            String animName = vo.regionName;
            replaceSpineCommandBuilder.setAnimationName(animName);
            ResourceRetrieverAttachmentLoader atlasAttachmentLoader = new ResourceRetrieverAttachmentLoader(animName, tiledPlugin.getAPI().getSceneLoader().getRm());
            SkeletonJson skeletonJson = new SkeletonJson(atlasAttachmentLoader);
            replaceSpineCommandBuilder.setSkeletonJson(skeletonJson);
            SkeletonData skeletonData = skeletonJson.readSkeletonData((tiledPlugin.getAPI().getSceneLoader().getRm().getSkeletonJSON(animName)));
            replaceSpineCommandBuilder.setSkeleton(new Skeleton(skeletonData));
            replaceSpineCommandBuilder.execute(tiledPlugin.facade);
        }
    }
}
