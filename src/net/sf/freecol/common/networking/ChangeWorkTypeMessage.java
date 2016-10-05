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
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when changing the work type of a unit.
 */
public class ChangeWorkTypeMessage extends TrivialMessage {

    public static final String TAG = "changeWorkType";
    private static final String UNIT_TAG = "unit";
    private static final String WORK_TYPE_TAG = "workType";


    /**
     * Create a new {@code ChangeWorkTypeMessage} with the
     * supplied unit and produce.
     *
     * @param unit The {@code Unit} that is working.
     * @param workType The {@code GoodsType} to produce.
     */
    public ChangeWorkTypeMessage(Unit unit, GoodsType workType) {
        super(TAG, UNIT_TAG, unit.getId(), WORK_TYPE_TAG, workType.getId());
    }

    /**
     * Create a new {@code ChangeWorkTypeMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public ChangeWorkTypeMessage(Game game, Element element) {
        super(TAG, UNIT_TAG, getStringAttribute(element, UNIT_TAG),
              WORK_TYPE_TAG, getStringAttribute(element, WORK_TYPE_TAG));
    }


    /**
     * Handle a "changeWorkType"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} the message applies to.
     * @param connection The {@code Connection} message was received on.
     * @return An update containing the changes, or an error
     *     {@code Element} on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);
        final Specification spec = server.getSpecification();
        final String unitId = getAttribute(UNIT_TAG);
        final String workTypeId = getAttribute(WORK_TYPE_TAG);

        Unit unit;
        try {
            unit = player.getOurFreeColGameObject(unitId, Unit.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }
        if (!unit.hasTile()) {
            return serverPlayer.clientError("Unit is not on the map: "
                + unitId)
                .build(serverPlayer);
        }

        GoodsType type = spec.getGoodsType(workTypeId);
        if (type == null) {
            return serverPlayer.clientError("Not a goods type: "
                + workTypeId)
                .build(serverPlayer);
        }

        // Proceed to changeWorkType.
        return server.getInGameController()
            .changeWorkType(serverPlayer, unit, type)
            .build(serverPlayer);
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "changeWorkType".
     */
    public static String getTagName() {
        return TAG;
    }
}

