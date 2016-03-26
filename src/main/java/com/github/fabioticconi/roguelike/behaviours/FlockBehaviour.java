package com.github.fabioticconi.roguelike.behaviours;

import java.util.Set;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Group;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sight;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;
import com.github.fabioticconi.roguelike.systems.GroupSystem;
import com.github.fabioticconi.roguelike.systems.MovementSystem;
import com.github.fabioticconi.roguelike.utils.Coords;

import it.unimi.dsi.fastutil.ints.IntSet;

public class FlockBehaviour extends AbstractBehaviour
{
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Group>    mGroup;

    MovementSystem            sMovement;
    GroupSystem               sGroup;

    @Wire
    EntityGrid                grid;
    @Wire
    Map                       map;

    Position                  curPos;
    Position                  centerOfGroup;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Group.class).build(world);

        centerOfGroup = new Position(0, 0);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (notInterested(entityId))
            return 0f;

        final int groupId = mGroup.get(entityId).groupId;
        final IntSet members = sGroup.getGroup(groupId);

        if (members.size() < 2)
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final Set<Integer> creatures = grid.getEntities(map.getVisibleCells(curPos.x, curPos.y, sight));

        centerOfGroup.x = 0;
        centerOfGroup.y = 0;

        int count = 0;
        Position temp;
        for (final int memberId : members)
        {
            if (creatures.contains(memberId) && memberId != entityId)
            {
                temp = mPosition.getSafe(memberId);

                if (temp == null)
                {
                    continue;
                }

                centerOfGroup.x += temp.x;
                centerOfGroup.y += temp.y;

                count++;
            }
        }

        if (count == 0)
            return 0f;

        centerOfGroup.x = Math.floorDiv(centerOfGroup.x, count);
        centerOfGroup.y = Math.floorDiv(centerOfGroup.y, count);

        final int dist = Coords.distanceChebyshev(curPos.x, curPos.y, centerOfGroup.x, centerOfGroup.y);

        if (dist == 0)
            return 0f;

        return (float) dist / sight;
    }

    @Override
    public float update()
    {
        // let's move toward the center of the group

        Side direction;

        direction = Side.getSideAt(centerOfGroup.x - curPos.x, centerOfGroup.y - curPos.y);

        if (!map.isObstacle(curPos.x, curPos.y, direction))
        {
            direction = Side.getSideAt(curPos.x, centerOfGroup.y - curPos.y);
        } else
        {
            // FIXME is that even possible, since we are looking at visible
            // cells and moving diagonally?
            // if so, we should try the closest exits to the target one

            direction = map.getFreeExitRandomised(curPos.x, curPos.y);
        }

        if (direction == Side.HERE)
            return 0f;

        final float speed = mSpeed.get(entityId).value;

        return sMovement.moveTo(entityId, speed, direction);
    }
}