package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import com.qlang.h2d.extention.spriter.SpriterObjectComponent;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.data.SpriterVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

public class ActionsSpriterDataCommand extends EntityModifyRevertibleCommand {
    private Integer entityId;
    private SpriterVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        SpriterVO vo = (SpriterVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new SpriterVO();

        SpriterObjectComponent component = ComponentRetriever.get(entity, SpriterObjectComponent.class);
        component.setLooping(vo.isLooping);
        component.doActions(vo.getActionName());

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);

        SpriterObjectComponent component = ComponentRetriever.get(entity, SpriterObjectComponent.class);
        component.setLooping(backup.isLooping);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
