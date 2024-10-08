package games.rednblack.editor.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;
import com.talosvfx.talos.runtime.utils.VectorField;

import games.rednblack.editor.data.SpriterAnimData;
import games.rednblack.editor.renderer.data.*;

import games.rednblack.editor.renderer.utils.H2DSkinLoader;
import games.rednblack.editor.renderer.utils.ShadedDistanceFieldFont;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;

import games.rednblack.h2d.extension.talos.ResourceRetrieverAssetProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Json;

import games.rednblack.editor.data.SpineAnimData;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.resources.FontSizePair;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

import org.puremvc.java.patterns.proxy.Proxy;

/**
 * Created by azakhary on 4/26/2015.
 */
public class ResourceManager extends Proxy implements IResourceRetriever {

    public String packResolutionName = "orig";

    private static final String TAG = ResourceManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private final HashMap<String, ParticleEffect> particleEffects = new HashMap<>(1);
    private final HashMap<String, ParticleEffectDescriptor> talosVFXs = new HashMap<>(1);
    private final HashMap<String, FileHandle> talosVFXsFiles = new HashMap<>(1);
    private HashMap<String, TextureAtlas> currentProjectAtlas = new HashMap<>(1);

    private final HashMap<String, SpineAnimData> spineAnimAtlases = new HashMap<>();
    private final HashMap<String, Array<TextureAtlas.AtlasRegion>> spriteAnimAtlases = new HashMap<>();
    private final HashMap<String, SpriterAnimData> spriterAnimAtlases = new HashMap<>();
    private final HashMap<String, TextureAtlas> atlasImageAtlases = new HashMap<>();
    private final HashMap<FontSizePair, BitmapFont> bitmapFonts = new HashMap<>();
    private final HashMap<String, ShaderProgram> shaderPrograms = new HashMap<>(1);

    private TextureRegion defaultRegion;

    private ResolutionManager resolutionManager;
    private SettingsManager settingsManager;
    private PixmapPacker fontPacker;

    public ResourceManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        WhitePixel.initializeShared();

