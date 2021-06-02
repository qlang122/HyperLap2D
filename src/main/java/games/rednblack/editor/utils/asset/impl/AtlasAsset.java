package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;

public class AtlasAsset extends Asset {

    @Override
    protected int getType() {
        return ImportUtils.TYPE_TEXTURE_ATLAS;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
            return file.extension().equalsIgnoreCase("atlas") && !ImportUtils.isAtlasAnimationSequence(atlas.getRegions());
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.ATLAS_IMAGE_DIR_PATH + File.separator + file.nameWithoutExtension() + File.separator +
                    file.nameWithoutExtension() + ".atlas");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File copiedFile = importExternalIntoProject(handle, progressHandler);
            if (copiedFile == null)
                continue;

            if (copiedFile.getName().toLowerCase().endsWith(".atlas")) {
                resolutionManager.resizeAtlasForAllResolutions(copiedFile, projectManager.getCurrentProjectInfoVO());
            }
        }
    }

    private File importExternalIntoProject(FileHandle atlasFileSource, ProgressHandler progressHandler) {
        try {
            String fileName = atlasFileSource.name();
            if (!HyperLap2DUtils.ATLAS_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
//            String sourcePath = atlasFileSource.path();
//            String sourceDataPath;
            String targetPath;

            int progress = 0;

//            sourceDataPath = FilenameUtils.getFullPathNoEndSeparator(sourcePath);
            targetPath = projectManager.getCurrentProjectPath() + ProjectManager.ATLAS_IMAGE_DIR_PATH;

            File atlasFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".atlas");

            Array<File> imageFiles = ImportUtils.getAtlasPages(atlasFileSource);
            for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                if (!imageFile.exists()) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not find " + imageFile.getName() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                    return null;
                }
            }
            progress += 5;
            progressHandler.progressChanged(progress);

            FileUtils.forceMkdir(new File(targetPath));

            File tempDir = new File(targetPath + File.separator + fileNameWithOutExt);
            FileUtils.forceMkdir(tempDir);
            unpackAtlasIntoFolder(atlasFileSource.file(), tempDir.getAbsolutePath());
            progress += 30;
            progressHandler.progressChanged(progress);

            FileUtils.copyFile(atlasFileSource.file(), atlasFileTarget);

            int size = imageFiles.size;
            int pp = (100 - progress) / size;
            for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                FileHandle imgFileTarget = new FileHandle(targetPath + File.separator + imageFile.getName());
                FileUtils.copyFile(imageFile, imgFileTarget.file());
                progress += pp;
                progressHandler.progressChanged(progress % 100);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void unpackAtlasIntoFolder(File atlasFile, String dir) {
        FileHandle atlasFileHandle = new FileHandle(atlasFile);
        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(atlasFileHandle, atlasFileHandle.parent(), false);
        TextureUnpacker unpacker = new TextureUnpacker();
        unpacker.splitAtlas(atlasData, dir);
    }
}
