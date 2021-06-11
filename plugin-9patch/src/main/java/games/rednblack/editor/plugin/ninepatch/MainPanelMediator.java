package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by azakhary on 8/18/2015.
 */
public class MainPanelMediator extends Mediator<MainPanel> {
    private static final String TAG = MainPanelMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private NinePatchPlugin plugin;

    private ImageUtils imageUtils = new ImageUtils();

    public MainPanelMediator(NinePatchPlugin plugin) {
        super(NAME, new MainPanel(plugin.facade));
        this.plugin = plugin;
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                NinePatchPlugin.EDIT_NINE_PATCH,
                NinePatchPlugin.CONVERT_TO_NINE_PATCH,
                MainPanel.SAVE_CLICKED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case NinePatchPlugin.EDIT_NINE_PATCH:
                loadNinePatch();
                break;
            case NinePatchPlugin.CONVERT_TO_NINE_PATCH:
                convertImageToNinePatch();
                break;
            case MainPanel.SAVE_CLICKED:
                Entity entity = plugin.currEditingEntity;
                NinePatchComponent ninePatchComponent = ComponentRetriever.get(entity, NinePatchComponent.class);
                String atlasName = ninePatchComponent.textureAtlasName;
                if (atlasName.isEmpty())
                    applyNewSplitsToImage(ninePatchComponent.textureRegionName, viewComponent.getSplits());
                else
                    applyNewSplitsToAtlasImage(atlasName, ninePatchComponent.textureRegionName, viewComponent.getSplits());
                viewComponent.hide();
                break;
        }
    }

    private void convertImageToNinePatch() {
        Entity entity = plugin.currEditingEntity;
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
        mainItemComponent.entityType = EntityFactory.NINE_PATCH;
        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        String atlasName = textureRegionComponent.textureAtlasName;
        String regionName = textureRegionComponent.regionName;
        NinePatchComponent ninePatchComponent = plugin.getAPI().getEngine().createComponent(NinePatchComponent.class);
        ninePatchComponent.textureAtlasName = atlasName;
        ninePatchComponent.textureRegionName = regionName;
        TextureAtlas.AtlasRegion newRegion = (TextureAtlas.AtlasRegion) textureRegionComponent.region;
        int[] splits = {0, 0, 0, 0};
        int[] pad = {0, 0, 0, 0};
        newRegion.names = new String[]{"split", "pad"};
        newRegion.values = new int[][]{splits, pad};
        ninePatchComponent.ninePatch = new NinePatch(textureRegionComponent.region, 0, 0, 0, 0);
        entity.add(ninePatchComponent);

        if (atlasName.isEmpty()) {
            //remove original image
            File originalImg = new File(plugin.getAPI().getProjectPath() + "/assets/orig/images/" + regionName + ".png");
            originalImg.delete();
        } else {
            File originalImg = new File(plugin.getAPI().getProjectPath() + "/assets/orig/atlas-images/" + atlasName + File.separator + regionName + ".png");
            originalImg.delete();
        }

        //save project
        plugin.getAPI().saveProject();

        if (atlasName.isEmpty()) {
            //save split data
            addSplitsToImageInAtlas(regionName, splits);
            applyNewSplitsToImage(regionName, splits);
        } else {
            addSplitsToAtlasImageInAtlas(atlasName, regionName, splits);
            applyNewSplitsToAtlasImage(atlasName, regionName, splits);
        }

        // reload
        plugin.getAPI().reLoadProject();
    }

    private void loadNinePatch() {
        Entity entity = plugin.currEditingEntity;
        NinePatchComponent ninePatchComponent = ComponentRetriever.get(entity, NinePatchComponent.class);
        loadRegion(ninePatchComponent.textureAtlasName, ninePatchComponent.textureRegionName);
        viewComponent.show(plugin.getAPI().getUIStage());
    }

    private void addSplitsToImageInAtlas(String textureRegionName, int[] splits) {
        String atlasPath = plugin.getAPI().getProjectPath() + "/assets/orig/pack/pack.atlas";
        FileHandle packAtlas = Gdx.files.internal(atlasPath);
        String content = packAtlas.readString();
        int regionIndex = content.indexOf(textureRegionName);
        int splitEnd = content.indexOf("orig: ", regionIndex);
        String splitStr = "split: " + splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3] + "\n  ";
        String newContent = content.substring(0, splitEnd) + splitStr + content.substring(splitEnd, content.length());
        File test = new File(atlasPath);
        writeFile(newContent, test);
    }

    private void addSplitsToAtlasImageInAtlas(String atlasName, String regionName, int[] splits) {
        String atlasPath = plugin.getAPI().getProjectPath() + "/assets/orig/atlas-images/" + atlasName + ".atlas";
        FileHandle packAtlas = Gdx.files.internal(atlasPath);
        String content = packAtlas.readString();
        int regionIndex = content.indexOf(regionName);
        int splitEnd = content.indexOf("orig: ", regionIndex);
        String splitStr = "split: " + splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3] + "\n  ";
        String newContent = content.substring(0, splitEnd) + splitStr + content.substring(splitEnd, content.length());
        File test = new File(atlasPath);
        writeFile(newContent, test);
    }

    private void applyNewSplitsToImage(String textureRegionName, int[] splits) {
        // first need to modify original image
        String atlasPath = plugin.getAPI().getProjectPath() + "/assets/orig/pack/pack.atlas";
        FileHandle packAtlas = Gdx.files.internal(atlasPath);
        FileHandle imagesDir = Gdx.files.internal(plugin.getAPI().getProjectPath() + "/assets/orig/pack/");
        TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(packAtlas, imagesDir, false);
        BufferedImage finalImage = imageUtils.extractImage(atlas, textureRegionName, splits);
        imageUtils.saveImage(finalImage, plugin.getAPI().getProjectPath() + "/assets/orig/images/" + textureRegionName + ".9.png");

        // now need to modify the pack
        String content = packAtlas.readString();
        int regionIndex = content.indexOf(textureRegionName);
        int splitStart = content.indexOf("split: ", regionIndex) + "split: ".length();
        int splitEnd = content.indexOf("orig: ", splitStart);
        String splitStr = splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3] + "\n  ";
        String newContent = content.substring(0, splitStart) + splitStr + content.substring(splitEnd, content.length());
        File test = new File(atlasPath);
        writeFile(newContent, test);
    }

    private void applyNewSplitsToAtlasImage(String atlasName, String regionName, int[] splits) {
        // first need to modify original image
        String atlasPath = plugin.getAPI().getProjectPath() + "/assets/orig/atlas-images/" + atlasName + ".atlas";
        FileHandle packAtlas = Gdx.files.internal(atlasPath);
        FileHandle imagesDir = Gdx.files.internal(plugin.getAPI().getProjectPath() + "/assets/orig/atlas-images/");
        TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(packAtlas, imagesDir, false);
        BufferedImage finalImage = imageUtils.extractImage(atlas, regionName, splits);
        imageUtils.saveImage(finalImage, imagesDir.path() + File.separator + atlasName + File.separator + regionName + ".9.png");

        // now need to modify the pack
        String content = packAtlas.readString();
        int regionIndex = content.indexOf(regionName);
        int splitStart = content.indexOf("split: ", regionIndex) + "split: ".length();
        int splitEnd = content.indexOf("orig: ", splitStart);
        String splitStr = splits[0] + ", " + splits[1] + ", " + splits[2] + ", " + splits[3] + "\n  ";
        String newContent = content.substring(0, splitStart) + splitStr + content.substring(splitEnd, content.length());
        File test = new File(atlasPath);
        writeFile(newContent, test);
    }

    private void writeFile(String content, File file) {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRegion(String atlasName, String name) {
        TextureAtlas atlas;
        if (atlasName.isEmpty()) {
            atlas = plugin.getAPI().getProjectTextureAtlas();
        } else atlas = plugin.getAPI().getAtlasImageAtlas(atlasName);

        validateNinePatchTextureRegion(atlas.findRegion(name));
        viewComponent.setTexture(atlas.findRegion(name));

        viewComponent.setListeners(plugin.getAPI().getUIStage());
    }

    private void validateNinePatchTextureRegion(TextureAtlas.AtlasRegion texture) {
        int[] s = texture.findValue("split");
        if (s == null) {
            // Add splits to the atlasRegion if they are missing
            fixNinePatch(texture);
        }
    }

    private void fixNinePatch(TextureAtlas.AtlasRegion texture) {
        int[] splits = {0, 0, 0, 0};
        int[] pad = {0, 0, 0, 0};
        texture.names = new String[]{"split", "pad"};
        texture.values = new int[][]{splits, pad};

        //remove original image
        File originalImg = new File(plugin.getAPI().getProjectPath() + "/assets/orig/images/" + texture.name + ".png");
        originalImg.delete();

        //save project
        plugin.getAPI().saveProject();

        //save split data
        addSplitsToImageInAtlas(texture.name, splits);
    }
}
