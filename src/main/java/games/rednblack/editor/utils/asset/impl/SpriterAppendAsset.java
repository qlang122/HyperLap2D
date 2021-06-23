package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.File;

import games.rednblack.h2d.common.ProgressHandler;

public class SpriterAppendAsset extends SpriterAsset {

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File copiedFile = importExternalAnimationIntoProject(handle, progressHandler);
            if (copiedFile == null)
                continue;

            if (copiedFile.getName().toLowerCase().endsWith(".atlas")) {
                resolutionManager.resizeSpriterAnimationForAllResolutions(copiedFile, projectManager.getCurrentProjectInfoVO());
            }
        }
    }

    private Array<String> getCanAppendAnimationsName() {
        Array<String> list = new Array<>();

        return list;
    }
}
