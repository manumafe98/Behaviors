/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.minion.move;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.behaviors.components.TargetComponent;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;

/**
 * Turns the actor to face the target defined by <b>TargetComponent</b>.<br/>
 * <br/>
 * <b>SUCCESS</b>: when the actor can see the target.<br/>
 * <b>FAILURE</b>: when there is no target or the target is out of sight.<br/>
 */
@BehaviorAction(name = "look_at")
public class LookAtAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(LookAtAction.class);

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        TargetComponent targetComponent = actor.getComponent(TargetComponent.class);

        if (targetComponent.target == null) {
            return BehaviorState.FAILURE;
        }
        boolean canSeeTarget = process(actor, targetComponent);

        return canSeeTarget ? BehaviorState.SUCCESS : BehaviorState.FAILURE;
    }

    private boolean process(Actor actor, TargetComponent targetComponent) {

        LocationComponent locationComponent = actor.getComponent(LocationComponent.class);
        boolean canSee = true;
        Vector3f worldPos = new Vector3f(locationComponent.getWorldPosition());
        Vector3f targetDirection = new Vector3f();
        LocationComponent targetLocation = targetComponent.target.getComponent(LocationComponent.class);
        targetDirection.sub(targetLocation.getWorldPosition(), worldPos);
        Vector3f drive = new Vector3f(); // Leave blank for no movement

        float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
        float requestedYaw = 180f + yaw * TeraMath.RAD_TO_DEG;

        targetDirection.normalize();

        CharacterMoveInputEvent wantedInput = new CharacterMoveInputEvent(
                0,
                0,
                requestedYaw,
                drive,
                false,
                false,
                false,
                (long) (actor.getDelta() * 1000));
        actor.getEntity().send(wantedInput);

        // TODO Some kind of ray cast to see if there are any obstacles
        return canSee;
    }

}
