/*
 * Copyright (C) 2017 Fabio Ticconi
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

package com.github.fabioticconi.alone.messages;

import com.github.fabioticconi.alone.constants.Side;

/**
 * Author: Fabio Ticconi
 * Date: 30/10/17
 */
public class CutMsg extends AbstractMessage
{
    public final String tree;

    public CutMsg(final Side direction, final String tree)
    {
        super(0, direction);

        this.tree = tree;
    }

    @Override
    public String format()
    {
        return String.format("You CUT down %s (%s)", tree.toLowerCase(), direction.toString());
    }
}
