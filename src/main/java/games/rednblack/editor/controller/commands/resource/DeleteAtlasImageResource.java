package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.AtlasImageVO;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

/**
 * Created by azakhary on 11/29/2015.
 */
public class DeleteAtlasImageResource extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteAtlasImageResource";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> tmpEntityList = new ArrayList<>();
    private final ArrayList<AtlasImageVO> tmpImageList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Image Resource";
    }

    @Override
    public void doAction() {
        String imageName = notification.getBody();
        String atlasName = "";
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        HashMap<String, TextureAtlas> imagesList = resourceManager.getProjectAtlasImagesList();
        for (Map.Entry<String, TextureAtlas> entry : imagesList.entrySet()) {
            TextureAtlas.AtlasRegion region = entry.getValue().findRegion(imageName);
            if (region != null) {
                atlasName = entry.getKey();
                break;
            }
        }
        if (atlasName.isEmpty()) return;

        if (projectManager.deleteAtlasImageForAllResolutions(atlasName, imageName)) {
            deleteEntitiesWithImages(sandbox.getRootEntity(), imageName);
            deleteAllItemsImages(imageName);
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectAtlasImagesForAllResolutions(true, atlasName,null);
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsImages(String imageName) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllImagesOfItem(compositeItemVO, imageName);
        }
    }

    private void deleteAllImagesOfItem(CompositeItemVO compositeItemVO, String imageName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemImage(rootItemVo, imageName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemImage(CompositeItemVO compositeItemVO, String imageName) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sAtlasImages.size() != 0) {
            ArrayList<AtlasImageVO> simpleImageVOs = compositeItemVO.composite.sAtlasImages;
            tmpImageList.addAll(simpleImageVOs
                    .stream()
                    .filter(simpleImageVO -> simpleImageVO.imageName.equals(imageName))
                    .collect(Collectors.toList()));
            simpleImageVOs.removeAll(tmpImageList);
            tmpImageList.clear();
        }
    }

    private void deleteEntitiesWithImages(Entity rootEntity, String regionName) {
        tmpEntityList.clear();
        Consumer<Entity> action = (root) -> {
            TextureRegionComponent regionComponent = ComponentRetriever.get(root, TextureRegionComponent.class);
            if (regionComponent != null && regionComponent.regionName.equals(regionName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }
}
