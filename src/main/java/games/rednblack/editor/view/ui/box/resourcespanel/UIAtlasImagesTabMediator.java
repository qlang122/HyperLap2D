/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.HashMap;
import java.util.Map;

import games.rednblack.editor.controller.commands.resource.DeleteAtlasImageResource;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.AtlasImageResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.ImageResource;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIAtlasImagesTabMediator extends UIResourcesTabMediator<UIAtlasImagesTab> {

    private static final String TAG = UIAtlasImagesTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIAtlasImagesTabMediator() {
        super(NAME, new UIAtlasImagesTab());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();

        listNotification = ArrayUtils.add(listNotification, DeleteAtlasImageResource.DONE);

        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case DeleteAtlasImageResource.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        HashMap<String, TextureAtlas> atlas = resourceManager.getProjectAtlasImagesList();

        HashMap<String, Array<DraggableResource>> thumbnailBoxes = new HashMap<>();
        for (Map.Entry<String, TextureAtlas> atlasEntry : atlas.entrySet()) {
            String atlasName = atlasEntry.getKey();
            Array<TextureAtlas.AtlasRegion> atlasRegions = atlasEntry.getValue().getRegions();
            Array<DraggableResource> array = new Array<>();
            for (TextureAtlas.AtlasRegion region : new Array.ArrayIterator<>(atlasRegions)) {
                if (!region.name.contains(searchText)) continue;
                boolean is9patch = region.findValue("split") != null;
                AtlasImageResource imageResource = new AtlasImageResource(atlasName, region, new Color(1, 1, 1, 0.2f), new Color(1, 1, 1, 0.4f),
                        new Color(200f / 255f, 200f / 255f, 200f / 255f, 0.2f), new Color(255f / 255f, 94f / 255f, 0f / 255f, 1f), true);
                DraggableResource draggableResource = new DraggableResource(imageResource);
                if (is9patch) {
                    draggableResource.setFactoryFunction(ItemFactory.get()::createAtlas9Patch);
                } else {
                    draggableResource.setFactoryFunction(ItemFactory.get()::createAtlasImage);
                }
                draggableResource.initDragDrop();
                array.add(draggableResource);
            }
            array.sort();
            thumbnailBoxes.put(atlasName, array);
        }
        viewComponent.setThumbnailBoxes(thumbnailBoxes);
    }
}
