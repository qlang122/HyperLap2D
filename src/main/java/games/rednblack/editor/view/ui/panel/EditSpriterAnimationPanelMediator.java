/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.impl.SpriterEditAsset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;

/**
 * @author Created by qlang on 6/25/2021.
 */
public class EditSpriterAnimationPanelMediator extends Mediator<EditSpriterAnimationPanel> {
    private static final String TAG = EditSpriterAnimationPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private ProgressHandler progressHandler;

    private final SpriterEditAsset asset;

    private String beEditToAnimation;

    public EditSpriterAnimationPanelMediator() {
        super(NAME, new EditSpriterAnimationPanel());
        asset = new SpriterEditAsset();
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        progressHandler = new AssetsImportProgressHandler();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SPRITER_EDIT_ANIMATION,
                EditSpriterAnimationPanel.BROWSE_BTN_CLICKED,
                EditSpriterAnimationPanel.ADD_BTN_CLICKED,
                EditSpriterAnimationPanel.REMOVE_BTN_CLICKED,
                EditSpriterAnimationPanel.IMPORT_FAILED,
                EditSpriterAnimationPanel.REMOVE_FAILED,
                MsgAPI.SPRITER_REMOVE_ANIMATION_BACKGROUND,
                MsgAPI.ACTION_FILES_DROPPED,
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case MsgAPI.SPRITER_EDIT_ANIMATION:
                beEditToAnimation = notification.getBody();
                asset.setCurrentAnimation(beEditToAnimation);
                viewComponent.show(uiStage);
                viewComponent.setAnimations(asset.getCanAppendAnimationsName());
                viewComponent.setAppendedAnimations(asset.getExtraAnimations());
                break;
            case EditSpriterAnimationPanel.BROWSE_BTN_CLICKED:
                showFileChoose();
                break;
            case EditSpriterAnimationPanel.ADD_BTN_CLICKED:
                String name = notification.getBody();
                if (!asset.getExtraAnimations().contains(name, false)) {
                    ArrayList<String> names = new ArrayList<>(1);
                    names.add(name);
                    asset.importAsset(names, progressHandler);
                }
                break;
            case MsgAPI.ACTION_FILES_DROPPED:
                ImportPanel.DropBundle bundle = notification.getBody();
                if (viewComponent.checkDropRegionHit(bundle.pos)) {
                    postPathObtainAction(bundle.paths);
                }
                break;
            case EditSpriterAnimationPanel.IMPORT_FAILED:
                viewComponent.showError(ImportUtils.TYPE_FAILED);
                break;
            case EditSpriterAnimationPanel.REMOVE_BTN_CLICKED:
                String anim = notification.getBody();
                Dialogs.showConfirmDialog(Sandbox.getInstance().getUIStage(),
                        "Delete animation", "The source file will not be delete, only delete the references.\nwould you like to remove it? ",
                        new String[]{"Ok", "Cancel"}, new Integer[]{0, 1}, result -> {
                            if (result == 0) {
                                doDelete(anim);
                            }
                        }).padBottom(20).pack();
                break;
            case EditSpriterAnimationPanel.REMOVE_FAILED:
                viewComponent.showError(null);
                break;
            case MsgAPI.SPRITER_REMOVE_ANIMATION_BACKGROUND:
                beEditToAnimation = notification.getBody();
                asset.setCurrentAnimation(beEditToAnimation);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    asset.deleteExtraAnimations(beEditToAnimation);
                });
                executor.shutdown();
                break;
        }
    }

    private void showFileChoose() {
        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FileTypeFilter.Rule allSupportedRule = ImportUtils.getInstance().getFileTypeFilter().getRules().get(0);
                PointerBuffer aFilterPatterns = stack.mallocPointer(allSupportedRule.getExtensions().size);
                for (String ext : new Array.ArrayIterator<>(allSupportedRule.getExtensions())) {
                    aFilterPatterns.put(stack.UTF8("*." + ext));
                }
                aFilterPatterns.flip();

                SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
                FileHandle importPath = (settingsManager.getImportPath() == null || !settingsManager.getImportPath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getImportPath();

                String files = TinyFileDialogs.tinyfd_openFileDialog("Import Resources...", importPath.path(), aFilterPatterns, allSupportedRule.getDescription(), true);
                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                    if (files != null) {
                        String[] paths = files.split("\\|");
                        if (paths.length > 0) {
                            postPathObtainAction(paths);
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    public void postPathObtainAction(String[] paths) {
        int fileType = ImportUtils.TYPE_UNKNOWN;

        Array<FileHandle> files = getFilesFromPaths(paths);
        fileType = asset.matchType(files);
        if (fileType > 0) {
            if (asset.checkExistence(files)) {
                int type = fileType;
                Dialogs.showConfirmDialog(Sandbox.getInstance().getUIStage(),
                        "Duplicate file", "You have already an asset with this name,\nwould you like to overwrite it?",
                        new String[]{"Overwrite", "Cancel"}, new Integer[]{0, 1}, result -> {
                            if (result == 0) {
                                initImportUI(type, files);
                                asset.asyncImport(files, progressHandler, false);
                            }
                        }).padBottom(20).pack();
            } else {
                initImportUI(fileType, files);
                asset.asyncImport(files, progressHandler, false);
            }
        }

        if (fileType <= 0) {
            viewComponent.showError(fileType);
        }
    }

    private void doDelete(String animation) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean isOk = asset.deleteExtraAnimation(animation);

            if (isOk) {
                reloadAsset();
            } else facade.sendNotification(EditSpriterAnimationPanel.REMOVE_FAILED);
        });
        executor.shutdown();
    }

    private void initImportUI(int type, Array<FileHandle> files) {
        SettingsManager settingsManager = HyperLap2DFacade.getInstance().retrieveProxy(SettingsManager.NAME);
        settingsManager.setLastImportedPath(files.get(0).parent().path());

        viewComponent.setImportingView(type, files.size);
    }

    private Array<FileHandle> getFilesFromPaths(String[] paths) {
        Array<FileHandle> files = new Array<>();
        for (String path : paths) {
            files.add(new FileHandle(new File(path)));
        }

        return files;
    }

    private void reloadAsset() {
        Gdx.app.postRunnable(() -> {
            Sandbox sandbox = Sandbox.getInstance();
            ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
            projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectPath());
            sandbox.loadCurrentProject();
            viewComponent.setDroppingView();
            viewComponent.setAnimations(asset.getCanAppendAnimationsName());
            viewComponent.setAppendedAnimations(asset.getExtraAnimations());
            facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
        });
    }

    public class AssetsImportProgressHandler implements ProgressHandler {

        @Override
        public void progressStarted() {
            viewComponent.getProgressBar().setValue(0);
        }

        @Override
        public void progressChanged(float value) {
            viewComponent.getProgressBar().setValue(value);
        }

        @Override
        public void progressComplete() {
            reloadAsset();
        }

        @Override
        public void progressFailed() {
            Gdx.app.postRunnable(() -> {
                facade.sendNotification(EditSpriterAnimationPanel.IMPORT_FAILED);
            });
        }
    }
}