        PixmapPacker packer = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.SkylineStrategy());
        packer.setTransparentColor(Color.WHITE);
        packer.getTransparentColor().a = 0;

        FreeTypeFontGenerator monoGenerator = new FreeTypeFontGenerator(Gdx.files.internal("freetypefonts/FiraCode-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters += "⌘⇧⌥\u25CF\u2022";
        parameter.kerning = true;
        parameter.renderCount = 3;
        parameter.packer = packer;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        BitmapFont defaultMono = monoGenerator.generateFont(parameter);
        defaultMono.setFixedWidthGlyphs(parameter.characters);
        monoGenerator.dispose();

//        TextureRegion dejavuRegion = new TextureRegion(new Texture(Gdx.files.internal("style/default-font-32.png")));
//        ShadedDistanceFieldFont smallDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        ShadedDistanceFieldFont smallDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal(ProjectManager.DEFAULT_FONT));
        smallDistanceField.setDistanceFieldSmoothing(1.5f);
        smallDistanceField.getData().setScale(9.0f / smallDistanceField.getCapHeight());//font 9
//        ShadedDistanceFieldFont defaultDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        ShadedDistanceFieldFont defaultDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal(ProjectManager.DEFAULT_FONT));
        defaultDistanceField.setDistanceFieldSmoothing(1.5f);
        defaultDistanceField.getData().setScale(10.0f / defaultDistanceField.getCapHeight());//font 10
//        ShadedDistanceFieldFont bigDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal("style/default-font-32.fnt"), dejavuRegion);
        ShadedDistanceFieldFont bigDistanceField = new ShadedDistanceFieldFont(Gdx.files.internal(ProjectManager.DEFAULT_FONT));
        bigDistanceField.setDistanceFieldSmoothing(1.5f);
        bigDistanceField.getData().setScale(11.0f / bigDistanceField.getCapHeight());//font 12

        /* Create the ObjectMap and add the fonts to it */
        ObjectMap<String, Object> fontMap = new ObjectMap<>();
        fontMap.put("small-font", smallDistanceField);
        fontMap.put("default-font", defaultDistanceField);
        fontMap.put("big-font", bigDistanceField);
        fontMap.put("default-mono-font", defaultMono);

        SkinLoader.SkinParameter skinParameter = new SkinLoader.SkinParameter(fontMap);

        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(Skin.class, new H2DSkinLoader(assetManager.getFileHandleResolver()));
        assetManager.load("style/uiskin.json", Skin.class, skinParameter);

        assetManager.finishLoading();
        Skin skin = assetManager.get("style/uiskin.json");

        VisUI.load(skin);
        VisUI.setDefaultTitleAlign(Align.center);

        // TODO: substitute this with "NO IMAGE" icon
        Pixmap pixmap = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1, 1, 1, 0.4f));
        pixmap.fill();
        defaultRegion = new TextureRegion(new Texture(pixmap));

        fontPacker = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.SkylineStrategy());
        fontPacker.setTransparentColor(Color.WHITE);
        fontPacker.getTransparentColor().a = 0;
    }

    @Override
    public TextureRegion getTextureRegion(String name, int index) {
        for (TextureAtlas atlas : currentProjectAtlas.values()) {
            TextureRegion region = atlas.findRegion(name, index);
            if (region != null)
                return region;
        }
        return defaultRegion;
    }

    @Override
    public TextureRegion getAtlasImagesTextureRegion(String atlasName, String name, int index) {
        if (!atlasName.isEmpty() && atlasImageAtlases.containsKey(atlasName)) {
            TextureAtlas textureAtlas = atlasImageAtlases.get(atlasName);
            return textureAtlas.findRegion(name, index);
        } else {
            for (Map.Entry<String, TextureAtlas> entry : atlasImageAtlases.entrySet()) {
                TextureAtlas.AtlasRegion region = entry.getValue().findRegion(name, index);
                if (region != null) return region;
            }
        }
        return null;
    }

    @Override
    public TextureAtlas getTextureAtlas(String atlasName) {
        return currentProjectAtlas.get(atlasName);
    }

    public TextureAtlas getAtlasImagesAtlas(String atlasName) {
        return atlasImageAtlases.get(atlasName);
    }

    @Override
    public ParticleEffect getParticleEffect(String name) {
        return new ParticleEffect(particleEffects.get(name));
    }

    @Override
    public FileHandle getTalosVFX(String name) {
        return talosVFXsFiles.get(name);
    }

    /**
     * Sets working resolution, please set before doing any loading
     *
     * @param resolution String resolution name, default is "orig" later use resolution names created in editor
     */
    public void setWorkingResolution(String resolution) {
        ResolutionEntryVO resolutionObject = getProjectVO().getResolution("resolutionName");
        if (resolutionObject != null) {
            packResolutionName = resolution;
        }
    }


    @Override
    public FileHandle getSkeletonJSON(String animationName) {
        SpineAnimData animData = spineAnimAtlases.get(animationName);
        return animData.jsonFile;
    }

    @Override
    public FileHandle getSpriterSCML(String name) {
        SpriterAnimData animData = spriterAnimAtlases.get(name);
        return animData.scmlFile;
    }

    @Override
    public Array<FileHandle> getSpriterExtraSCML(String name) {
        SpriterAnimData animData = spriterAnimAtlases.get(name);
        return animData.extraScmlFiles;
    }

    @Override
    public TextureAtlas getSpriterAtlas(String name) {
        SpriterAnimData animData = spriterAnimAtlases.get(name);
        return animData.atlas;
    }

    @Override
    public Array<TextureAtlas.AtlasRegion> getSpriteAnimation(String animationName) {
        return spriteAnimAtlases.get(animationName);
    }

    @Override
    public BitmapFont getBitmapFont(String fontName, int fontSize) {
        FontSizePair pair = new FontSizePair(fontName, fontSize);
        return bitmapFonts.get(pair);
    }

    @Override
    public boolean hasTextureRegion(String regionName) {
        for (TextureAtlas atlas : currentProjectAtlas.values()) {
            if (atlas.findRegion(regionName) != null)
                return true;
        }
        return false;
    }

    @Override
    public ProjectInfoVO getProjectVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO();
    }

    @Override
    public SceneVO getSceneVO(String name) {
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        // TODO: this should be cached
        FileHandle file = Gdx.files.internal(sceneDataManager.getCurrProjectScenePathByName(name));
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        return json.fromJson(SceneVO.class, file.readString("utf-8"));
    }

    public void loadCurrentProjectData(String projectPath, String curResolution) {
        packResolutionName = curResolution;
        loadCurrentProjectAssets(projectPath + "/assets/" + curResolution + "/pack");
        loadCurrentProjectParticles(projectPath + "/assets/orig/particles");
        loadCurrentProjectTalosVFXs(projectPath + "/assets/orig/talos-vfx");
        loadCurrentProjectSpineAnimations(projectPath + "/assets/", curResolution);
        loadCurrentProjectSpriteAnimations(projectPath + "/assets/", curResolution);
        loadCurrentProjectSpriterAnimations(projectPath + "/assets/", curResolution);
        loadCurrentProjectAtlasImages(projectPath + "/assets/", curResolution);
        loadCurrentProjectBitmapFonts(projectPath, curResolution);
        loadCurrentProjectShaders(projectPath + "/assets/shaders/");
    }

    private void loadCurrentProjectParticles(String path) {
        particleEffects.clear();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store")) continue;

            ParticleEffect particleEffect = new ParticleEffect();
            particleEffect.loadEmitters(Gdx.files.internal(file.getAbsolutePath()));
            for (TextureAtlas atlas : currentProjectAtlas.values()) {
                try {
                    particleEffect.loadEmitterImages(atlas, "");
                    break;
                } catch (Exception ignore) {
                }
            }
            particleEffects.put(filename, particleEffect);
        }
    }

    private void loadCurrentProjectTalosVFXs(String path) {
        talosVFXs.clear();
        talosResPath = path;
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".DS_Store") || filename.endsWith("shdr") || filename.endsWith(".fga"))
                continue;

            ResourceRetrieverAssetProvider assetProvider = new ResourceRetrieverAssetProvider(this);
            assetProvider.setAssetHandler(ShaderDescriptor.class, this::findShaderDescriptorOnLoad);
            assetProvider.setAssetHandler(VectorField.class, this::findVectorFieldDescriptorOnLoad);
            ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor();
            effectDescriptor.setAssetProvider(assetProvider);
            talosVFXsFiles.put(filename, Gdx.files.internal(file.getAbsolutePath()));
            effectDescriptor.load(Gdx.files.internal(file.getAbsolutePath()));
            talosVFXs.put(filename, effectDescriptor);
        }
    }

    private ObjectMap<String, ShaderDescriptor> shaderDescriptorObjectMap = new ObjectMap<>();
    private String talosResPath;

    private ShaderDescriptor findShaderDescriptorOnLoad(String assetName) {
        ShaderDescriptor asset = shaderDescriptorObjectMap.get(assetName);
        if (asset == null) {
            //Look in all paths, and hopefully load the requested asset, or fail (crash)
            final FileHandle file = new FileHandle(talosResPath + File.separator + assetName);

            asset = new ShaderDescriptor();
            if (file.exists()) {
                asset.setData(file.readString());
            }
        }
        return asset;
    }

    private ObjectMap<String, VectorField> vectorFieldDescriptorObjectMap = new ObjectMap<>();

    private VectorField findVectorFieldDescriptorOnLoad(String assetName) {
        VectorField asset = vectorFieldDescriptorObjectMap.get(assetName);
        if (asset == null) {
            final FileHandle file = new FileHandle(talosResPath + File.separator + assetName + ".fga");

            if (file.exists()) {
                asset = new VectorField(file);
            } else {
                asset = new VectorField();
            }
        }
        return asset;
    }

    private void loadCurrentProjectSpineAnimations(String path, String curResolution) {
        spineAnimAtlases.clear();
        FileHandle sourceDir = new FileHandle(path + "orig/spine-animations");
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());
                FileHandle animJsonFile = Gdx.files.internal(entry.file().getAbsolutePath() + File.separator + animName + ".json");
                SpineAnimData data = new SpineAnimData();
                data.jsonFile = animJsonFile;
                data.animName = animName;
                spineAnimAtlases.put(animName, data);
            }
        }

    }

    private void loadCurrentProjectSpriteAnimations(String path, String curResolution) {
        spriteAnimAtlases.clear();
        FileHandle sourceDir = new FileHandle(path + "orig" + File.separator + "sprite-animations");
        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = FilenameUtils.removeExtension(entry.file().getName());
                Array<TextureAtlas.AtlasRegion> regions = null;
                for (TextureAtlas atlas : currentProjectAtlas.values()) {
                    regions = atlas.findRegions(animName);
                    if (regions.size > 0)
                        break;
                }
                if (regions != null)
                    spriteAnimAtlases.put(animName, regions);
            }
        }
    }

    private void loadCurrentProjectSpriterAnimations(String path, String curResolution) {
        spriterAnimAtlases.clear();
        FileHandle sourceDir = new FileHandle(path + "orig" + "/spriter-animations");
        FileHandle extraConfigFile = new FileHandle(sourceDir.path() + File.separator + "anim-relation.dt");
        SpriterRelationVO rVO = null;
        if (extraConfigFile.exists()) {
            try {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                String jsonString = FileUtils.readFileToString(extraConfigFile.file(), "utf-8");
                rVO = json.fromJson(SpriterRelationVO.class, jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (FileHandle entry : sourceDir.list()) {
            if (entry.file().isDirectory()) {
                String animName = entry.file().getName();
                if ("extra".equals(animName)) continue;
                TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path + curResolution + "/spriter-animations/"
                        + animName + File.separator + animName + ".atlas"));
                FileHandle scmlFile = new FileHandle(path + "orig" + "/spriter-animations/" + animName + "/" + animName + ".scml");
                SpriterAnimData data = new SpriterAnimData();
                data.scmlFile = scmlFile;
                data.atlas = atlas;
                data.animName = animName;
                if (rVO != null && rVO.animations != null && rVO.animations.containsKey(animName)) {
                    SpriterVO vo = rVO.animations.get(animName);
                    if (vo != null) {
                        for (String s : vo.animations) {
                            FileHandle f = new FileHandle(path + "orig" + "/spriter-animations/extra" + "/" + s + ".scml");
                            data.extraScmlFiles.add(f);
                        }
                    }
                }
                spriterAnimAtlases.put(animName, data);
            }
        }
    }

    private void loadCurrentProjectAtlasImages(String path, String curResolution) {
        atlasImageAtlases.clear();
        FileHandle sourceDir = new FileHandle(path + "orig" + "/atlas-images");
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            if (file.isFile() && file.getName().toLowerCase().endsWith(".atlas")) {
                String fileName = file.getName();
                TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(path + curResolution + "/atlas-images/" + fileName));
                atlasImageAtlases.put(fileName.replace(".atlas", ""), atlas);
            }
        }
    }

    public void loadCurrentProjectAssets(String packFolderPath) {
        FileHandle folder = new FileHandle(packFolderPath);
        for (FileHandle file : folder.list()) {
            if (file.extension().equals("atlas")) {
                String name = file.nameWithoutExtension().equals("pack") ? "main" : file.nameWithoutExtension();
                currentProjectAtlas.put(name, new TextureAtlas(file));
            }
        }
    }

    public ArrayList<FontSizePair> getProjectRequiredFontsList() {
        HashSet<FontSizePair> fontsToLoad = new HashSet<>();

        for (int i = 0; i < getProjectVO().scenes.size(); i++) {
            SceneVO scene = getSceneVO(getProjectVO().scenes.get(i).sceneName);
            CompositeVO composite = scene.composite;
            if (composite == null) {
                continue;
            }
            FontSizePair[] fonts = composite.getRecursiveFontList();
            for (CompositeItemVO library : getProjectVO().libraryItems.values()) {
                FontSizePair[] libFonts = library.composite.getRecursiveFontList();
                Collections.addAll(fontsToLoad, libFonts);
            }
            Collections.addAll(fontsToLoad, fonts);
        }

        return new ArrayList<>(fontsToLoad);
    }

    public void loadCurrentProjectBitmapFonts(String path, String curResolution) {
        bitmapFonts.clear();

        ArrayList<FontSizePair> requiredFonts = getProjectRequiredFontsList();
        for (int i = 0; i < requiredFonts.size(); i++) {
            FontSizePair pair = requiredFonts.get(i);
            if ("Internal".equals(pair.fontName)) {
                loadInternalFont(pair.fontSize);
                continue;
            }

            try {
                FileHandle txtFile = Gdx.files.internal("freetypefonts/gbk-chars.txt");
                String charsTxt = FileUtils.readFileToString(txtFile.file(), "utf-8");

                FileHandle fontFile = getTTFSafely(pair.fontName);
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = Math.round(pair.fontSize * resolutionManager.getCurrentMul());
                parameter.packer = fontPacker;
                parameter.characters = charsTxt;
                parameter.gamma = 4.0f;
                parameter.renderCount = 3;
                parameter.minFilter = Texture.TextureFilter.Linear;
                parameter.magFilter = Texture.TextureFilter.Linear;
                BitmapFont font = generator.generateFont(parameter);
                font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                font.setUseIntegerPositions(false);
                font.setFixedWidthGlyphs(parameter.characters);
                generator.dispose();

                bitmapFonts.put(pair, font);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadInternalFont(int size) {
        ShadedDistanceFieldFont font = new ShadedDistanceFieldFont(Gdx.files.internal(ProjectManager.DEFAULT_FONT));
        font.setDistanceFieldSmoothing(1.5f);
        font.getData().setScale(size / font.getCapHeight());
        bitmapFonts.put(new FontSizePair("Internal", size), font);
    }

    private void loadCurrentProjectShaders(String path) {
        Iterator<Entry<String, ShaderProgram>> it = shaderPrograms.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, ShaderProgram> pair = it.next();
            pair.getValue().dispose();
            it.remove();
        }
        shaderPrograms.clear();
        FileHandle sourceDir = new FileHandle(path);
        for (FileHandle entry : sourceDir.list()) {
            File file = entry.file();
            String filename = file.getName().replace(".vert", "").replace(".frag", "");
            if (file.isDirectory() || filename.endsWith(".DS_Store") || shaderPrograms.containsKey(filename))
                continue;
            // check if pair exists.
            if (Gdx.files.internal(path + filename + ".vert").exists() && Gdx.files.internal(path + filename + ".frag").exists()) {
                ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal(path + filename + ".vert"), Gdx.files.internal(path + filename + ".frag"));
                if (!shaderProgram.isCompiled()) {
                    System.out.println("Error compiling shader: " + shaderProgram.getLog());
                }
                shaderPrograms.put(filename, shaderProgram);
            }
        }

    }

    public void reloadShader(String shaderName) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String shader = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + shaderName;

        if (Gdx.files.internal(shader + ".vert").exists() && Gdx.files.internal(shader + ".frag").exists()) {
            ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal(shader + ".vert"), Gdx.files.internal(shader + ".frag"));
            if (shaderProgram.isCompiled()) {
                shaderPrograms.remove(shaderName);
                shaderPrograms.put(shaderName, shaderProgram);
            } else {
                System.out.println("Error compiling shader: " + shaderProgram.getLog());
            }
        }
    }

    public FileHandle getTTFSafely(String fontName) throws IOException {
        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String name = URLEncoder.encode(new String(fontName.getBytes(), "utf-8"), "utf-8");
        String expectedPath = projectManager.getFreeTypeFontPath() + File.separator + name + ".ttf";
        FileHandle expectedFile = Gdx.files.internal(expectedPath);
        if (!expectedFile.exists()) {
            // let's check if system fonts fot it
            HashMap<String, String> fonts = fontManager.getFontsMap();
            if (fonts.containsKey(fontName)) {
                File source = new File(fonts.get(fontName));
                FileUtils.copyFile(source, expectedFile.file());
                expectedFile = Gdx.files.internal(expectedPath);
            } else {
                throw new FileNotFoundException();
            }
        }

        return expectedFile;
    }

    public void addBitmapFont(String name, int size, BitmapFont font) {
        bitmapFonts.put(new FontSizePair(name, size), font);
    }

    public void flushAllUnusedFonts() {
        //List of fonts that are required to be in memory
        ArrayList<FontSizePair> requiredFonts = getProjectRequiredFontsList();
        ArrayList<FontSizePair> fontsInMemory = new ArrayList<>(bitmapFonts.keySet());

        for (FontSizePair font : fontsInMemory) {
            if (!requiredFonts.contains(font)) {
                bitmapFonts.remove(font);
            }
        }
    }

    public boolean isFontLoaded(String shortName, int fontSize) {
        FontSizePair key = new FontSizePair(shortName, fontSize);
        return bitmapFonts.containsKey(key);
    }

    public void prepareEmbeddingFont(String fontfamily, int fontSize) {
        flushAllUnusedFonts();

        if (isFontLoaded(fontfamily, fontSize)) {
            return;
        }

        if ("Internal".equals(fontfamily)) {
            loadInternalFont(fontSize);
            return;
        }

        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);

        FileHandle txtFile = Gdx.files.internal("freetypefonts/gbk-chars.txt");
        String charsTxt = "";
        try {
            charsTxt = FileUtils.readFileToString(txtFile.file(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = Math.round(fontSize * resolutionManager.getCurrentMul());
        parameter.packer = fontPacker;
        parameter.characters = charsTxt;
        parameter.gamma = 4.0f;
        parameter.renderCount = 3;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        FileHandle fileHandle = fontManager.getTTFByName(fontfamily);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fileHandle);
        BitmapFont font = generator.generateFont(parameter);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        font.setFixedWidthGlyphs(parameter.characters);
        generator.dispose();

        addBitmapFont(fontfamily, fontSize, font);
    }

    public HashMap<String, SpineAnimData> getProjectSpineAnimationsList() {
        return spineAnimAtlases;
    }

    public HashMap<String, Array<TextureAtlas.AtlasRegion>> getProjectSpriteAnimationsList() {
        return spriteAnimAtlases;
    }

    public HashMap<String, SpriterAnimData> getProjectSpriterAnimationsList() {
        return spriterAnimAtlases;
    }

    public HashMap<String, TextureAtlas> getProjectAtlasImagesList() {
        return atlasImageAtlases;
    }

    public HashMap<String, ParticleEffect> getProjectParticleList() {
        return particleEffects;
    }

    public HashMap<String, ParticleEffectDescriptor> getProjectTalosList() {
        return talosVFXs;
    }

    @Override
    public ResolutionEntryVO getLoadedResolution() {
        if (packResolutionName.equals("orig")) {
            return getProjectVO().originalResolution;
        }
        return getProjectVO().getResolution(packResolutionName);
    }

    @Override
    public ShaderProgram getShaderProgram(String shaderName) {
        return shaderPrograms.get(shaderName);
    }

    public HashMap<String, ShaderProgram> getShaders() {
        return shaderPrograms;
    }
}
