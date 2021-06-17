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

package games.rednblack.editor.view.ui.box.resourcespanel.draggable.box;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.SpriterVO;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.widget.actors.SpriterActor;
import games.rednblack.h2d.common.ResourcePayloadObject;

/**
 * Created by qlang on 28/5/2021.
 */
public class SpriterResource extends BoxItemResource {
    private final HyperLap2DFacade facade;
    private final Actor payloadActor;
    private final ResourcePayloadObject payload;
    private boolean isMouseInside = false;

    public SpriterResource(String animationName) {
        facade = HyperLap2DFacade.getInstance();

        SpriterVO vo = new SpriterVO();
        vo.animationName = animationName;
        SpriterActor animThumb = new SpriterActor(animationName, sandbox.getSceneControl().sceneLoader.getRm());

        if (animThumb.getWidth() > thumbnailSize || animThumb.getHeight() > thumbnailSize) {
            // resizing is needed
            float scaleFactor = 1.0f;
            if (animThumb.getWidth() > animThumb.getHeight()) {
                //scale by width
                scaleFactor = 1.0f / (animThumb.getWidth() / thumbnailSize);
            } else {
                scaleFactor = 1.0f / (animThumb.getHeight() / thumbnailSize);
            }
            animThumb.setScale(scaleFactor);
        } else {
            // resizing is needed
            float scaleFactor = 1.0f;
            if (animThumb.getWidth() > animThumb.getHeight()) {
                //scale by width
                scaleFactor = 1.0f / (thumbnailSize / animThumb.getWidth());
            } else {
                scaleFactor = 1.0f / (thumbnailSize / animThumb.getHeight());
            }
            animThumb.setScale(scaleFactor);
        }

        // put it in middle
        animThumb.setX((getWidth() - animThumb.getWidth() * animThumb.getScaleX()) / 2f);
        animThumb.setY((getHeight() - animThumb.getHeight() * animThumb.getScaleY()) / 2f);
        animThumb.animation.setPosition(getX() - (animThumb.getWidth() * animThumb.getScaleX()) - animThumb.getRectangle().x * animThumb.getScaleX(),
                getY() - (animThumb.getHeight() * animThumb.getScaleY()) - animThumb.getRectangle().y * animThumb.getScaleY());

        addListener(new ClickListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (animThumb.animation != null) animThumb.animation.setPlay(true);
                super.enter(event, x, y, pointer, fromActor);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (animThumb.animation != null) animThumb.animation.setPlay(false);
                super.enter(event, x, y, pointer, toActor);
            }
        });
        animThumb.setAnimation(animThumb.getAnimations().get(0).getName());

        addActor(animThumb);

        payloadActor = new SpriterActor(animationName, sandbox.getSceneControl().sceneLoader.getRm());
        payload = new ResourcePayloadObject();
        payload.name = animationName;
        payload.className = getClass().getName();

        setWidth(thumbnailSize);
        setHeight(thumbnailSize);

        setRightClickEvent(UIResourcesBoxMediator.SPRITER_ANIMATION_RIGHT_CLICK, payload.name);

        if (animThumb.animation != null) animThumb.animation.setPlay(true);
    }


    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public Actor getDragActor() {
        return payloadActor;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
