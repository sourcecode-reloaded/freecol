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
 * The message sent when unloading goods.
 */
public class UnloadGoodsMessage extends TrivialMessage {

    public static final String TAG = "unloadGoods";
    private static final String AMOUNT_TAG = "amount";
    private static final String CARRIER_TAG = "carrier";
    private static final String TYPE_TAG = "type";


    /**
     * Create a new {@code UnloadGoodsMessage}.
     *
     * @param goodsType The {@code GoodsType} to unload.
     * @param amount The amount of goods to unload.
     * @param carrier The {@code Unit} carrying the goods.
     */
    public UnloadGoodsMessage(GoodsType goodsType, int amount, Unit carrier) {
        super(TAG, TYPE_TAG, goodsType.getId(),
              AMOUNT_TAG, String.valueOf(amount),
              CARRIER_TAG, carrier.getId());
    }

    /**
     * Create a new {@code UnloadGoodsMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public UnloadGoodsMessage(Game game, Element element) {
        super(TAG, TYPE_TAG, getStringAttribute(element, TYPE_TAG),
              AMOUNT_TAG, getStringAttribute(element, AMOUNT_TAG),
              CARRIER_TAG, getStringAttribute(element, CARRIER_TAG));
    }


    /**
     * Handle a "unloadGoods"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} the message applies to.
     * @param connection The {@code Connection} message was received on.
     * @return An update containing the carrier, or an error
     *     {@code Element} on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);
        final Specification spec = server.getSpecification();
        final String typeId = getAttribute(TYPE_TAG);
        final String amountString = getAttribute(AMOUNT_TAG);
        final String carrierId = getAttribute(CARRIER_TAG);
        
        Unit carrier;
        try {
            carrier = player.getOurFreeColGameObject(carrierId, Unit.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }
        if (!carrier.canCarryGoods()) {
            return serverPlayer.clientError("Not a goods carrier: " + carrierId)
                .build(serverPlayer);
        }
        // Do not check location, carriers can dump goods anywhere

        GoodsType type = spec.getGoodsType(typeId);
        if (type == null) {
            return serverPlayer.clientError("Not a goods type: " + typeId)
                .build(serverPlayer);
        }

        int amount;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            return serverPlayer.clientError("Bad amount: " + amountString)
                .build(serverPlayer);
        }
        if (amount <= 0) {
            return serverPlayer.clientError("Amount must be positive: "
                + amountString)
                .build(serverPlayer);
        }
        int present = carrier.getGoodsCount(type);
        if (present < amount) {
            return serverPlayer.clientError("Attempt to unload " + amount
                + " " + type.getId() + " but only " + present + " present.")
                .build(serverPlayer);
        }

        // Try to unload.
        return server.getInGameController()
            .unloadGoods(serverPlayer, type, amount, carrier)
            .build(serverPlayer);
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "unloadGoods".
     */
    public static String getTagName() {
        return TAG;
    }
}
