package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.util.HashMap;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class AppendSpriterAnimationPanel extends UIDraggablePanel {
    public static final String CLASS_NAME = "games.rednblack.editor.view.ui.panel.AppendSpriterAnimationPanel";

    public static final String BROWSE_BTN_CLICKED = CLASS_NAME + ".BROWSE_BTN_CLICKED";
    public static final String CHOICE_BTN_CLICKED = CLASS_NAME + ".CHOICE_BTN_CLICKED";

    public static final String IMPORT_FAILED = CLASS_NAME + ".IMPORT_FAILED";

    private HyperLap2DFacade facade;

    private VisTable mainTable;
    private Image dropRegion;
    private VisLabel errorLabel;
    private VisTextButton choiceBtn;
    private VisSelectBox<String> animationsSb;

    private VisProgressBar progressBar;

    private HashMap<Integer, String> typeNames = new HashMap<>();

    public AppendSpriterAnimationPanel() {
        super("Import Animation");
        setMovable(true);
        setModal(false);
        addCloseButton();
        setStyle(VisUI.getSkin().get("box", WindowStyle.class));
        getTitleLabel().setAlignment(Align.left);

        setWidth(250);
        setHeight(100);

        facade = HyperLap2DFacade.getInstance();

        fillTypeNames();

        mainTable = new VisTable();

        add(mainTable).fill().expand();
        row();

        setDroppingView();

        errorLabel = new VisLabel("File you selected was too sexy to import");
        errorLabel.setColor(Color.RED);
        errorLabel.setWidth(250);
        errorLabel.setWrap(true);
        errorLabel.getColor().a = 0;
        errorLabel.setTouchable(Touchable.disabled);

        mainTable.add(errorLabel).width(250).padTop(6);
        mainTable.row().padTop(5);
    }

    private void fillTypeNames() {
        typeNames.clear();

        typeNames.put(ImportUtils.TYPE_SPRITER_ANIMATION, "Spriter Animation");
    }

    public Image getDropRegion() {
        return dropRegion;
    }

    public boolean checkDropRegionHit(Vector2 mousePos) {
        Vector2 pos = Sandbox.getInstance().getUIStage().getViewport().unproject(mousePos);
        pos = dropRegion.stageToLocalCoordinates(pos);
        if (dropRegion.hit(pos.x, pos.y, true) != null) {
            return true;
        }

        dropRegion.getColor().a = 0.3f;

        return false;
    }

    public void dragOver() {
        dropRegion.getColor().a = 0.5f;
    }

    public void dragExit() {
        dropRegion.getColor().a = 0.3f;
    }

    public void setDroppingView() {
        mainTable.clear();

        VisLabel helpLbl = new VisLabel("Choose one already exists, or import new spriter animation(*.scml file)");
        helpLbl.setWidth(250);
        helpLbl.setWrap(true);
        mainTable.add(helpLbl).width(250);
        mainTable.row().padBottom(5);

        dropRegion = new Image(VisUI.getSkin().getDrawable("dropHere"));
        dropRegion.setTouchable(Touchable.disabled);
        mainTable.add(dropRegion).padRight(10).padBottom(6).padTop(10);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("or browse files on file system"));
        mainTable.row().padTop(10);

        VisTextButton showFileSelectBtn = new VisTextButton("Browse");
        mainTable.add(showFileSelectBtn).width(88).padRight(5);
        mainTable.row().padTop(5);

        animationsSb = StandardWidgetsFactory.createSelectBox(String.class);
        Table table = new Table();
        choiceBtn = new VisTextButton("Choice");
        table.add(animationsSb).fill().expandX().padRight(5);
        table.add(choiceBtn).width(70);
        mainTable.add(table).width(240).padRight(10);
        mainTable.row().padTop(5).padBottom(20);

        initDropListeners(showFileSelectBtn);

        dragExit();
        pack();
    }

    @Override
    public VisDialog show(Stage stage) {
        dropRegion.setTouchable(Touchable.enabled);
        return super.show(stage);
    }

    @Override
    public void cancel() {
        dropRegion.setTouchable(Touchable.disabled);
        super.cancel();
    }

    public void setImportingView(int type, int count) {
        mainTable.clear();

        errorLabel.getColor().a = 0;
        errorLabel.clearActions();

        String typeText = typeNames.get(type);
        if (count > 1) typeText += " (" + count + ")";

        mainTable.add(new VisLabel("Currently importing: " + typeText)).left();
        mainTable.row().padBottom(5);

        progressBar = new VisProgressBar(0, 100, 1, false);
        mainTable.add(progressBar).fillX().padTop(5).width(250);
        mainTable.row().padBottom(5);

        pack();
    }

    private void initDropListeners(VisTextButton browseBtn) {
        browseBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(BROWSE_BTN_CLICKED);
            }
        });
        choiceBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String selected = animationsSb.getSelected();
                if (!selected.isEmpty()) facade.sendNotification(CHOICE_BTN_CLICKED, selected);
            }
        });
    }

    public void setAnimations(Array<String> names) {
        if (animationsSb != null) animationsSb.setItems(names);
    }

    public void showError(int type) {
        String text = "";
        if (type == ImportUtils.TYPE_UNSUPPORTED || type == ImportUtils.TYPE_UNKNOWN) {
            text = "Unsupported file type/types/size";
        }
        if (type == ImportUtils.TYPE_MIXED) {
            text = "Multiple import types, please use one";
        }
        switch (type) {
            case ImportUtils.TYPE_UNSUPPORTED:
            case ImportUtils.TYPE_UNKNOWN:
                text = "Unsupported file type/types/size";
                break;
            case ImportUtils.TYPE_MIXED:
                text = "Multiple import types, please use one";
                break;
            case ImportUtils.TYPE_FAILED:
                text = "Import has failed";
                break;
        }

        errorLabel.setText(text);

        errorLabel.addAction(Actions.fadeIn(0.3f));
        dragExit();
    }

    public VisProgressBar getProgressBar() {
        return progressBar;
    }
}
