package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.h2d.common.ProgressHandler;

public class SpriterAppendAsset extends SpriterAsset {
    private String beAppendToAnimation;

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        Array<String> list = new Array<>();
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File file = importExternalAnimation(handle, progressHandler);
            if (file != null) list.add(FilenameUtils.removeExtension(file.getName()));
        }

    }

    public void setNeedAppendAnimation(String animation) {
        beAppendToAnimation = animation;
    }

    private File importExternalAnimation(FileHandle animationFileSource, ProgressHandler progressHandler) {
        try {
            String fileName = animationFileSource.name();
            if (!HyperLap2DUtils.SCML_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String targetPath = projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITER_DIR_PATH + File.separator + "extra";

            FileUtils.forceMkdir(new File(targetPath));

            File scmlFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".scml");
            FileUtils.copyFile(animationFileSource.file(), scmlFileTarget);
            return scmlFileTarget;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Array<String> getCanAppendAnimationsName() {
        Array<String> list = new Array<>();

        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "extra";

        File files = new File(targetPath);
        for (File file : files.listFiles()) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".scml")) {
                list.add(FilenameUtils.removeExtension(file.getName()));
            }
        }

        return list;
    }
}
