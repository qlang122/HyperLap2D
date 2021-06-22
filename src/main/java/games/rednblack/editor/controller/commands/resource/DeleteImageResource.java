package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;

import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.vo.ResourceExtraData;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by azakhary on 11/29/2015.
 */
public class DeleteImageResource extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteImageResource";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> tmpEntityList = new ArrayList<>();
    private final ArrayList<SimpleImageVO> tmpImageList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Image Resource";
    }

    @Override
    public void doAction() {
        ResourceExtraData data = notification.getBody();
        String imageName = data == null ? "" : (String) data.value1;
        int index = data == null ? -1 : (int) data.value3;

        if (projectManager.deleteSingleImageForAllResolutions(imageName, index)) {
            deleteEntitiesWithImages(sandbox.getRootEntity(), imageName, index);
            deleteAllItemsImages(imageName, index);
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsImages(String imageName, int regionIndex) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllImagesOfItem(compositeItemVO, imageName, regionIndex);
        }
    }

    private void deleteAllImagesOfItem(CompositeItemVO compositeItemVO, String imageName, int regionIndex) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemImage(rootItemVo, imageName, regionIndex);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemImage(CompositeItemVO compositeItemVO, String imageName, int regionIndex) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sImages.size() != 0) {
            ArrayList<SimpleImageVO> simpleImageVOs = compositeItemVO.composite.sImages;
            tmpImageList.addAll(simpleImageVOs
                    .stream()
                    .filter(vo -> vo.imageName.equals(imageName) && vo.index == regionIndex)
                    .collect(Collectors.toList()));
            simpleImageVOs.removeAll(tmpImageList);
            tmpImageList.clear();
        }
    }

    private void deleteEntitiesWithImages(Entity rootEntity, String regionName, int regionIndex) {
        tmpEntityList.clear();
        Consumer<Entity> action = (root) -> {
            TextureRegionComponent regionComponent = ComponentRetriever.get(root, TextureRegionComponent.class);
            if (regionComponent != null && regionComponent.regionName.equals(regionName) && regionComponent.index == regionIndex) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }
}
