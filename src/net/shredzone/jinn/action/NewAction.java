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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import net.shredzone.jinn.JinnRegistryKeys;
import net.shredzone.jinn.Registry;
import net.shredzone.jinn.i18n.L;
import net.shredzone.jinn.pool.ImgPool;
import net.shredzone.jinn.property.PropertyModel;

/**
 * Start a new set of properties.
 *
 * @author  Richard Körber &lt;dev@shredzone.de&gt;
 * @version $Id: NewAction.java,v 1.1 2005/11/01 23:19:32 shred Exp $
 */
public class NewAction extends BaseAction {
  private static final long serialVersionUID = -5321317574685533552L;
  protected final Registry registry;
  
  /**
   * Create a new NewAction.
   *
   * @param   registry    The application's Registry
   */
  public NewAction( Registry registry ) {
    super (
      L.tr( "action.new" ),
      ImgPool.get( "new.png" ),
      L.tr( "action.new.tt" ),
      KeyStroke.getKeyStroke( KeyEvent.VK_W, ActionEvent.CTRL_MASK )
    );

    this.registry = registry;
  }
  
  /**
   * The action implementation itself.
   * 
   * @param  e      ActionEvent, may be null if directly invoked
   */
  public void perform( ActionEvent e ) {
    registry.put( JinnRegistryKeys.FILE_TRANSLATION, null );
    registry.put( JinnRegistryKeys.MODEL_TRANSLATION, new PropertyModel() );
    registry.put( JinnRegistryKeys.FILE_REFERENCE, null );
    registry.put( JinnRegistryKeys.MODEL_REFERENCE, new PropertyModel() );
  }
  
}