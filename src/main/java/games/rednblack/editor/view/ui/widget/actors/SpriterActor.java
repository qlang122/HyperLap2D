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

package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

import games.rednblack.editor.renderer.resources.IResourceRetriever;
import me.winter.gdx.animation.Animation;
import me.winter.gdx.animation.Entity;
import me.winter.gdx.animation.scml.SCMLLoader;
import me.winter.gdx.animation.scml.SCMLProject;

/**
 * @author Created by qlang on 5/28/2021.
 */
public class SpriterActor extends Actor {
    private String animationName = "";

    public Entity entity;
    public Animation animation;

    private ArrayList<Animation> animations = new ArrayList<>();
    private ArrayList<Entity> entities = new ArrayList<>();

    private int currentEntityIndex = 0;
    private int currentAnimationIndex;
    private Rectangle rectangle = new Rectangle();

    private IResourceRetriever irr;

    public SpriterActor(String animationName, IResourceRetriever irr) {
        this.irr = irr;
        this.animationName = animationName;

        initSpriterAnimation();
    }

    private void initSpriterAnimation() {
        FileHandle scmlFile = irr.getSpriterSCML(animationName);
        TextureAtlas atlas = irr.getSpriterAtlas(animationName);
        SCMLLoader loader = new SCMLLoader(new InternalFileHandleResolver());
        SCMLProject scmlProject = loader.load(atlas, scmlFile);

        currentAnimationIndex = 0;
        currentEntityIndex = 0;

        entity = scmlProject.getEntity(currentEntityIndex);
        if (entity != null) {
            animation = entity.getAnimation(currentAnimationIndex);
            rectangle.set(animation.getBoundingRectangle(null));
            setSize(rectangle.width, rectangle.height);
            animation.update(0);

            Array<Animation> array = entity.getAnimations();
            for (Animation animation : array) {
                animations.add(animation);
            }
        }
        Array<Entity> array = scmlProject.getSourceEntities();
        for (Entity entity : array.iterator()) {
            entities.add(entity);
        }
    }

    @Override
    public void setScale(float scaleXY) {
        super.setScale(scaleXY);
        animation.setScale(scaleXY, scaleXY);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animation.update(delta * 1000);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(1, 1, 1, parentAlpha * getColor().a);
        super.draw(batch, parentAlpha);

        animation.setPosition(getX() + (getWidth() * getScaleX() / 2) - rectangle.x * getScaleX(),
                getY() + (getHeight() * getScaleY() / 2) - rectangle.y * getScaleY());
        animation.setAngle(getRotation());
        animation.setScale(getScaleX(), getScaleY());
        animation.draw(batch);
    }

    public void setAnimation(String animName) {
        animation = entity.getAnimation(animName);
    }

    public ArrayList<Animation> getAnimations() {
        return animations;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}