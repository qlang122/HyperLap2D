package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import com.qlang.h2d.extention.spriter.SpriterObjectComponent;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.SpriterDataComponent;
import games.rednblack.editor.renderer.data.SpriterVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateSpriterDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private SpriterVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        SpriterVO vo = (SpriterVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new SpriterVO();
        backup.loadFromEntity(entity);

        SpriterDataComponent dataComponent = ComponentRetriever.get(entity, SpriterDataComponent.class);
        SpriterObjectComponent objectComponent = ComponentRetriever.get(entity, SpriterObjectComponent.class);

        dataComponent.isLooping = vo.isLooping;
        dataComponent.currentEntityIndex = vo.currentEntityIndex;
        dataComponent.currentAnimationName = vo.currentAnimationName;
        objectComponent.setAnimation(vo.currentAnimationName);
        objectComponent.setLooping(vo.isLooping);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        SpriterDataComponent dataComponent = ComponentRetriever.get(entity, SpriterDataComponent.class);
        SpriterObjectComponent objectComponent = ComponentRetriever.get(entity, SpriterObjectComponent.class);

        dataComponent.isLooping = backup.isLooping;
        dataComponent.currentEntityIndex = backup.currentEntityIndex;
        dataComponent.currentAnimationName = backup.currentAnimationName;
        objectComponent.setAnimation(backup.currentAnimationName);
        objectComponent.setLooping(backup.isLooping);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, SpriterVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
