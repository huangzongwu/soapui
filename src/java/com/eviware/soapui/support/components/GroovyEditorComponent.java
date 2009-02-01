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
 
package com.eviware.soapui.support.components;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GroovyEditorComponent extends JPanel
{
	private GroovyEditor editor;
	private JButton insertCodeButton;

	public GroovyEditorComponent( GroovyEditorModel editorModel, String helpUrl )
	{
		super( new BorderLayout() );
		
		editor = new GroovyEditor( editorModel );
		editor.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ), 
					editor.getBorder() ));
		add( editor, BorderLayout.CENTER );
		addToolbar( editorModel, helpUrl );
	}

	public GroovyEditor getEditor()
	{
		return editor;
	}

	private void addToolbar( GroovyEditorModel editorModel, String helpUrl )
	{
		JXToolBar toolBar = UISupport.createSmallToolbar();
		
		Action runAction = editorModel.getRunAction();
		if( runAction != null )
		{
			JButton runButton = UISupport.createToolbarButton( runAction );
			if( runButton.getIcon() == null )
				runButton.setIcon( UISupport.createImageIcon( "/run_testcase.gif" ) );
			
			if( runButton.getToolTipText() == null )
				runButton.setToolTipText( "Runs this script" );
			
			toolBar.add( runButton );
			toolBar.addRelatedGap();
		}
		
		insertCodeButton = new JButton( new InsertCodeAction() );
		insertCodeButton.setIcon( UISupport.createImageIcon( "/down_arrow.gif" ) );
		insertCodeButton.setHorizontalTextPosition( SwingConstants.LEFT );
		toolBar.addFixed( insertCodeButton );
		
		toolBar.add( Box.createHorizontalGlue() );
		
		String [] args = editorModel.getKeywords();
		if( args != null && args.length > 0 )
		{
			String scriptName = editorModel.getScriptName();
			if( scriptName == null )
				scriptName = "";
			else 
				scriptName = scriptName.trim() + " ";
			
			StringBuilder text = new StringBuilder( "<html>" + scriptName + "Script is invoked with " );
			for( int c = 0; c < args.length; c++ )
			{
				if( c > 0 )
					text.append( ", " );
				
				text.append( "<font face=\"courier\">" ).append( args[c] ).append( "</font>" );
			}
			text.append( " variables</html>" );
			
			JLabel label = new JLabel( text.toString() );
			label.setToolTipText( label.getText() );
			label.setMaximumSize( label.getPreferredSize() );
			
			toolBar.addFixed( label);
			toolBar.addUnrelatedGap();
		}
		
		if( helpUrl != null )
		{
			toolBar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( helpUrl ) ) );
		}
		
		add( toolBar, BorderLayout.NORTH );
	}
	
	public class InsertCodeAction extends AbstractAction
   {
		public InsertCodeAction()
		{
			super( "Edit" );
			putValue( Action.SHORT_DESCRIPTION, "Inserts code at caret" );
		}
		
		public void actionPerformed( ActionEvent e )
		{
			JPopupMenu popup = editor.getEditArea().getComponentPopupMenu();
			popup.show( insertCodeButton, insertCodeButton.getWidth()/2, insertCodeButton.getHeight()/2 );
		}
   }
}
