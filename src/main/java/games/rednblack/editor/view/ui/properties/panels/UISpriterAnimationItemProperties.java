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

package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UISpriterAnimationItemProperties extends UIItemCollapsibleProperties {
    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UISpriterAnimationItemProperties";
    public static final String START_BUTTON_CLICKED = prefix + ".START_BUTTON_CLICKED";
    public static final String FIRST_BUTTON_CLICKED = prefix + ".FIRST_BUTTON_CLICKED";
    public static final String PREV_BUTTON_CLICKED = prefix + ".PREV_BUTTON_CLICKED";
    public static final String NEXT_BUTTON_CLICKED = prefix + ".NEXT_BUTTON_CLICKED";
    public static final String LAST_BUTTON_CLICKED = prefix + ".LAST_BUTTON_CLICKED";

    private VisSelectBox<String> animationsSelectBox;
    private VisImageButton btnStart, btnFirst, btnPrev, btnNext, btnLast;
    private VisCheckBox cbLoop;

    public UISpriterAnimationItemProperties() {
        super("Spriter Animations");
        animationsSelectBox = StandardWidgetsFactory.createSelectBox(String.class);

        mainTable.add(StandardWidgetsFactory.createLabel("Animations:", Align.left)).padRight(5);
        mainTable.add(animationsSelectBox).colspan(2).fillX();

        btnStart = StandardWidgetsFactory.createImageButton("spriter-playbtn-start");
        btnFirst = StandardWidgetsFactory.createImageButton("spriter-playbtn-first");
        btnLast = StandardWidgetsFactory.createImageButton("spriter-playbtn-last");
        btnPrev = StandardWidgetsFactory.createImageButton("spriter-playbtn-prev");
        btnNext = StandardWidgetsFactory.createImageButton("spriter-playbtn-next");

        cbLoop = StandardWidgetsFactory.createCheckBox("Loop");

        mainTable.row().padTop(6);
        mainTable.add(cbLoop).left();
        VisTable playTable = new VisTable();
        playTable.add(btnFirst).width(20);
        playTable.add(btnPrev).width(20).padLeft(8);
        playTable.add(btnStart).width(20).padLeft(8);
        playTable.add(btnNext).width(20).padLeft(8);
        playTable.add(btnLast).width(20).padLeft(8);
        mainTable.add(playTable).colspan(2).left();

        setListeners();
    }

    public Array<String> getAnimations() {
        return animationsSelectBox.getItems();
    }

    public void setAnimations(Array<String> animations) {
        animationsSelectBox.setItems(animations);
    }

    public String getAnimationSelected() {
        return animationsSelectBox.getSelected();
    }

    public void setSelectedAnimation(String currentAnimationName) {
        animationsSelectBox.setSelected(currentAnimationName);
    }

    public void changePlayBtnStage(boolean isPlaying) {
        btnStart.setChecked(isPlaying);
    }

    public boolean isPlayBtnChecked() {
        return btnStart.isChecked();
    }

    public void setCanAnimationLooping(boolean value) {
        cbLoop.setChecked(value);
    }

    public boolean isCanAnimationLooping() {
        return cbLoop.isChecked();
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    private void setListeners() {
        animationsSelectBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        btnStart.addListener(new CheckBoxChangeListener(START_BUTTON_CLICKED) {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                super.changed(changeEvent, actor);
                btnStart.setChecked(!btnStart.isChecked());
            }
        });
        btnFirst.addListener(new ButtonToNotificationListener(FIRST_BUTTON_CLICKED));
        btnPrev.addListener(new ButtonToNotificationListener(PREV_BUTTON_CLICKED));
        btnNext.addListener(new ButtonToNotificationListener(NEXT_BUTTON_CLICKED));
        btnLast.addListener(new ButtonToNotificationListener(LAST_BUTTON_CLICKED));
    }
}
