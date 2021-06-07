package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageAsset extends Asset {

    @Override
    public int matchType(Array<FileHandle> files) {
        String[] names = new String[files.size];
        for (int i = 0; i < files.size; i++) {
            names[i] = files.get(i).nameWithoutExtension();
        }

        return ImportUtils.isAnimationSequence(names) ? ImportUtils.TYPE_UNKNOWN : super.matchType(files);
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_IMAGE;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            BufferedImage sourceImg = ImageIO.read(new FileInputStream(file.file()));
            int width = sourceImg.getWidth();
            int height = sourceImg.getHeight();
            TexturePacker.Settings settings = projectManager.getTexturePackerSettings();
            if (width >= settings.maxWidth || height >= settings.maxHeight) {
                Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                        "\nImage width and height can't be max " + settings.maxWidth + "x" + settings.maxHeight
                                + " and X edge padding " + settings.paddingX + "*" + settings.paddingY + ":\n"
                                + file.file().getName()).padBottom(20).pack();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.extension().equalsIgnoreCase("png");
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.IMAGE_DIR_PATH + File.separator + file.nameWithoutExtension() + ".png");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        projectManager.copyImageFilesForAllResolutionsIntoProject(files, true, progressHandler);

        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
    }
}
