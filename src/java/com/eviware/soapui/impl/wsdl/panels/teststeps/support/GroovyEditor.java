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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.FindAndReplaceDialog;
import com.eviware.soapui.support.actions.FindAndReplaceable;
import com.eviware.soapui.support.components.JEditorStatusBar.JEditorStatusBarTarget;
import com.eviware.soapui.support.swing.RSyntaxAreaPopupMenu;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.LineNumberBorder;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.syntax.jedit.KeywordMap;
import org.syntax.jedit.tokenmarker.GroovyTokenMarker;
import org.syntax.jedit.tokenmarker.Token;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Groovy editor wrapper
 *
 * @author ole.matzura
 */

public class GroovyEditor extends JPanel implements JEditorStatusBarTarget
{
   private RSyntaxTextArea editArea;
   private GroovyEditorModel model;
   private InternalSettingsListener settingsListener;
   private GroovyDocumentListener groovyDocumentListener;
   private RTextScrollPane scrollPane;
   private JCheckBoxMenuItem toggleLineNumbersMenuItem;
   //private JPanel lineNumbersPanel;

   public GroovyEditor( GroovyEditorModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      Settings settings = model.getSettings();
      Font editorFont = UISupport.getEditorFont( settings );

      editArea = new RSyntaxTextArea();
      editArea.restoreDefaultSyntaxHighlightingColorScheme();

      setEditorFont( editorFont );

      editArea.setSyntaxEditingStyle( RSyntaxTextArea.GROOVY_SYNTAX_STYLE );
      editArea.setBorder( BorderFactory.createMatteBorder( 0, 2, 0, 0, Color.WHITE ) );

      editArea.setText( model.getScript() );
      editArea.setCaretPosition( 0 );
      editArea.setCurrentLineHighlightEnabled( false );
      Action runAction = model.getRunAction();
      if( runAction != null )
      {
         editArea.getInputMap().put( KeyStroke.getKeyStroke( "alt ENTER" ), "run-action" );
         editArea.getActionMap().put( "run-action", runAction );
      }

      editArea.getInputMap().put( KeyStroke.getKeyStroke( "F3" ), "find-action" );
      editArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F" ), "find-action" );
      RSyntaxTextAreaFindAndReplaceable findAndReplaceable = new RSyntaxTextAreaFindAndReplaceable();
      editArea.getActionMap().put( "find-action", new FindAndReplaceDialog( findAndReplaceable ) );

      groovyDocumentListener = new GroovyDocumentListener();
      editArea.getDocument().addDocumentListener( groovyDocumentListener );

      settingsListener = new InternalSettingsListener();
      settings.addSettingsListener( settingsListener );

      scrollPane = new RTextScrollPane( 500, 300, editArea, true );
      add( scrollPane );

      UISupport.addPreviewCorner( scrollPane, true );

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            editArea.requestFocusInWindow();
         }
      }
      );

      RSyntaxAreaPopupMenu popup = RSyntaxAreaPopupMenu.add( editArea );
      popup.add( new FindAndReplaceDialog( findAndReplaceable ) );
      popup.addSeparator();
      popup.add( new GoToLineAction() );

      toggleLineNumbersMenuItem = new JCheckBoxMenuItem( "Show Line Numbers", scrollPane.areLineNumbersEnabled() );
      toggleLineNumbersMenuItem.setAccelerator( UISupport.getKeyStroke( "alt L" ) );
      toggleLineNumbersMenuItem.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            enableLineNumbers( toggleLineNumbersMenuItem.isSelected() );
         }
      } );

      editArea.getInputMap().put( KeyStroke.getKeyStroke( "alt L" ), new AbstractAction()
      {
         public void actionPerformed( ActionEvent e )
         {
            enableLineNumbers( !scrollPane.areLineNumbersEnabled() );
         }
      } );

      popup.add( toggleLineNumbersMenuItem );
      editArea.setComponentPopupMenu( popup );

      enableLineNumbers( settings.getBoolean( UISettings.SHOW_GROOVY_LINE_NUMBERS ) );
   }

   public void enableLineNumbers( boolean enable )
   {
      scrollPane.setLineNumbersEnabled( enable );
      try
      {
         if( scrollPane.areLineNumbersEnabled() )
            ( (LineNumberBorder) scrollPane.getViewportBorder() ).setBackground( StandaloneSoapUICore.SoapUITheme.BACKGROUND_COLOR );
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }

      toggleLineNumbersMenuItem.setSelected( enable );
   }

