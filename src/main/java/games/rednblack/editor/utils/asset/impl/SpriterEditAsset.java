package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.SpriterRelationVO;
import games.rednblack.editor.renderer.data.SpriterVO;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.h2d.common.ProgressHandler;

/**
 * @author Created by qlang on 6/25/2021.
 */
public class SpriterEditAsset extends SpriterAsset {
    private ArrayList<String> animationNames = new ArrayList<>();
    private String currentAnimation;

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITER_DIR_PATH + File.separator + "extra" + File.separator +
                    file.nameWithoutExtension() + ".scml");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        animationNames.clear();

        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File file = importExternalAnimation(handle, progressHandler);
            if (file != null) animationNames.add(FilenameUtils.removeExtension(file.getName()));
        }

        if (saveToConfigFile(animationNames)) {
        }
    }

    public void importAsset(ArrayList<String> names, ProgressHandler progressHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (saveToConfigFile(names)) {
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

    private boolean saveToConfigFile(ArrayList<String> names) {
        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "anim-relation.dt";

        try {
            SpriterRelationVO rVO = null;
            File file = new File(targetPath);
            if (file.exists()) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                String jsonString = FileUtils.readFileToString(file, "utf-8");
                rVO = json.fromJson(SpriterRelationVO.class, jsonString);
            }
            SpriterVO vo = null;
            if (rVO != null) {
                vo = rVO.animations.get(currentAnimation);
            } else rVO = new SpriterRelationVO();

            if (vo == null) vo = new SpriterVO();
            vo.animations.addAll(names);
            rVO.animations.put(currentAnimation, vo);

            FileUtils.writeStringToFile(file, rVO.constructJsonString(), "utf-8");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setCurrentAnimation(String animation) {
        currentAnimation = animation;
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

    public Array<String> getCanAppendAnimationsName() {
        Array<String> list = new Array<>();

        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "extra";

        try {
            FileUtils.forceMkdir(new File(targetPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File files = new File(targetPath);
        for (File file : files.listFiles()) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".scml")) {
                list.add(FilenameUtils.removeExtension(file.getName()));
            }
        }

        return list;
    }

    public Array<String> getExtraAnimations() {
        Array<String> list = new Array<>();
        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "anim-relation.dt";

        try {
            SpriterRelationVO rVO = null;
            File file = new File(targetPath);
            if (file.exists()) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                String jsonString = FileUtils.readFileToString(file, "utf-8");
                rVO = json.fromJson(SpriterRelationVO.class, jsonString);
            }
            if (rVO != null && rVO.animations != null && rVO.animations.containsKey(currentAnimation)) {
                SpriterVO spriterVO = rVO.animations.get(currentAnimation);
                for (String s : spriterVO.animations) {
                    list.add(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteExtraAnimation(String extraAnimation) {
        if (extraAnimation == null || extraAnimation.isEmpty()) return false;

        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "anim-relation.dt";

        try {
            SpriterRelationVO rVO = null;
            File file = new File(targetPath);
            if (file.exists()) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                String jsonString = FileUtils.readFileToString(file, "utf-8");
                rVO = json.fromJson(SpriterRelationVO.class, jsonString);
            }
            if (rVO == null) return false;

            SpriterVO vo = rVO.animations.get(currentAnimation);
            if (vo == null) return false;

            vo.animations.remove(extraAnimation);
            rVO.animations.put(currentAnimation, vo);

            FileUtils.writeStringToFile(file, rVO.constructJsonString(), "utf-8");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteExtraAnimations(String animation) {
        if (animation == null || animation.isEmpty()) return false;

        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITER_DIR_PATH + File.separator + "anim-relation.dt";

        try {
            SpriterRelationVO rVO = null;
            File file = new File(targetPath);
            if (file.exists()) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                String jsonString = FileUtils.readFileToString(file, "utf-8");
                rVO = json.fromJson(SpriterRelationVO.class, jsonString);
            }
            if (rVO == null) return false;

            rVO.animations.remove(animation);

            FileUtils.writeStringToFile(file, rVO.constructJsonString(), "utf-8");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
