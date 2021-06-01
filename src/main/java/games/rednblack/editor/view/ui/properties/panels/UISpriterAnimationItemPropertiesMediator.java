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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.qlang.h2d.extention.spriter.SpriterObjectComponent;

import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import games.rednblack.editor.controller.commands.component.UpdateSpriterDataCommand;
import games.rednblack.editor.renderer.components.SpriterDataComponent;
import games.rednblack.editor.renderer.data.SpriterVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import me.winter.gdx.animation.Animation;

public class UISpriterAnimationItemPropertiesMediator extends UIItemPropertiesMediator<Entity, UISpriterAnimationItemProperties> {
    private static final String TAG = UISpriterAnimationItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private SpriterDataComponent spineDataComponent;
    private SpriterObjectComponent spineObjectComponent;

    public UISpriterAnimationItemPropertiesMediator() {
        super(NAME, new UISpriterAnimationItemProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UISpriterAnimationItemProperties.START_BUTTON_CLICKED,
                UISpriterAnimationItemProperties.FIRST_BUTTON_CLICKED,
                UISpriterAnimationItemProperties.PREV_BUTTON_CLICKED,
                UISpriterAnimationItemProperties.NEXT_BUTTON_CLICKED,
                UISpriterAnimationItemProperties.LAST_BUTTON_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        SpriterVO payloadVO = new SpriterVO();
        payloadVO.isLooping = viewComponent.isCanAnimationLooping();

        switch (notification.getName()) {
            case UISpriterAnimationItemProperties.START_BUTTON_CLICKED:
                payloadVO.setActionName(viewComponent.isPlayBtnChecked() ?
                        SpriterObjectComponent.ACTION_PAUSE_CLICKED :
                        SpriterObjectComponent.ACTION_PLAY_CLICKED);
                break;
            case UISpriterAnimationItemProperties.FIRST_BUTTON_CLICKED:
                payloadVO.setActionName(SpriterObjectComponent.ACTION_GO2FIRST_CLICKED);
                break;
            case UISpriterAnimationItemProperties.PREV_BUTTON_CLICKED:
                payloadVO.setActionName(SpriterObjectComponent.ACTION_GO2PREV_CLICKED);
                break;
            case UISpriterAnimationItemProperties.NEXT_BUTTON_CLICKED:
                payloadVO.setActionName(SpriterObjectComponent.ACTION_GO2NEXT_CLICKED);
                break;
            case UISpriterAnimationItemProperties.LAST_BUTTON_CLICKED:
                payloadVO.setActionName(SpriterObjectComponent.ACTION_GO2LAST_CLICKED);
                break;
        }
        if (payloadVO.getActionName().isEmpty()) return;

        Object payload = UpdateSpriterDataCommand.payload(observableReference, payloadVO);
        facade.sendNotification(MsgAPI.ACTION_ACTIONS_SPRITER_ANIMATION_DATA, payload);
    }

    @Override
    protected void translateObservableDataToView(Entity entity) {
        spineObjectComponent = ComponentRetriever.get(entity, SpriterObjectComponent.class);
        spineDataComponent = ComponentRetriever.get(entity, SpriterDataComponent.class);

        Array<String> animations = new Array<>();
        for (Animation animation : spineObjectComponent.getAnimations()) {
            animations.add(animation.getName());
        }

        viewComponent.setAnimations(animations);
        viewComponent.setSelectedAnimation(spineDataComponent.currentAnimationName);
        viewComponent.setCanAnimationLooping(spineDataComponent.isLooping);
        viewComponent.changePlayBtnStage(spineObjectComponent.isPlaying());
    }

    @Override
    protected void translateViewToItemData() {
        SpriterVO payloadVO = new SpriterVO();
        payloadVO.currentAnimationName = viewComponent.getAnimationSelected();
        payloadVO.isLooping = viewComponent.isCanAnimationLooping();

        Object payload = UpdateSpriterDataCommand.payload(observableReference, payloadVO);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_SPRITER_ANIMATION_DATA, payload);
    }
}
