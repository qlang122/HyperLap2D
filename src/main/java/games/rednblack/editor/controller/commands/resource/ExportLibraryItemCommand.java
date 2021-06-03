package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.modules.*;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.common.vo.ExportMapperVO.ExportedAsset;

import org.apache.commons.io.FileUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportLibraryItemCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.ExportLibraryItemCommand";
    public static final String DONE = CLASS_NAME + "DONE";
    private final Json json = new Json(JsonWriter.OutputType.json);

    private final String currentProjectPath;
    private final ResourceManager resourceManager;
    private final ExportMapperVO exportMapperVO;

    public ExportLibraryItemCommand() {
        cancel();
        setShowConfirmDialog(false);
        currentProjectPath = projectManager.getCurrentProjectPath() + File.separator;
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        exportMapperVO = new ExportMapperVO();
    }

    @Override
    public void doAction() {
        exportMapperVO.mapper.clear();
        exportMapperVO.projectVersion = projectManager.currentProjectVO.projectVersion;

        String libraryItemName = notification.getBody();

        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer aFilterPatterns = stack.mallocPointer(1);
                aFilterPatterns.put(stack.UTF8("*.h2dlib"));
                aFilterPatterns.flip();

                FileHandle workspacePath = (settingsManager.getWorkspacePath() == null || !settingsManager.getWorkspacePath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getWorkspacePath();

                String fileName = TinyFileDialogs.tinyfd_saveFileDialog("Export Library Item...",
                        workspacePath.path() + File.separator + libraryItemName, aFilterPatterns,
                        "HyperLap2D Library (*.h2dlib)");
                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                    if (fileName != null) {
                        String fullFileName = fileName.endsWith(".h2dlib") ? fileName : fileName + ".h2dlib";

                        FileHandle file = new FileHandle(new File(fullFileName));
                        try {
                            if (file.exists()) {
                                FileUtils.forceDelete(file.file());
                            }
                            doExport(libraryItemName, file.pathWithoutExtension());

                            facade.sendNotification(DONE, libraryItemName);
                            facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "'" + libraryItemName + "' successfully exported");
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                FileUtils.deleteDirectory(new File(file.pathWithoutExtension() + "TMP"));
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    private void doExport(String libraryItemName, String destFile) throws IOException {
        File tempDir = new File(destFile + "TMP");
        FileUtils.forceMkdir(tempDir);

        CompositeItemVO compositeItemVO = libraryItems.get(libraryItemName).clone();
        adjustPPWCoordinates(compositeItemVO);

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + libraryItemName + ".lib"), json.toJson(compositeItemVO), "utf-8");

        exportAllAssets(compositeItemVO.composite, tempDir);

        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY, libraryItemName + ".lib"));

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + "mapper"), json.toJson(exportMapperVO), "utf-8");

        ZipUtils.pack(tempDir.getPath(), destFile + ".h2dlib");
        FileUtils.deleteDirectory(tempDir);
    }

    private void exportAllAssets(CompositeVO compositeVO, File tmpDir) throws IOException {
        for (SimpleImageVO imageVO : compositeVO.sImages) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_IMAGE, fileSrc.getName()));
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (AtlasImageVO imageVO : compositeVO.sAtlasImages) {
            File fileSrc = new File(currentProjectPath + ProjectManager.ATLAS_IMAGE_DIR_PATH);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            File[] files = fileSrc.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName().toLowerCase();
                    if (file.isFile() && (fileName.endsWith(".atlas") || fileName.endsWith(".png")))
                        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_TEXTURE_ATLAS, file.getName()));
                }
            }
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (Image9patchVO imageVO : compositeVO.sImage9patchs) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".9.png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_IMAGE, fileSrc.getName()));
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (SpineVO imageVO : compositeVO.sSpineAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPINE_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPINE_ANIMATION, fileSrc.getName() + ".json"));
            copyShader(imageVO.shaderName, tmpDir);
        }
        for (SpriterVO imageVO : compositeVO.sSpriterAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPRITER_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPRITER_ANIMATION, fileSrc.getName() + ".scml"));
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPRITER_ANIMATION, fileSrc.getName() + ".atlas"));
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (SpriteAnimationVO imageVO : compositeVO.sSpriteAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPRITE_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS, fileSrc.getName() + ".atlas"));
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (ParticleEffectVO imageVO : compositeVO.sParticleEffects) {
            File fileSrc = new File(currentProjectPath + ProjectManager.PARTICLE_DIR_PATH + File.separator + imageVO.particleName);
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_PARTICLE_EFFECT, fileSrc.getName()));
            ParticleEffect particleEffect = new ParticleEffect(resourceManager.getParticleEffect(imageVO.particleName));
            for (ParticleEmitter emitter : particleEffect.getEmitters()) {
                for (String path : emitter.getImagePaths()) {
                    File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + path);
                    FileUtils.copyFileToDirectory(f, tmpDir);
                }
            }
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (TalosVO imageVO : compositeVO.sTalosVFX) {
            File fileSrc = new File(currentProjectPath + ProjectManager.TALOS_VFX_DIR_PATH + File.separator + imageVO.particleName);
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_TALOS_VFX, fileSrc.getName()));
            ParticleEffectDescriptor particleEffect = resourceManager.getProjectTalosList().get(imageVO.particleName);
            for (ParticleEmitterDescriptor emitter : new Array.ArrayIterator<>(particleEffect.emitterModuleGraphs)) {
                for (AbstractModule module : new Array.ArrayIterator<>(emitter.getModules())) {
                    if (module instanceof TextureModule) {
                        String path = ((TextureModule) module).regionName + ".png";
                        File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + path);
                        FileUtils.copyFileToDirectory(f, tmpDir);
                    }

                    if (module instanceof PolylineModule) {
                        String path = ((PolylineModule) module).regionName + ".png";
                        File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + path);
                        FileUtils.copyFileToDirectory(f, tmpDir);
                    }

                    if (module instanceof FlipbookModule) {
                        String path = ((FlipbookModule) module).regionName + ".png";
                        File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + path);
                        FileUtils.copyFileToDirectory(f, tmpDir);
                    }

                    if (module instanceof ShadedSpriteModule) {
                        String path = ((ShadedSpriteModule) module).shdrFileName;
                        File f = new File(currentProjectPath + ProjectManager.TALOS_VFX_DIR_PATH + File.separator + path);
                        FileUtils.copyFileToDirectory(f, tmpDir);
                    }
                }
            }
            copyShader(imageVO.shaderName, tmpDir);
        }

        for (CompositeItemVO compositeItemVO : compositeVO.sComposites) {
            exportAllAssets(compositeItemVO.composite, tmpDir);
        }
    }

    private void copyShader(String shaderName, File tmpDir) throws IOException {
        if (shaderName.equals(""))
            return;

        File f = new File(currentProjectPath + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName + ".frag");
        FileUtils.copyFileToDirectory(f, tmpDir);

        File v = new File(currentProjectPath + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName + ".vert");
        FileUtils.copyFileToDirectory(v, tmpDir);

        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SHADER, shaderName + ".frag"));
        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SHADER, shaderName + ".vert"));
    }

    private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
        for (MainItemVO item : compositeItemVO.composite.getAllItems()) {
            item.originX = item.originX * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            item.originY = item.originY * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            item.x = item.x * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            item.y = item.y * projectManager.getCurrentProjectInfoVO().pixelToWorld;

            if (item instanceof CompositeItemVO) {
                ((CompositeItemVO) item).width = ((CompositeItemVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
                ((CompositeItemVO) item).height = ((CompositeItemVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            }

            if (item instanceof Image9patchVO) {
                ((Image9patchVO) item).width = ((Image9patchVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
                ((Image9patchVO) item).height = ((Image9patchVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            }

            if (item instanceof LabelVO) {
                ((LabelVO) item).width = ((LabelVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
                ((LabelVO) item).height = ((LabelVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
            }
        }
    }
}
