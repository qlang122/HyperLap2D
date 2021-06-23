package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
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
    private Semaphore lock = new Semaphore(1);

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
            File copiedFile = importExternalAnimationIntoProject(handle, progressHandler);
            if (copiedFile == null)
                continue;

            if (copiedFile.getName().toLowerCase().endsWith(".atlas")) {
                resolutionManager.resizeSpriterAnimationForAllResolutions(copiedFile, projectManager.getCurrentProjectInfoVO());
            }
        }
    }

    protected File importExternalAnimationIntoProject(FileHandle animationFileSource, ProgressHandler progressHandler) {
        try {
            String fileName = animationFileSource.name();
            if (!HyperLap2DUtils.SCML_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String sourcePath = animationFileSource.path();
            String animationDataPath;
            String targetPath;

            animationDataPath = FilenameUtils.getFullPathNoEndSeparator(sourcePath);
            targetPath = projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITER_DIR_PATH + File.separator + fileNameWithOutExt;
            FileHandle atlasFileSource = new FileHandle(animationDataPath + File.separator
                    + fileNameWithOutExt + ".atlas");

            File atlasFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".atlas");

            if (!atlasFileSource.exists()) {
                ArrayList<String> fileNames = readAssetFileNames(animationFileSource);
                File tempDir = new File(targetPath + File.separator + "temp");
                FileUtils.forceMkdir(tempDir);
                for (String name : fileNames) {
                    FileHandle imgSource = new FileHandle(animationDataPath + File.separator + name);
                    if (imgSource.exists()) {
                        String[] split = name.split("/");
                        String _name = split[split.length - 1];
                        File imgTarget = new File(targetPath + File.separator + "temp" + File.separator + _name);
                        FileUtils.copyFile(imgSource.file(), imgTarget);
                    } else {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not find '" + imgSource.name()
                                        + "'.\nCheck if the file exists in the same directory.").padBottom(20).pack();
                        return null;
                    }
                }

                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                TexturePacker.Settings settings = projectManager.getTexturePackerSettings();
                lock.acquire();
                TexturePacker.process(settings, tempDir.getAbsolutePath(), targetPath,
                        fileNameWithOutExt, new TexturePacker.ProgressListener() {
                            @Override
                            public void progress(float progress) {
                                progressHandler.progressChanged((progress * 100) % 100);
                                if (progress >= 1.0) lock.release();
                            }
                        });
                lock.acquire();
                FileUtils.deleteDirectory(tempDir);
                lock.release();
            } else {
                FileUtils.forceMkdir(new File(targetPath));

                Array<File> imageFiles = ImportUtils.getAtlasPages(atlasFileSource);
                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    if (!imageFile.exists()) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not find " + imageFile.getName()
                                        + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                        return null;
                    }
                }

                FileUtils.copyFile(atlasFileSource.file(), atlasFileTarget);

                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    FileHandle imgFileTarget = new FileHandle(targetPath + File.separator + imageFile.getName());
                    FileUtils.copyFile(imageFile, imgFileTarget.file());
                }
            }

            File scmlFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".scml");
            FileUtils.copyFile(animationFileSource.file(), scmlFileTarget);

            return atlasFileTarget;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Version getVersion(FileHandle fileHandle) {
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

    protected ArrayList<String> readAssetFileNames(FileHandle fileHandle) {
        XmlReader reader = new XmlReader();
        InputStream read = fileHandle.read();
        XmlReader.Element root = reader.parse(read);
        Array<XmlReader.Element> folders = root.getChildrenByName("folder");

        ArrayList<String> files = new ArrayList<>();
        for (XmlReader.Element folder : folders) {
            for (XmlReader.Element file : folder.getChildrenByName("file")) {
                String name = file.get("name");
                files.add(name);
            }
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
