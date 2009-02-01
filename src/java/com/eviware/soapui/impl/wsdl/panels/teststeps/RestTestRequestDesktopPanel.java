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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RestTestRequestDesktopPanel extends AbstractRestRequestDesktopPanel<RestTestRequestStep, RestTestRequest>
{
   private JLogList logArea;
   private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
   private JButton addAssertionButton;
   protected boolean updatingRequest;
   private AssertionsPanel assertionsPanel;
   private JInspectorPanel inspectorPanel;
   private JComponentInspector<?> assertionInspector;
   private JComponentInspector<?> logInspector;
   private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
   private long startTime;
   private SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

   public RestTestRequestDesktopPanel( RestTestRequestStep requestStep )
   {
      super( requestStep, requestStep.getTestRequest() );

      SoapUI.getTestMonitor().addTestMonitorListener( testMonitorListener );
      setEnabled( !SoapUI.getTestMonitor().hasRunningTest( requestStep.getTestCase() ) );

      requestStep.getTestRequest().addAssertionsListener( assertionsListener );
   }

   protected JComponent buildLogPanel()
   {
      logArea = new JLogList( "Request Log" );

      logArea.getLogList().getModel().addListDataListener( new ListDataChangeListener()
      {

         public void dataChanged( ListModel model )
         {
            logInspector.setTitle( "Request Log (" + model.getSize() + ")" );
         }
      } );

      return logArea;
   }

   protected AssertionsPanel buildAssertionsPanel()
   {
      return new AssertionsPanel( getRequest() )
      {
         protected void selectError( AssertionError error )
         {
            ModelItemXmlEditor<?, ?> editor = (ModelItemXmlEditor<?, ?>) getResponseEditor();
            editor.requestFocus();
         }
      };
   }

   public void setContent( JComponent content )
   {
      inspectorPanel.setContentComponent( content );
   }

   public void removeContent( JComponent content )
   {
      inspectorPanel.setContentComponent( null );
   }

   protected String getHelpUrl()
   {
      return HelpUrls.TESTREQUESTEDITOR_HELP_URL;
   }

   protected JComponent buildContent()
   {
      JComponent component = super.buildContent();

      inspectorPanel = JInspectorPanelFactory.build( component );
      assertionsPanel = buildAssertionsPanel();

      assertionInspector = new JComponentInspector<JComponent>( assertionsPanel, "Assertions ("
              + getModelItem().getAssertionCount() + ")", "Assertions for this Test Request", true );

      inspectorPanel.addInspector( assertionInspector );

      logInspector = new JComponentInspector<JComponent>( buildLogPanel(), "Request Log (0)", "Log of requests", true );
      inspectorPanel.addInspector( logInspector );
      inspectorPanel.setDefaultDividerLocation( 0.6F );
      inspectorPanel.setCurrentInspector( "Assertions" );

      updateStatusIcon();

      return inspectorPanel.getComponent();
   }

   private void updateStatusIcon()
   {
      AssertionStatus status = getModelItem().getTestRequest().getAssertionStatus();
      switch( status )
      {
         case FAILED:
         {
            assertionInspector.setIcon( UISupport.createImageIcon( "/failed_assertion.gif" ) );
            inspectorPanel.activate( assertionInspector );
            break;
         }
         case UNKNOWN:
         {
            assertionInspector.setIcon( UISupport.createImageIcon( "/unknown_assertion.gif" ) );
            break;
         }
         case VALID:
         {
            assertionInspector.setIcon( UISupport.createImageIcon( "/valid_assertion.gif" ) );
            inspectorPanel.deactivate();
            break;
         }
      }
   }

   protected JComponent buildToolbar()
   {
      addAssertionButton = createActionButton( new AddAssertionAction( getRequest() ), true );
      return super.buildToolbar();
   }

   protected void insertButtons( JXToolBar toolbar )
   {
      toolbar.add( addAssertionButton );
      super.insertButtons( toolbar );
   }

   public void setEnabled( boolean enabled )
   {
      if( enabled == true )
         enabled = !SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem().getTestCase() );

      super.setEnabled( enabled );
      addAssertionButton.setEnabled( enabled );
      assertionsPanel.setEnabled( enabled );

      if( SoapUI.getTestMonitor().hasRunningLoadTest( getRequest().getTestCase() ) )
      {
         getRequest().removeSubmitListener( this );
      }
      else
      {
         getRequest().addSubmitListener( this );
      }
   }

   protected Submit doSubmit() throws SubmitException
   {
      return getRequest().submit( new WsdlTestRunContext( getModelItem() ), true );
   }

   private final class InternalAssertionsListener implements AssertionsListener
   {
      public void assertionAdded( TestAssertion assertion )
      {
         assertionInspector.setTitle( "Assertions (" + getModelItem().getAssertionCount() + ")" );
      }

      public void assertionRemoved( TestAssertion assertion )
      {
         assertionInspector.setTitle( "Assertions (" + getModelItem().getAssertionCount() + ")" );
      }
   }

   public boolean beforeSubmit( Submit submit, SubmitContext context )
   {
      boolean result = super.beforeSubmit( submit, context );
      startTime = System.currentTimeMillis();
      return result;
   }

   protected void logMessages( String message, String infoMessage )
   {
      super.logMessages( message, infoMessage );
      logArea.addLine( sdf.format( new Date( startTime ) ) + " - " + message );
   }

   @Override
   public void afterSubmit( Submit submit, SubmitContext context )
   {
      super.afterSubmit( submit, context );
      if( !isHasClosed() )
         updateStatusIcon();
   }

   public boolean onClose( boolean canCancel )
   {
      if( super.onClose( canCancel ) )
      {
         assertionsPanel.release();
         inspectorPanel.release();
         SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener );
         logArea.release();
         getModelItem().getTestRequest().removeAssertionsListener( assertionsListener );
         return true;
      }

      return false;
   }

   public boolean dependsOn( ModelItem modelItem )
   {
      if( getRequest().getResource() == null )
      {
         return modelItem == getRequest() || modelItem == getModelItem() || modelItem == getRequest().getOperation()
                 || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite()
                 || modelItem == getModelItem().getTestCase().getTestSuite().getProject();
      }
      else
      {
         return modelItem == getRequest() || modelItem == getModelItem() || modelItem == getRequest().getOperation()
                 || modelItem == getRequest().getOperation().getInterface()
                 || modelItem == getRequest().getOperation().getInterface().getProject()
                 || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite();
      }
   }

   private class InternalTestMonitorListener extends TestMonitorListenerAdapter
   {
      public void loadTestFinished( LoadTestRunner runner )
      {
         setEnabled( !SoapUI.getTestMonitor().hasRunningTest( getModelItem().getTestCase() ) );
      }

      public void loadTestStarted( LoadTestRunner runner )
      {
         if( runner.getLoadTest().getTestCase() == getModelItem().getTestCase() )
            setEnabled( false );
      }

      public void testCaseFinished( TestRunner runner )
      {
         setEnabled( !SoapUI.getTestMonitor().hasRunningTest( getModelItem().getTestCase() ) );
      }

      public void testCaseStarted( TestRunner runner )
      {
         if( runner.getTestCase() == getModelItem().getTestCase() )
            setEnabled( false );
      }
   }

   public void propertyChange( PropertyChangeEvent evt )
   {
      super.propertyChange( evt );

      if( evt.getPropertyName().equals( RestTestRequest.STATUS_PROPERTY ) )
         updateStatusIcon();
   }
}
