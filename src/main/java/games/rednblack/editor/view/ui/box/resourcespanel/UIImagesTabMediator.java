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

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import games.rednblack.editor.controller.commands.resource.DeleteImageResource;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.ImageResource;

import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.ArrayList;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIImagesTabMediator extends UIResourcesTabMediator<UIImagesTab> {

    private static final String TAG = UIImagesTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIImagesTabMediator() {
        super(NAME, new UIImagesTab());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();

        listNotification = ArrayUtils.add(listNotification, DeleteImageResource.DONE);

        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case DeleteImageResource.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        ArrayList<TextureAtlas> atlass = resourceManager.getProjectAtlasList();

        Array<DraggableResource> thumbnailBoxes = new Array<>();
        for (TextureAtlas atlas : atlass) {
            Array<TextureAtlas.AtlasRegion> atlasRegions = atlas.getRegions();
            for (TextureAtlas.AtlasRegion region : new Array.ArrayIterator<>(atlasRegions)) {
                if (!region.name.contains(searchText)) continue;
                boolean is9patch = region.findValue("split") != null;
                DraggableResource draggableResource = new DraggableResource(new ImageResource(region));
                if (is9patch) {
                    draggableResource.setFactoryFunction(ItemFactory.get()::create9Patch);
                } else {
                    draggableResource.setFactoryFunction(ItemFactory.get()::createSimpleImage);
                }
                draggableResource.initDragDrop();
                thumbnailBoxes.add(draggableResource);
            }
        }

        thumbnailBoxes.sort();
        viewComponent.setThumbnailBoxes(thumbnailBoxes);
    }
}
