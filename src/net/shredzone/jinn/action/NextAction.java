/*
 * jinn -- A tool for easier translation of properties files
 *
 * Copyright (c) 2005 Richard "Shred" Körber
 *   http://www.shredzone.net/go/jinn
 *
 *-----------------------------------------------------------------------
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is JINN.
 *
 * The Initial Developer of the Original Code is
 * Richard "Shred" Körber.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK *****
 */
 
package net.shredzone.jinn.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.shredzone.jinn.JinnRegistryKeys;
import net.shredzone.jinn.Registry;
import net.shredzone.jinn.gui.PropertyKeyModel;
import net.shredzone.jinn.gui.PropertyKeyRefModel;
import net.shredzone.jinn.i18n.L;
import net.shredzone.jinn.pool.ImgPool;

/**
 * Jump to the next untranslated string.
 *
 * @author  Richard Körber &lt;dev@shredzone.de&gt;
 * @version $Id: NextAction.java,v 1.4 2005/11/14 12:14:35 shred Exp $
 */
public class NextAction extends BaseAction {
  private static final long serialVersionUID = 7168091727814398003L;
  protected final Registry registry;
  
  /**
   * Create a new NextAction.
   *
   * @param   registry    The application's Registry
   */
  public NextAction( Registry registry ) {
    super (
      L.tr( "action.next" ),
      ImgPool.get( "next.png" ),
      L.tr( "action.next.tt" ),
      KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK )
    );

    this.registry = registry;
    
    setEnabled( registry.get( JinnRegistryKeys.FILE_REFERENCE ) != null );
    
    registry.addPropertyChangeListener( JinnRegistryKeys.FILE_REFERENCE, new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        setEnabled( evt.getNewValue() != null );
      }
    });
  }
  
  /**
   * The action implementation itself.
   * 
   * @param  e      ActionEvent, may be null if directly invoked
   */
  public void perform( ActionEvent e ) {
    
    final PropertyKeyModel keyModel = (PropertyKeyModel) registry.get( JinnRegistryKeys.MODEL_REFERENCE_KEY );
    if (keyModel instanceof PropertyKeyRefModel) {
      final PropertyKeyRefModel pkrm = (PropertyKeyRefModel) keyModel;
      final String currentKey = registry.getString( JinnRegistryKeys.CURRENT_KEY );
      String nextKey = pkrm.findNext( currentKey );
      
      if (nextKey == null) {
        JOptionPane.showMessageDialog(
            (Component) registry.get( JinnRegistryKeys.FRAME_MAIN ),
            L.tr("a.next.eof"),
            L.tr("a.next.eof.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (pkrm.getSize() > 0) {
          // Start all over again
          nextKey = (String) pkrm.getElementAt( 0 );
        }
      }
      
      registry.put( JinnRegistryKeys.CURRENT_KEY, nextKey );
    }

  }
  
}