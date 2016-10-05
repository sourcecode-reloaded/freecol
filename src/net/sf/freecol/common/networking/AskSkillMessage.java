/**
 *  Copyright (C) 2002-2016   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Direction;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.MoveType;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when asking for the skill taught at a settlement.
 */
public class AskSkillMessage extends TrivialMessage {

    public static final String TAG = "askSkill";
    private static final String DIRECTION_TAG = "direction";
    private static final String UNIT_TAG = "unit";


    /**
     * Create a new {@code AskSkillMessage} with the
     * supplied unit and direction.
     *
     * @param unit The {@code Unit} that is asking.
     * @param direction The {@code Direction} the unit is looking.
     */
    public AskSkillMessage(Unit unit, Direction direction) {
        super(TAG, UNIT_TAG, unit.getId(),
              DIRECTION_TAG, String.valueOf(direction));
    }

    /**
     * Create a new {@code AskSkillMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public AskSkillMessage(Game game, Element element) {
        super(TAG, UNIT_TAG, getStringAttribute(element, UNIT_TAG),
              DIRECTION_TAG, getStringAttribute(element, DIRECTION_TAG));
    }


    /**
     * Handle a "askSkill"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} the message applies to.
     * @param connection The {@code Connection} message was received on.
     *
     * @return An {@code Element} to update the originating player
     *         with the result of the query.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);
        final String unitId = getAttribute(UNIT_TAG);
        final String directionString = getAttribute(DIRECTION_TAG);

        Unit unit;
        try {
            unit = serverPlayer.getOurFreeColGameObject(unitId, Unit.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }

        Tile tile;
        try {
            tile = unit.getNeighbourTile(directionString);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }

        IndianSettlement is = tile.getIndianSettlement();
        if (is == null) {
            return serverPlayer.clientError("There is no native settlement at: "
                + tile.getId())
                .build(serverPlayer);
        }

        MoveType type = unit.getMoveType(is.getTile());
        if (type != MoveType.ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST) {
            return serverPlayer.clientError("Unable to enter " + is.getName()
                + ": " + type.whyIllegal())
                .build(serverPlayer);
        }

        // Update the skill
        return server.getInGameController()
            .askLearnSkill(serverPlayer, unit, is)
            .build(serverPlayer);
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "askSkill".
     */
    public static String getTagName() {
        return TAG;
    }
}
