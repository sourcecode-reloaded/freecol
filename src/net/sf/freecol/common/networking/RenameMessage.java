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

import net.sf.freecol.common.model.FreeColGameObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Nameable;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when renaming a FreeColGameObject.
 */
public class RenameMessage extends TrivialMessage {

    public static final String TAG = "rename";
    private static final String NAMEABLE_TAG = "nameable";
    private static final String NAME_TAG = "name";


    /**
     * Create a new {@code RenameMessage} with the
     * supplied name.
     *
     * @param object The {@code FreeColGameObject} to rename.
     * @param newName The new name for the object.
     */
    public RenameMessage(FreeColGameObject object, String newName) {
        super(TAG, NAMEABLE_TAG, object.getId(), NAME_TAG, newName);
    }

    /**
     * Create a new {@code RenameMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public RenameMessage(Game game, Element element) {
        super(TAG, NAMEABLE_TAG, getStringAttribute(element, NAMEABLE_TAG),
              NAME_TAG, getStringAttribute(element, NAME_TAG));
    }


    /**
     * Handle a "rename"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} the message applies to.
     * @param connection The {@code Connection} message was received on.
     *
     * @return An update containing the renamed unit,
     *         or an error {@code Element} on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);
        final String nameableId = getAttribute(NAMEABLE_TAG);
        
        FreeColGameObject fcgo;
        try {
            fcgo = player.getOurFreeColGameObject(nameableId, FreeColGameObject.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }
        if (!(fcgo instanceof Nameable)) {
            return serverPlayer.clientError("Not a nameable: " + nameableId)
                .build(serverPlayer);
        }

        // Proceed to rename.
        return server.getInGameController()
            .renameObject(serverPlayer, (Nameable)fcgo, getAttribute(NAME_TAG))
            .build(serverPlayer);
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "rename".
     */
    public static String getTagName() {
        return TAG;
    }
}
