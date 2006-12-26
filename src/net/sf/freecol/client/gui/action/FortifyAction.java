

package net.sf.freecol.client.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.common.model.Unit;


/**
* An action for fortifying the active unit.
*/
public class FortifyAction extends MapboardAction {
    private static final Logger logger = Logger.getLogger(FortifyAction.class.getName());

    public static final String  COPYRIGHT = "Copyright (C) 2003-2005 The FreeCol Team";
    public static final String  LICENSE = "http://www.gnu.org/licenses/gpl.html";
    public static final String  REVISION = "$Revision$";

    public static final String ID = "fortifyAction";

    /**
     * Creates this action.
     * @param freeColClient The main controller object for the client.
     */
    FortifyAction(FreeColClient freeColClient) {
        super(freeColClient, "unit.state.2", null, KeyStroke.getKeyStroke('F', 0));
        putValue(BUTTON_IMAGE, freeColClient.getImageLibrary().getUnitButtonImageIcon(ImageLibrary.UNIT_BUTTON_FORTIFY, 0));
        putValue(BUTTON_ROLLOVER_IMAGE, freeColClient.getImageLibrary().getUnitButtonImageIcon(ImageLibrary.UNIT_BUTTON_FORTIFY, 1));
        putValue(BUTTON_PRESSED_IMAGE, freeColClient.getImageLibrary().getUnitButtonImageIcon(ImageLibrary.UNIT_BUTTON_FORTIFY, 2));
        putValue(BUTTON_DISABLED_IMAGE, freeColClient.getImageLibrary().getUnitButtonImageIcon(ImageLibrary.UNIT_BUTTON_FORTIFY, 3));        
    }
    
    
    /**
     * Checks if this action should be enabled.
     * 
     * @return <code>true</code> if there is an active unit.
     */
    protected boolean shouldBeEnabled() { 
        return super.shouldBeEnabled()  
                && getFreeColClient().getGUI().getActiveUnit() != null
                && getFreeColClient().getGUI().getActiveUnit().checkSetState(Unit.FORTIFYING);
    }
    
    /**
    * Returns the id of this <code>Option</code>.
    * @return 
    */
    public String getId() {
        return ID;
    }


    /**
     * Applies this action.
     * @param e The <code>ActionEvent</code>.
     */    
    public void actionPerformed(ActionEvent e) {
        getFreeColClient().getInGameController().changeState(getFreeColClient().getGUI().getActiveUnit(), Unit.FORTIFYING);
    }
}
