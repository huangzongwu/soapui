/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlGroovyTestStep
 * 
 * @author Ole.Matzura
 */

public class GroovyScriptStepPanelBuilder extends EmptyPanelBuilder<WsdlGroovyScriptTestStep>
{
	public GroovyScriptStepPanelBuilder()
   {
   }

   public DesktopPanel buildDesktopPanel(WsdlGroovyScriptTestStep testStep)
   {
      return new GroovyScriptStepDesktopPanel( testStep );
   }

   public boolean hasDesktopPanel()
   {
      return true;
   }
}
