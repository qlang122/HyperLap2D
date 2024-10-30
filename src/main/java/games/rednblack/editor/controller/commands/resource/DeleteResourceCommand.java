package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.h2d.common.vo.ResourceExtraData;

public abstract class DeleteResourceCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteResourceCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmAction() {
        return "Delete";
    }

    @Override
    protected String confirmDialogMessage() {
        ResourceExtraData data = notification.getBody();
        String imageName = data == null ? "" : (String) data.value1;
        return "Are you sure you want to delete '" + imageName +"'?";
    }

    @Override
    protected void callDoAction() {
        super.callDoAction();

        if (!isCancelled) {
            facade.sendNotification(DONE, notification.getBody());
        }
    }
}