//	private JComponent buildLineNumbers()
//	{
//		editArea.getInputHandler().addKeyBinding( "A+L", new ActionListener() {
//
//			public void actionPerformed( ActionEvent e )
//			{
//				lineNumbersPanel.setVisible( !lineNumbersPanel.isVisible() );
//				toggleLineNumbersMenuItem.setSelected( lineNumbersPanel.isVisible() );
//			}} );
//
//		lineNumbersPanel = new LineNumbersPanel( editArea );
//		return lineNumbersPanel;
//	}

   public RSyntaxTextArea getEditArea()
   {
      return editArea;
   }

   public void release()
   {
      if( model != null )
      {
         model.getSettings().removeSettingsListener( settingsListener );
      }
      model = null;
      editArea.getDocument().removeDocumentListener( groovyDocumentListener );
//		editArea.getInputHandler().removeAllKeyBindings();
//		editArea.getRightClickPopup().removeAll();
//		for( PopupMenuListener listener : editArea.getRightClickPopup().getPopupMenuListeners() )
//		{
//			editArea.getRightClickPopup().removePopupMenuListener( listener );
//		}
   }

   public void selectError( String message )
   {
      int ix = message == null ? -1 : message.indexOf( "@ line " );
      if( ix >= 0 )
      {
         try
         {
            int ix2 = message.indexOf( ',', ix );
            int line = ix2 == -1 ? Integer.parseInt( message.substring( ix + 6 ).trim() ) : Integer.parseInt( message
                    .substring( ix + 6, ix2 ).trim() );
            int column = 0;
            if( ix2 != -1 )
            {
               ix = message.indexOf( "column ", ix2 );
               if( ix >= 0 )
               {
                  ix2 = message.indexOf( '.', ix );
                  column = ix2 == -1 ? Integer.parseInt( message.substring( ix + 7 ).trim() ) : Integer
                          .parseInt( message.substring( ix + 7, ix2 ).trim() );
               }
            }

            editArea.setCaretPosition( editArea.getLineStartOffset( line - 1 ) + column - 1 );
         }
         catch( Exception ex )
         {
         }

         editArea.requestFocus();
      }
   }

   private KeywordMap initKeywords()
   {
      KeywordMap keywords = GroovyTokenMarker.getKeywords();

      String[] kw = model.getKeywords();
      if( kw != null )
      {
         for( String keyword : kw )
            keywords.add( keyword, Token.KEYWORD2 );
      }

      return keywords;
   }

   private final class GroovyDocumentListener extends DocumentListenerAdapter
   {
      public void update( Document document )
      {
         GroovyEditor.this.model.setScript( editArea.getText() );
      }
   }

   private final class InternalSettingsListener implements SettingsListener
   {
      public void settingChanged( String name, String newValue, String oldValue )
      {
         if( name.equals( UISettings.EDITOR_FONT ) )
         {
            Font newFont = Font.decode( newValue );
            setEditorFont( newFont );
            invalidate();
         }
      }
   }

   public void setEditorFont( Font newFont )
   {
      editArea.setFont( newFont );

      for( SyntaxScheme scheme : editArea.getSyntaxHighlightingColorScheme().syntaxSchemes )
      {
         if( scheme != null )
         {
            scheme.font = newFont;
         }
      }
   }

   public void addCaretListener( CaretListener listener )
   {
      editArea.addCaretListener( listener );
   }

   public int getCaretPosition()
   {
      return editArea.getCaretPosition();
   }

   public int getLineOfOffset( int offset ) throws Exception
   {
      return editArea.getLineOfOffset( offset );
   }

   public int getLineStartOffset( int line ) throws Exception
   {
      return editArea.getLineStartOffset( line );
   }

   public void removeCaretListener( CaretListener listener )
   {
      editArea.removeCaretListener( listener );
   }

   private final class GoToLineAction extends AbstractAction
   {
      public GoToLineAction()
      {
         super( "Go To Line" );
         putValue( Action.SHORT_DESCRIPTION, "Moves the caret to the specified line" );
         putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu alt L" ) );
      }

      public void actionPerformed( ActionEvent e )
      {
         String line = UISupport.prompt( "Enter line-number to (1.." + ( editArea.getLineCount() ) + ")", "Go To Line",
                 String.valueOf( editArea.getCaretLineNumber() + 1 ) );

         if( line != null )
         {
            try
            {
               int ln = Integer.parseInt( line ) - 1;
               if( ln >= 0 && ln < editArea.getLineCount() )
               {
                  editArea.scrollRectToVisible( editArea.modelToView( editArea.getLineStartOffset( ln ) ) );
                  editArea.setCaretPosition( getLineStartOffset( ln ) );
               }
            }
            catch( Exception e1 )
            {
            }
         }
      }
   }

   private class RSyntaxTextAreaFindAndReplaceable implements FindAndReplaceable
   {
      public boolean isEditable()
      {
         return editArea.isEditable();
      }

      public int getCaretPosition()
      {
         return editArea.getCaretPosition();
      }

      public String getText()
      {
         return editArea.getText();
      }

      public void select( int start, int end )
      {
         editArea.select( start, end );
      }

      public int getSelectionStart()
      {
         return editArea.getSelectionStart();
      }

      public int getSelectionEnd()
      {
         return editArea.getSelectionEnd();
      }

      public void setSelectedText( String txt )
      {
         editArea.replaceSelection( txt );
      }

      public String getSelectedText()
      {
         return editArea.getSelectedText();
      }
   }
}
