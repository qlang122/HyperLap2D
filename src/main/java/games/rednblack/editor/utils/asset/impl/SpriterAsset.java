package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.utils.Version;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;

public class SpriterAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            String contents = FileUtils.readFileToString(file.file(), "utf-8");
            return file.extension().equalsIgnoreCase("scml")
                    && (contents.contains("<spriter_data"));
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_SPRITER_ANIMATION;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITER_DIR_PATH + File.separator + file.nameWithoutExtension() + File.separator +
                    file.nameWithoutExtension() + ".scml");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File copiedFile = importExternalAnimationIntoProject(handle);
            if (copiedFile == null)
                continue;

            if (copiedFile.getName().toLowerCase().endsWith(".atlas")) {
                resolutionManager.resizeSpriterAnimationForAllResolutions(copiedFile, projectManager.getCurrentProjectInfoVO());
            }
        }
    }

    private File importExternalAnimationIntoProject(FileHandle animationFileSource) {
        try {
            String fileName = animationFileSource.name();
            if (!HyperLap2DUtils.SCML_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String sourcePath;
            String animationDataPath;
            String targetPath;
            if (HyperLap2DUtils.SCML_FILTER.accept(null, fileName)) {
                sourcePath = animationFileSource.path();

                animationDataPath = FilenameUtils.getFullPathNoEndSeparator(sourcePath);
                targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/spriter-animations" + File.separator + fileNameWithOutExt;
                FileHandle atlasFileSource = new FileHandle(animationDataPath + File.separator + fileNameWithOutExt + ".atlas");
                if (!atlasFileSource.exists()) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not find '" + atlasFileSource.name() + "'.\nCheck if the file exists in the same directory.").padBottom(20).pack();
                    return null;
                }
                Array<File> imageFiles = ImportUtils.getAtlasPages(atlasFileSource);
                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    if (!imageFile.exists()) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not find " + imageFile.getName() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                        return null;
                    }
                }

//                Version version = getVersion(animationFileSource);
//                Version that = new Version("1.0");
//                if (version.compareTo(that) < 0) {
//                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
//                            "\nCould not import Spriter Animation.\nRequired version >=" + that.get() + " found " + version.get()).padBottom(20).pack();
//                    return null;
//                }

                FileUtils.forceMkdir(new File(targetPath));
                File scmlFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".scml");
                File atlasFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".atlas");

                FileUtils.copyFile(animationFileSource.file(), scmlFileTarget);
                FileUtils.copyFile(atlasFileSource.file(), atlasFileTarget);

                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    FileHandle imgFileTarget = new FileHandle(targetPath + File.separator + imageFile.getName());
                    FileUtils.copyFile(imageFile, imgFileTarget.file());
                }

                return atlasFileTarget;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Version getVersion(FileHandle fileHandle) {
        Version version;

        String regex = "scml_version *= *\"(\\d+\\.\\d+\\.?\\d*)\"";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileHandle.readString());

        if (matcher.find()) {
            version = new Version(matcher.group(1));
        } else {
            version = new Version("0.0");
        }

        return version;
    }
}
