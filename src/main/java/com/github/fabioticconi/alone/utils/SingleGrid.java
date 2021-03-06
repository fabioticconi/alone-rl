/*
 * Copyright (C) 2015-2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.fabioticconi.alone.utils;

import com.artemis.utils.IntBag;

import java.util.Arrays;

/**
 * Supports storage and retrieval of entities placed on a 2D grid, where each cell can contain only a single
 * entity.
 * <p>
 * Has plenty of support methods for accumulating entities in specific topologies (eg, by
 * moving outward/spiralling from a central point, up to a specified radius).
 *
 * @author Fabio Ticconi
 */
public class SingleGrid
{
    final int     width;
    final int     height;
    final int[][] grid;

    public SingleGrid(final int width, final int height)
    {
        this.width = width;
        this.height = height;

        grid = new int[width][height];

        clear();
    }

    /**
     * Just a boundary check, does not check the content of the cell.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean has(final int x, final int y)
    {
        return Util.in(x, 0, width - 1) && Util.in(y, 0, height - 1);
    }

    public boolean has(final int id, final int x, final int y)
    {
        return has(x, y) && grid[x][y] == id;
    }

    public int get(final int x, final int y)
    {
        return grid[x][y];
    }

    public int set(final int id, final int x, final int y)
    {
        final int old = grid[x][y];
        grid[x][y] = id;
        return old;
    }

    public int del(final int x, final int y)
    {
        return set(-1, x, y);
    }

    public int move(final int oldX, final int oldY, final int x, final int y)
    {
        if (!has(oldX, oldY) || !has(x, y))
            return -1;

        final int id = grid[oldX][oldY];
        grid[oldX][oldY] = -1;
        final int old = grid[x][y];
        grid[x][y] = id;

        return old;
    }

    public void clear()
    {
        // -1 means "no entity"
        for (final int[] row : grid)
        {
            Arrays.fill(row, -1);
        }
    }

    public boolean isEmpty(final int x, final int y)
    {
        return grid[x][y] < 0;
    }

    /**
     * Returns all entities in the specified cells.
     *
     * @param cells set of packed coordinates of entities
     * @return
     */
    public IntBag getEntities(final LongBag cells)
    {
        final IntBag entities = new IntBag(8);

        final int[] coords = new int[2];

        for (int i = 0, size = cells.size(); i < size; i++)
        {
            final long pos = cells.get(i);

            Coords.unpackCoords(pos, coords);

            final int id = grid[coords[0]][coords[1]];

            if (id >= 0)
                entities.add(id);
        }

        return entities;
    }

    /**
     * Moves concentrically from the specified cell, collecting entities. If the
     * current cell has an entity, returns only that. If there are any entities
     * in the first ring around the cell, returns those only.
     * It keeps going around until it finds a non-empty "ring" or up to maxRadius,
     * whichever comes first.
     *
     * @param x
     * @param y
     * @param maxRadius
     * @return
     */
    public IntBag getClosestEntities(final int x, final int y, int maxRadius)
    {
        if (grid[x][y] >= 0)
        {
            final IntBag single = new IntBag(1);
            single.add(grid[x][y]);
            return single;
        }
        else if (maxRadius == 0) // we don't care about looking further
            return new IntBag(0);

        final IntBag entities = new IntBag(maxRadius * maxRadius * 4);

        // avoid stupid crashes for negative radii
        maxRadius = Math.abs(maxRadius);

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= maxRadius; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to
            // the correct east-side column, at the same y position as we are

            final int max_x = x + d;
            final int max_y = y + d;
            final int min_x = x - d;
            final int min_y = y - d;

            // continue east, through the north row
            for (; cur_x < max_x; cur_x++)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= width)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue south, through the east column
            for (; cur_y < max_y; cur_y++)
            {
                if (cur_y < 0 || cur_y >= height)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue west, through the south row
            for (; cur_x > min_x; cur_x--)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= width)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue north, through the west column of this circle
            for (; cur_y >= min_y; cur_y--)
            {
                if (cur_y < 0 || cur_y >= height)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // if at this round we have found entities, we must stop
            if (!entities.isEmpty())
                return entities;

            // at this point we are positioned WITHIN the north row of the next
            // cycle
        }

