package games.rednblack.editor.plugin.tiled.view.tabs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.TiledPanel;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.plugin.tiled.manager.ResourcesManager;
import games.rednblack.editor.plugin.tiled.view.SpineDrawable;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.vo.ResourceExtraData;

/**
 * Created by mariam on 2/11/16.
 */
public class GridTilesTab extends DefaultTab {

    public static boolean isDrop;

    private int tilesCount = 19;
    private Array<VisImageButton> tiles;
    private Array<TileVO> savedTiles;
    private int tileIndex;
    private VisScrollPane pane;
    private boolean isBottomEdge;

    private TiledPlugin tiledPlugin;
    private ResourcesManager resourcesManager;

    public GridTilesTab(TiledPanel panel, int tabIndex) {
        super(panel, "Tiles", tabIndex);

        tiledPlugin = panel.tiledPlugin;
        resourcesManager = tiledPlugin.pluginRM;
        tiles = new Array<>();
        savedTiles = tiledPlugin.dataToSave.getTiles();
        tileIndex = savedTiles.size;
    }

    @Override
    public void initView() {
        if (isDrop = savedTiles.size == 0) {
            VisImageButton.VisImageButtonStyle dropBoxStyle = new VisImageButton.VisImageButtonStyle();
            dropBoxStyle.up = new TextureRegionDrawable(resourcesManager.getTextureRegion("tiles-drop-here-normal", "", -1, -1));
            dropBoxStyle.imageOver = new TextureRegionDrawable(resourcesManager.getTextureRegion("tiles-drop-here-over", "", -1, -1));
            VisImageButton dropRegion = new VisImageButton(dropBoxStyle);
            content.clear();
            content.add(dropRegion).center().padRight(6).padBottom(6).padTop(10).row();
            content.add(new VisLabel("Drop an image from resources box")).expandX().center().padBottom(5);
            content.pack();
        } else {
            if (tileIndex > tilesCount) {
                tilesCount = tileIndex;
            }
            initTiles();
        }
    }

    public void addTile(String tileName, String atlasName, int regionIndex, int type) {
        if (pane != null) isBottomEdge = pane.isBottomEdge();
        if (tileIndex == 0) {
            setGridSizeToFirstTileSize(tileName, atlasName, regionIndex, type);
            isDrop = false;
            panel.reInitTabTable();
        }
        initTiles(tileName, atlasName, regionIndex, type);
        panel.pack();
        scrollTiles();
        tiles.get(tileIndex).setChecked(true);
        tiledPlugin.facade.sendNotification(TiledPlugin.TILE_SELECTED, new TileVO(tileName));
        tileIndex++;
    }

    public void selectTile(TileVO tileVO) {
        tiledPlugin.setSelectedTileVO(tileVO);
    }

    public void removeTile() {
        if (pane != null) isBottomEdge = pane.isBottomEdge();
        tileIndex = --tileIndex < 0 ? 0 : tileIndex;
        tilesCount = --tilesCount < 19 ? 19 : tilesCount;
        tiles.clear();
        initView();
    }

    public void scrollTiles() {
        if (savedTiles.size + 1 >= tilesCount) {
            pane.layout();
            pane.setSmoothScrolling(!isBottomEdge);
            pane.setScrollY(100);
        }
    }

    private void setGridSizeToFirstTileSize(String tileName, String atlasName, int regionIndex, int type) {
        float width = 0;
        float height = 0;
        if (type == EntityFactory.SPINE_TYPE) {
            SpineDrawable spineDrawable = tiledPlugin.pluginRM.getSpineDrawable(tileName);
            width = spineDrawable.width;
            height = spineDrawable.height;
        } else {
            TextureRegion r = tiledPlugin.pluginRM.getTextureRegion(tileName, atlasName, regionIndex, type);
            width = r.getRegionWidth();
            height = r.getRegionHeight();
        }
        float gridWidth = width / tiledPlugin.getPixelToWorld();
        float gridHeight = height / tiledPlugin.getPixelToWorld();
        tiledPlugin.dataToSave.setGrid(gridWidth, gridHeight);
        tiledPlugin.facade.sendNotification(TiledPlugin.GRID_CHANGED);
    }

    private void initTiles(String tileName, String atlasName, int regionIndex, int type) {
        content.clear();
        tiles.clear();

        VisTable listTable = new VisTable();
        pane = StandardWidgetsFactory.createScrollPane(listTable);
        pane.setScrollingDisabled(true, false);
        content.add(pane)
                .padTop(10);
        listTable.top();

        if (tileIndex >= tilesCount && !tileName.equals("")) {
            tilesCount = tileIndex + 1;
        }

        for (int i = 0; i < tilesCount + 1; i++) {
            VisImageButton ct;
            VisImageButton.VisImageButtonStyle imageBoxStyle = new VisImageButton.VisImageButtonStyle();
            NinePatchDrawable inactive = new NinePatchDrawable(new NinePatch(resourcesManager.getPluginNinePatch("image-Box-inactive")));
            NinePatchDrawable active = new NinePatchDrawable(new NinePatch(resourcesManager.getPluginNinePatch("image-Box-active")));
            imageBoxStyle.up = inactive;
            imageBoxStyle.down = active;
            imageBoxStyle.checked = active;
            imageBoxStyle.over = active;
            Drawable tileDrawable = null;
            if (i < savedTiles.size) {
                TileVO tileVO = savedTiles.get(i);
                int t = tileVO.entityType;
                if (t == EntityFactory.SPINE_TYPE) {
                    tileDrawable = resourcesManager.getSpineDrawable(tileVO.regionName);
                } else {
                    tileDrawable = new TextureRegionDrawable(resourcesManager.getTextureRegion(tileVO.regionName, tileVO.atlasName, tileVO.regionIndex, t));
                }
            } else if (!tileName.equals("")) {
                if (i == tileIndex) {
                    if (type == EntityFactory.SPINE_TYPE) {
                        tileDrawable = resourcesManager.getSpineDrawable(tileName);
                    } else {
                        tileDrawable = new TextureRegionDrawable(resourcesManager.getTextureRegion(tileName, atlasName, regionIndex, type));
                    }
                }
            }
            imageBoxStyle.imageUp = tileDrawable;
            imageBoxStyle.imageDown = tileDrawable;
            imageBoxStyle.imageChecked = tileDrawable;
            imageBoxStyle.imageOver = tileDrawable;
            ct = new VisImageButton(imageBoxStyle);

            int index = i;
            ct.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (index >= savedTiles.size) return true;

                    for (VisImageButton tile : tiles) {
                        if (tile.isChecked()) {
                            tile.setChecked(false);
                        }
                    }

                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchUp(event, x, y, pointer, button);
                    if (button == Input.Buttons.RIGHT && index < savedTiles.size) {
                        tiledPlugin.facade.sendNotification(TiledPlugin.OPEN_DROP_DOWN, savedTiles.get(index));
                        return;
                    }

                    if (index >= savedTiles.size) {
                        tiles.get(index).setChecked(false);
                        return;
                    }

                    tiledPlugin.facade.sendNotification(TiledPlugin.TILE_SELECTED, savedTiles.get(index));
                }
            });
            listTable.add(ct).width(40).height(40).pad(3);
            if ((i + 1) % 4 == 0) {
                listTable.row();
            }
            tiles.add(ct);
        }

        content.pack();
    }

    private void initTiles() {
        initTiles("", "", -1, -1);
    }
}
