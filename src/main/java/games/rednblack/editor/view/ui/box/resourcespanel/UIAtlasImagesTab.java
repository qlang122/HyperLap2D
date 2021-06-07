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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.HashMap;
import java.util.Map;

import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIAtlasImagesTab extends UIResourcesTab {
    protected VisTable mainTable;

    public UIAtlasImagesTab() {
        super();
    }

    @Override
    protected VisScrollPane crateScrollPane() {
        mainTable = new VisTable();
        return StandardWidgetsFactory.createScrollPane(mainTable);
    }

    @Override
    public String getTabTitle() {
        return "Atlas Images";
    }

    @Override
    public String getTabIconStyle() {
        return "images-button";
    }

    public void setThumbnailBoxes(HashMap<String, Array<DraggableResource>> draggableResources) {
        mainTable.clearChildren();
        for (Map.Entry<String, Array<DraggableResource>> entry : draggableResources.entrySet()) {
            String key = entry.getKey();
            Array<DraggableResource> value = entry.getValue();

            VisTable imagesTable = new VisTable();
            CollapsibleWidget collapsibleWidget = new CollapsibleWidget(imagesTable);
            Table titleTable = crateTitleTable(key, collapsibleWidget);

            for (int i = 0; i < value.size; i++) {
                DraggableResource draggableResource = value.get(i);
                imagesTable.add((Actor) draggableResource.getViewComponent()).padRight(5).padBottom(5);
                if ((i - 7) % 4 == 0) {
                    imagesTable.row();
                }
            }
            mainTable.add(titleTable).expandX().fillX().padBottom(7);
            mainTable.row();
            mainTable.add(collapsibleWidget).expand();
            mainTable.row();
        }
    }

    private Table crateTitleTable(String title, CollapsibleWidget collapsibleWidget) {
        VisTable header = new VisTable();
        header.setTouchable(Touchable.enabled);
        header.setBackground(VisUI.getSkin().getDrawable("expandable-properties-active-bg"));
        header.add(StandardWidgetsFactory.createLabel(title)).left().expandX().padRight(6).padLeft(8);
        VisImageButton button = StandardWidgetsFactory.createImageButton("expandable-properties-button");
        header.add(button).padRight(8);
        header.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                collapse(header, collapsibleWidget);
            }
        });
        return header;
    }

    private void collapse(VisTable header, CollapsibleWidget collapsibleWidget) {
        collapsibleWidget.setCollapsed(!collapsibleWidget.isCollapsed());
        header.setBackground(VisUI.getSkin().getDrawable("expandable-properties-" + (collapsibleWidget.isCollapsed() ? "inactive" : "active") + "-bg"));
    }

}
