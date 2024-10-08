package games.rednblack.editor.view.stage.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.EntityBounds;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class SandboxInputAdapter implements InputProcessor {

	private HyperLap2DFacade facade;
	private Entity rootEntity;
	private InputListenerComponent inpputListenerComponent;
	private Entity target;
	private Vector2 hitTargetLocalCoordinates = new Vector2();
	private Sandbox sandbox;
	private final EntityBounds tempEntityBounds = new EntityBounds();

	public SandboxInputAdapter() {
		facade = HyperLap2DFacade.getInstance();
		SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);
		sandbox = sandboxMediator.getViewComponent();
	}

	@Override
	public boolean keyDown(int keycode) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyDown(null, keycode);
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyUp(null, keycode);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyTyped(null, character);
		}

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		//Handle Global Listeners than others
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == null){
			return false;
		}

		Viewport viewPort = ComponentRetriever.get(rootEntity, ViewPortComponent.class).viewPort;
		if (screenX < viewPort.getScreenX() || screenX >= viewPort.getScreenX() + viewPort.getScreenWidth()) return false;
		if (Gdx.graphics.getHeight() - screenY < viewPort.getScreenY()
			|| Gdx.graphics.getHeight() - screenY >= viewPort.getScreenY() + viewPort.getScreenHeight()) return false;

		hitTargetLocalCoordinates.set(screenX, screenY);
		screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

		target = hit(rootEntity, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y);
		if(target == null){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchDown(null, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button);
			}

			return false;
		}

		hitTargetLocalCoordinates.set(screenX, screenY);
		screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

		inpputListenerComponent = target.getComponent(InputListenerComponent.class);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
		TransformMathUtils.sceneToLocalCoordinates(target, hitTargetLocalCoordinates);
		for (int j = 0, s = listeners.size; j < s; j++) {
			if (listeners.get(j).touchDown(target, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == null){
			return false;
		}

		if(target == null){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchUp(null, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button);
			}

			return false;
		}
		inpputListenerComponent = target.getComponent(InputListenerComponent.class);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchUp(target, screenX, screenY, pointer, button);
		}
		target = null;
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == null){
			return false;
		}

		if(target == null){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchDragged(null, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer);
			}
			return false;
		}

		inpputListenerComponent = target.getComponent(InputListenerComponent.class);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchDragged(target, screenX, screenY, pointer);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
//		entities = engine.getEntitiesFor(root);
//		for (int i = 0, n = entities.size(); i < n; i++){
//			Entity entity = entities.get(i);
//			inpputListenerComponent = ComponentRetriever.get(target, InputListenerComponent.class);
//			Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
//			for (int j = 0, s = listeners.size; j < s; j++){
//				if (listeners.get(j).mouseMoved(entity, screenX, screenY)){
//					return true;
//				}
//			}
//			
//		}
		return false;
	}

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

	@Override
	public boolean scrolled(float amountX, float amountY) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == null){
			return false;
		}

		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).scrolled(null, amountX, amountY);
		}

		//TODO scroll for other Entities don't know how deep tis should go all entities or only hit tested
//		inpputListenerComponent = ComponentRetriever.get(entity, InputListenerComponent.class);
//		if(inpputListenerComponent == null) continue;
//		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
//		for (int j = 0, s = listeners.size; j < s; j++){				
//			if (listeners.get(j).scrolled(entity,amount)){
//				return true;
//			}
//		}


		return false;
	}

	Vector2 tmpVector2 = new Vector2();

	public Entity hit(Entity root, float x, float y){
		Vector2 localCoordinates  = tmpVector2.set(x, y);

		TransformMathUtils.parentToLocalCoordinates(root, localCoordinates);

		NodeComponent nodeComponent = ComponentRetriever.get(root, NodeComponent.class);
		SnapshotArray<Entity> childrenEntities = nodeComponent.children;
		int n = childrenEntities.size-1;
		for (int i = n; i >= 0; i--){
			Entity childEntity = childrenEntities.get(i);

			// get layer locked or not
			LayerItemVO layerItemVO = EntityUtils.getEntityLayer(childEntity);
			if(layerItemVO != null && (layerItemVO.isLocked || !layerItemVO.isVisible)) {
				continue;
			}

			if (Intersector.isPointInPolygon(tempEntityBounds.getBoundPointsList(childEntity), localCoordinates)) {
				return childEntity;
			}
		}
		return null;
	}

	public Vector2 screenToSceneCoordinates (Entity root, Vector2 screenCoords) {
		ViewPortComponent viewPortComponent = ComponentRetriever.get(root, ViewPortComponent.class);
		viewPortComponent.viewPort.unproject(screenCoords);
		return screenCoords;
	}
}
