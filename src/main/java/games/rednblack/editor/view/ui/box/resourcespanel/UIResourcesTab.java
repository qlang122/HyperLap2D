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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;

/**
 * Created by sargis on 5/4/15.
 */
public abstract class UIResourcesTab extends ImageTab {

    protected final VisTable contentTable;

    public String searchString = "";

    public static final String SEARCH = "games.rednblack.editor.view.ui.box.resourcespanel.UIResourcesTab" + ".SEARCH";

    public UIResourcesTab() {
        super(false, false);
        contentTable = new VisTable();
        contentTable.add(StandardWidgetsFactory.createLabel("Search:")).padLeft(1).padBottom(6);
        contentTable.add(createTextField()).padLeft(0).padRight(7).fillX().padBottom(4);
        contentTable.row();

        VisScrollPane scrollPane = crateScrollPane();
        contentTable.add(scrollPane).padLeft(7).colspan(2).maxHeight(Gdx.graphics.getHeight() * 0.22f).expandX().fillX();
        contentTable.padTop(4);
    }

    protected VisTextField createTextField() {
        VisTextField visTextField = StandardWidgetsFactory.createTextField();
        visTextField.setMessageText(getTabTitle());
        visTextField.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                searchString = textField.getText();
                HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
                facade.sendNotification(SEARCH);
            }
        });
        return visTextField;
    }

    @Override
    public Table getContentTable() {
        return contentTable;
    }

    protected abstract VisScrollPane crateScrollPane();

}
