package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by qlang on 6/23/2021.
 */
public class SpriterEditAnimationCommand extends NonRevertibleCommand {
    private static final String CLASS_NAME = "com.uwsoft.editor.controller.commands.resource.SpriterEditAnimations";
    public static final String DONE = CLASS_NAME + "DONE";

    public SpriterEditAnimationCommand() {
        setShowConfirmDialog(false);
    }

    @Override
    protected String confirmDialogTitle() {
        return "Edit animation";
    }

    @Override
    public void doAction() {
        String spriterAnimName = notification.getBody();
        facade.sendNotification(MsgAPI.SPRITER_EDIT_ANIMATION, spriterAnimName);

        cancel();
    }
}
