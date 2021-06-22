package games.rednblack.editor.plugin.tiled.save;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import games.rednblack.editor.plugin.tiled.data.ParameterVO;
import games.rednblack.editor.plugin.tiled.data.TileVO;

import java.util.stream.StreamSupport;

/**
 * Created by mariam on 3/23/16.
 */
public class DataToSave {

    private Array<TileVO> tiles;
    private ParameterVO parameterVO;

    public DataToSave() {
        tiles = new Array<>();
        parameterVO = new ParameterVO();
    }

    public void addTile(String tileDrawableName, String atlasName, int regionIndex, int type) {
        TileVO newTile = new TileVO(tileDrawableName);
        newTile.entityType = type;
        newTile.atlasName = atlasName;
        newTile.regionIndex = regionIndex;
        if (!tiles.contains(newTile, false)) {
            tiles.add(newTile);
        }
    }

    public void removeTile(String tileDrawableName, int reginIndex) {
        tiles.forEach(tile -> {
            if (tile.regionName.equals(tileDrawableName) && tile.regionIndex == reginIndex) {
                tiles.removeValue(tile, false);
            }
        });
    }

    public void setTileGridOffset(TileVO tileVO) {
        StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(tileVO.regionName) && tile.regionIndex == tileVO.regionIndex)
                .findFirst()
                .ifPresent(t -> t.gridOffset = tileVO.gridOffset);
    }

    public Vector2 getTileGridOffset(String regionName, int reginIndex) {
        return StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(regionName) && tile.regionIndex == reginIndex)
                .findFirst()
                .get()
                .gridOffset;
    }

    public TileVO getTile(String regionName, int reginIndex) {
        return StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(regionName) && tile.regionIndex == reginIndex)
                .findFirst()
                .get();
    }

    public Array<TileVO> getTiles() {
        return tiles;
    }

    public boolean containsTile(String regionName, int reginIndex) {
        return StreamSupport.stream(tiles.spliterator(), false).anyMatch(tile ->
                tile.regionName.equals(regionName) && tile.regionIndex == reginIndex);
    }

    public ParameterVO getParameterVO() {
        return parameterVO;
    }

    public void setParameterVO(ParameterVO parameterVO) {
        this.parameterVO = parameterVO;
    }

    public void setGrid(float gridWidth, float gridHeight) {
        parameterVO.gridWidth = gridWidth;
        parameterVO.gridHeight = gridHeight;
    }
}