        // if we are here, we haven't found any entities so we return the empty
        // set we had created at the beginning

        return entities;
    }

    /**
     * Gets all entities inside the squared-ring at distance r from the
     * specified point.
     *
     * @param x
     * @param y
     * @param r
     * @return
     */
    public IntBag getEntitiesAtRadius(final int x, final int y, final int r)
    {
        if (r < 0)
            return new IntBag(0);

        final IntBag entities = new IntBag(r * 8);

        if (r == 0)
        {
            if (grid[x][y] < 0)
                return new IntBag(0);

            entities.add(grid[x][y]);

            return entities;
        }

        // we put the cursor where it would have been if we were in one
        // iteration of "getClosestEntities"
        int cur_y = y - r;
        int cur_x = x - r + 1;

        // FIXME what do we do if the north row is "out of bound" already?
        // we should skip the next for and position ourselves immediately to the
        // correct east-side column, at the same y position as we are

        final int max_x = x + r;
        final int max_y = y + r;
        final int min_x = x - r;
        final int min_y = y - r;

        // continue east, through the north row
        for (; cur_x < max_x; cur_x++)
        {
            // if we are out of bounds
            if (cur_x < 0 || cur_x >= width)
            {
                continue;
            }

            if (grid[cur_x][cur_y] >= 0)
            {
                entities.add(grid[cur_x][cur_y]);
            }
        }

        // continue south, through the east column
        for (; cur_y < max_y; cur_y++)
        {
            if (cur_y < 0 || cur_y >= height)
            {
                continue;
            }

            if (grid[cur_x][cur_y] >= 0)
            {
                entities.add(grid[cur_x][cur_y]);
            }
        }

        // continue west, through the south row
        for (; cur_x > min_x; cur_x--)
        {
            // if we are out of bounds
            if (cur_x < 0 || cur_x >= width)
            {
                continue;
            }

            if (grid[cur_x][cur_y] >= 0)
            {
                entities.add(grid[cur_x][cur_y]);
            }
        }

        // continue north, through the west column of this circle
        for (; cur_y >= min_y; cur_y--)
        {
            if (cur_y < 0 || cur_y >= height)
            {
                continue;
            }

            if (grid[cur_x][cur_y] >= 0)
            {
                entities.add(grid[cur_x][cur_y]);
            }
        }

        return entities;
    }

    /**
     * Starting from the specified cell, moves concentrically within the given
     * radius. The returned set guarantees the entities are ordered by closeness
     * to the starting point.
     *
     * @param x
     * @param y
     * @param r
     * @return
     */
    public IntBag getEntitiesWithinRadius(final int x, final int y, final int r)
    {
        final IntBag entities = new IntBag(r * r * 8);

        if (grid[x][y] >= 0)
        {
            entities.add(grid[x][y]);

            return entities;
        }

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= r; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to
            // the
            // correct east-side column, at the same y position as we are

            final int max_x = x + d;
            final int max_y = y + d;
            final int min_x = x - d;
            final int min_y = y - d;

            // continue east, through the north row
            for (; cur_x < max_x; cur_x++)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= width)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue south, through the east column
            for (; cur_y < max_y; cur_y++)
            {
                if (cur_y < 0 || cur_y >= height)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue west, through the south row
            for (; cur_x > min_x; cur_x--)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= width)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // continue north, through the west column of this circle
            for (; cur_y >= min_y; cur_y--)
            {
                if (cur_y < 0 || cur_y >= height)
                {
                    continue;
                }

                if (grid[cur_x][cur_y] >= 0)
                {
                    entities.add(grid[cur_x][cur_y]);
                }
            }

            // at this point we are positioned WITHIN the north row of the next
            // circle
        }

        return entities;
    }
}
