package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by qlang on 6/23/2021.
 */
public class SpriterAppendCommand extends NonRevertibleCommand {
    private static final String CLASS_NAME = "com.uwsoft.editor.controller.commands.resource.SpriterAppendAnimations";
    public static final String DONE = CLASS_NAME + "DONE";

    public SpriterAppendCommand() {

    }

    @Override
    protected String confirmDialogTitle() {
        return "Append into animation";
    }

    @Override
    protected String confirmDialogMessage() {
        String msg = super.confirmDialogMessage();
        return "Append new animation into current," + msg;
    }

    @Override
    public void doAction() {
        String spriterAnimName = notification.getBody();
        facade.sendNotification(MsgAPI.SPRITER_APPEND_ANIMATION, spriterAnimName);

        cancel();
    }
}
