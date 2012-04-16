package info.kwarc.sissi.ooalex.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import info.kwarc.sissi.Message;
import info.kwarc.sissi.Util;
import info.kwarc.sissi.ooalex.OOMessage;

import com.sun.star.table.CellAddress;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.accessibility.XAccessible;
import com.sun.star.accessibility.XAccessibleComponent;
import com.sun.star.accessibility.XAccessibleContext;
import com.sun.star.accessibility.XAccessibleTable;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.DispatchDescriptor;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.script.provider.XScript;
import com.sun.star.script.provider.XScriptProvider;
import com.sun.star.script.provider.XScriptProviderSupplier;
import com.sun.star.sheet.XCellAddressable;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.lib.uno.helper.WeakBase;


public final class Alex4OOImpl extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              info.kwarc.sissi.ooalex.XAlex4OO,
              com.sun.star.frame.XDispatch,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XInitialization,
              com.sun.star.view.XSelectionChangeListener
              
{
    private final XComponentContext m_xContext;
    private static final String m_implementationName = Alex4OOImpl.class.getName();
    private static final String[] m_serviceNames = {
        "info.kwarc.sissi.ooalex.Alex4OO" };
    
    /** The toolkit, that we can create UNO dialogs.
     */
    private XToolkit m_xToolkit;

    /** The frame where the addon depends on.
     */
    private XFrame m_xFrame;

    private Logger myLogger;
    private AlexWorker alex = null;
    private boolean log_start = false;
    
	public void init_logger() {
		try {
			if (!log_start) {
				myLogger = Logger.getLogger(this.getClass());
				Logger.getRootLogger().addAppender(
						new FileAppender(new SimpleLayout(),
								"D:\\tmp\\log4j.log"));
				Logger.getRootLogger().setLevel(Level.DEBUG);
				Logger.getRootLogger().debug("asdafsa");
			}
			log_start = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public Alex4OOImpl( XComponentContext context )
    {
    	//CONSTRUTOR    	
        m_xContext = context;
    };

    //******************************************
    // RESTRICTED AREA
    //******************************************
    // Do NOT modify the code below unless you know what you are doing.
    // I MEAN IT!
    //******************************************
    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(Alex4OOImpl.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }
    
    //*******************************************
    // END OF RESTRICTED AREA!
    //*******************************************

    /**
	 * @param strMacroName
	 *            The full macro uri to call
	 * @param aParams
	 *            A list of string parameters
	 * @return Returns the return value of the macro.
	 */
	public Object runMacro(String strMacroName, Object[] aParams) {
		try {
			myLogger.info("getting active doc");
			XSpreadsheetDocument activeDoc = getActiveDoc();
			myLogger.info("got active doc:");
			myLogger.info(activeDoc.toString());
			XScriptProviderSupplier xScriptPS = (XScriptProviderSupplier) UnoRuntime
					.queryInterface(XScriptProviderSupplier.class, activeDoc);
			XScriptProvider xScriptProvider = xScriptPS.getScriptProvider();

			// uriMacroname = "vnd.sun.star.script:" + macroname +
			// "?language=Basic&location=application";
			// Example: uriMacroname
			// ="vnd.sun.star.script:Standard.Module1.main?language=Basic&location=application";
			XScript xScript = xScriptProvider.getScript("vnd.sun.star.script:"
					+ strMacroName);

			short[][] aOutParamIndex = new short[1][1];
			Object[][] aOutParam = new Object[1][1];
			myLogger.info("about to invoke macro");
			return xScript.invoke(aParams, aOutParamIndex, aOutParam);

		} catch (Exception e) {
			myLogger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	// *******************************************************************
	public XSpreadsheetDocument getActiveDoc()
			throws com.sun.star.uno.Exception {
		// gets active Document and even active spreadsheet ...
		// What about com.sun.star.sheet.XSpreadsheet with method GetActiveSheet
		// ?
		myLogger.debug("1");
		Object desktop = m_xContext.getServiceManager()
				.createInstanceWithContext("com.sun.star.frame.Desktop",
						m_xContext);
		myLogger.debug(desktop);
		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(
				XDesktop.class, desktop);
		myLogger.debug(xDesktop);
		XComponent xcomponent = xDesktop.getCurrentComponent();
		myLogger.debug(xcomponent);

		XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument) UnoRuntime
				.queryInterface(XSpreadsheetDocument.class, xcomponent);
		myLogger.debug(xSpreadsheetDocument);
		/*
		 * myLogger.debug("5"); XModel xDocModel =
		 * (XModel)UnoRuntime.queryInterface(XModel.class, xSpreadsheetDocument
		 * ); myLogger.debug("6");
		 * 
		 * XModel xSpreadsheetModel = (XModel)
		 * UnoRuntime.queryInterface(XModel.class, xDocModel);
		 * myLogger.debug("7"); XController xSpreadsheetController =
		 * xSpreadsheetModel.getCurrentController(); myLogger.debug("8");
		 * XSpreadsheetView xSpreadsheetView = (XSpreadsheetView)
		 * UnoRuntime.queryInterface(XSpreadsheetView.class,
		 * xSpreadsheetController); myLogger.debug("9");
		 * 
		 * XSpreadsheet activeSheet=xSpreadsheetView.getActiveSheet();
		 * myLogger.debug("10");
		 */
		return xSpreadsheetDocument;
	}

    
	@Override
	public OOMessage composeMessage(short reqid, String action, String data) {
//		return MessageAdapter.adapt(
//				new Message(reqid, action, data )
//		);
		//TODO: fix this		
		return null;
	}

	@Override
	public void loadSemanticData(String arg0) {
		//TODO: implement
		myLogger.debug("asdf");
	}

	@Override
	public String saveSemanticData() {
		//TODO: implement
		return null;
	}

	@Override
	public void sendSallyRequest(OOMessage msg) {
		myLogger.debug("OOAlexImpl.sendSallyRequest: About to execute sendSallyRequest");
		myLogger.debug(msg);
		msg.setAction("graph");
		int resp = this.alex.sendMessage(Util.toJSON(msg));
		if (resp != 0) {
			myLogger.debug("Message has not been sent!!! " + Util.toJSON(msg));
		}
	}

	@Override
	public String stopSally() {
		myLogger.debug("Stopping OOAlex");
		if (this.alex != null && this.alex.isAlive() ) {			
			this.alex.alexStop();			
			myLogger.debug("Stopping OOAlex: " + alex.getState());
		}
		return "success";
	}
	
	public void moveCursorAt(String position) {
		Object[] semObjID = new Object[1];
		semObjID[0] = position;
		runMacro(
				"OOCalcAlexLib.OOCalcAlexModul.moveCursorAt?language=Basic&location=application",
				semObjID);
	}
	
	//Unimplemented methods from XDispatch, XDispatchProvider
	
    // XDispatchProvider
    public XDispatch queryDispatch( /*IN*/com.sun.star.util.URL aURL,
                                    /*IN*/String sTargetFrameName,
                                    /*IN*/int iSearchFlags ) {
        XDispatch xRet = null;
        
        myLogger.debug("queryDispatch");
        myLogger.debug(aURL.Protocol.compareTo("info.kwarc.sissi.ooalex:"));
        
        if ( aURL.Protocol.compareTo("info.kwarc.sissi.ooalex:") == 0 ) {
            if ( aURL.Path.compareTo( "start" ) == 0 )
                xRet = this;
            if ( aURL.Path.compareTo( "stop" ) == 0 )
                xRet = this;           
        }
        return xRet;
    }
    
    public XDispatch[] queryDispatches( /*IN*/DispatchDescriptor[] seqDescripts ) {
        int nCount = seqDescripts.length;
        XDispatch[] lDispatcher = new XDispatch[nCount];
        
        for( int i=0; i<nCount; ++i )
            lDispatcher[i] = queryDispatch( seqDescripts[i].FeatureURL,
                                            seqDescripts[i].FrameName,
                                            seqDescripts[i].SearchFlags );
        
        return lDispatcher;           
    }

    // XDispatch
    public void dispatch( /*IN*/com.sun.star.util.URL aURL,
                          /*IN*/com.sun.star.beans.PropertyValue[] aArguments ) {
    	
    	init_logger();
        
    	myLogger.debug("in dispatch");
    	myLogger.debug(aURL.Protocol.compareTo("info.kwarc.sissi.ooalex:"));
        
        if ( aURL.Protocol.compareTo("info.kwarc.sissi.ooalex:") == 0 )
        {
            if ( aURL.Path.compareTo( "start" ) == 0 )
            {
            	Logger.getRootLogger().debug("trying to attach");
            	try{
	            	//Code from above
	            	Object desktop = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);        		
	        		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);        		
	        		XComponent xcomponent = xDesktop.getCurrentComponent();
	        		XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, xcomponent);
	                com.sun.star.frame.XModel xModel = (com.sun.star.frame.XModel)UnoRuntime.queryInterface( com.sun.star.frame.XModel.class, xSpreadsheetDocument);
	                //End code from above
	            	
	            	XSpreadsheetView xSheetView = (XSpreadsheetView)UnoRuntime.queryInterface(XSpreadsheetView.class, xModel.getCurrentController() );
	                XSelectionSupplier xSelectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(XSelectionSupplier.class, xSheetView );
	                xSelectionSupplier.addSelectionChangeListener(this);
	                
            	} catch (Exception ex) {
            		Logger.getRootLogger().error(ex);
            	}
            	Logger.getRootLogger().debug("fingers crossed");
            	this.alex = new AlexWorker("localhost", 54117);
                showMessageBox("SDK DevGuide Add-On example", "Event attached");
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if ( aURL.Path.compareTo( "stop" ) == 0 )
            {
            	Logger.getRootLogger().debug("trying to detach");
            	try{
	            	//Code from above
	            	Object desktop = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);        		
	        		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);        		
	        		XComponent xcomponent = xDesktop.getCurrentComponent();
	        		XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, xcomponent);
	                XModel xModel = (XModel)UnoRuntime.queryInterface( XModel.class, xSpreadsheetDocument);
	                //End code from above
	            	
	            	XSpreadsheetView xSheetView = (XSpreadsheetView)UnoRuntime.queryInterface(XSpreadsheetView.class, xModel.getCurrentController() );
	                XSelectionSupplier xSelectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(XSelectionSupplier.class, xSheetView );
	                xSelectionSupplier.removeSelectionChangeListener(this);
            	} catch(Exception ex) {
            	}
            	Logger.getRootLogger().debug("fingers crossed");
            }            
        }
    }
    
    public void addStatusListener( /*IN*/XStatusListener xControl,
                                   /*IN*/com.sun.star.util.URL aURL ) {
    }
    
    public void removeStatusListener( /*IN*/XStatusListener xControl,
                                      /*IN*/com.sun.star.util.URL aURL ) {
    }

    public void showMessageBox(String sTitle, String sMessage) {
        try {
            if ( null != m_xFrame && null != m_xToolkit ) {

                // describe window properties.
                WindowDescriptor aDescriptor = new WindowDescriptor();
                aDescriptor.Type              = WindowClass.MODALTOP;
                aDescriptor.WindowServiceName = new String( "infobox" );
                aDescriptor.ParentIndex       = -1;
                aDescriptor.Parent            = (XWindowPeer)UnoRuntime.queryInterface(
                    XWindowPeer.class, m_xFrame.getContainerWindow());
                aDescriptor.Bounds            = new Rectangle(0,0,300,200);
                aDescriptor.WindowAttributes  = WindowAttribute.BORDER |
                    WindowAttribute.MOVEABLE |
                    WindowAttribute.CLOSEABLE;
                
                XWindowPeer xPeer = m_xToolkit.createWindow( aDescriptor );
                if ( null != xPeer ) {
                    XMessageBox xMsgBox = (XMessageBox)UnoRuntime.queryInterface(
                        XMessageBox.class, xPeer);
                    if ( null != xMsgBox )
                    {
                        xMsgBox.setCaptionText( sTitle );
                        xMsgBox.setMessageText( sMessage );
                        xMsgBox.execute();
                    }
                }
            }
        } catch ( com.sun.star.uno.Exception e) {
            // do your error handling 
        }
    }

	@Override
	public void initialize(Object[] object) throws com.sun.star.uno.Exception {		
		if (object.length > 0) {
			m_xFrame = (XFrame) UnoRuntime.queryInterface(XFrame.class,
					object[0]);
		}

		// Create the toolkit to have access to it later
		m_xToolkit = (XToolkit) UnoRuntime.queryInterface(
				XToolkit.class,
				m_xContext.getServiceManager().createInstanceWithContext(
						"com.sun.star.awt.Toolkit", m_xContext));
		
		init_logger();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	@Override
	public void disposing(EventObject aSourceObj) {
		// stop listening for selection changes
        XSelectionSupplier aCtrl = (XSelectionSupplier) UnoRuntime.queryInterface(
            XSelectionSupplier.class, aSourceObj );
        if( aCtrl != null )
            aCtrl.removeSelectionChangeListener( this );

        // remove as dispose listener
        XComponent aComp = (XComponent) UnoRuntime.queryInterface( XComponent.class, aSourceObj );
        if( aComp != null )
            aComp.removeEventListener( this );

	}

	static int eventTriggered = Integer.MIN_VALUE; 
	
	@Override
	public void selectionChanged(EventObject aEvent) {
		Logger.getRootLogger().debug("in selection changed event" + eventTriggered%3);		
        XController aCtrl = (XController) UnoRuntime.queryInterface( XController.class, aEvent.Source );
        if( aCtrl != null && (eventTriggered % 3 == 0)) {
			try {
				//Code from above
				Object desktop = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);			        		
	    		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);        		
	    		XComponent xcomponent = xDesktop.getCurrentComponent();
	    		XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, xcomponent);
	            XModel xModel = (XModel)UnoRuntime.queryInterface( XModel.class, xSpreadsheetDocument);
	            //End code from above
	            
	            XCellRangeAddressable xSheetCellAddressable = (XCellRangeAddressable) 
	            		UnoRuntime.queryInterface(XCellRangeAddressable.class, xModel.getCurrentSelection());
	            CellRangeAddress addr = xSheetCellAddressable.getRangeAddress();
	            Logger.getRootLogger().debug(
	            		addr.StartColumn + ":" + addr.EndColumn + " " + 
	            		addr.StartRow + ":" + addr.EndRow
	            		);
	            
	             	            
	            //now get the position
	            XFrame frame = xDesktop.getCurrentFrame();
	            XWindow win = frame.getContainerWindow();
	            
	            
	            XAccessible accessible = (XAccessible) UnoRuntime.queryInterface(XAccessible.class, win);
	    		XAccessibleContext accessibleContext = accessible.getAccessibleContext();
	    		XAccessibleContext table = getAccessibleForRole(accessibleContext, com.sun.star.accessibility.AccessibleRole.TABLE);
	    		XAccessibleTable accessibleTable = (XAccessibleTable) UnoRuntime.queryInterface(XAccessibleTable.class, table);
	    		
	    		XAccessibleContext cellContext = null;
	    		XCellAddressable cellAddressable = (XCellAddressable) UnoRuntime.queryInterface(XCellAddressable.class, xModel.getCurrentSelection());
	    		CellAddress cellAddress = cellAddressable.getCellAddress();	    		
	    		
	    		if(accessibleTable != null)
	    		{
	    			try
	    			{
	    				cellContext = accessibleTable.getAccessibleCellAt(cellAddress.Row, cellAddress.Column).getAccessibleContext();
	    			}
	    			catch (IndexOutOfBoundsException e)	{
	    			}
	    		}
	    		
	    		XAccessibleComponent accessibleComponent = (XAccessibleComponent) UnoRuntime.queryInterface(XAccessibleComponent.class, cellContext);
	    		if (accessibleComponent == null) {
	    			Logger.getRootLogger().debug("no info");
	    		}
	            int x = accessibleComponent.getLocationOnScreen().X;
	            int y = accessibleComponent.getLocationOnScreen().Y;
	            
	            
	            XCell c = (XCell)UnoRuntime.queryInterface(XCell.class, xModel.getCurrentSelection());
	            Logger.getRootLogger().debug("Formula " + c.getFormula());
	            Logger.getRootLogger().debug("Value " + c.getValue());
	            
	            XColumnRowRange rng = (XColumnRowRange)UnoRuntime.queryInterface(XColumnRowRange.class, xModel.getCurrentSelection());
	            Object oCol = rng.getColumns().getByIndex(cellAddress.Column);
	            XPropertySet xPropertySet1 = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oCol);
	            String width = xPropertySet1.getPropertyValue("Width").toString();
	            int w = Integer.parseInt(width);	            
	            x += w;
	            
	            Logger.getRootLogger().debug("pos: " + x + " " + y);
	            
	            HashMap<String,String> map = new HashMap<String,String>();
	        	map.put("select", addr.StartColumn + ":" + addr.EndColumn + " " + 
	            		addr.StartRow + ":" + addr.EndRow);
	        	map.put("pos", x + "," + y);
	        	
	        	Message m = new Message(Message.randomReqId(), "alex.click", map);
	        	this.alex.sendMessage(Util.messageToJSON(m));
	            
			} catch (com.sun.star.uno.Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        eventTriggered++;
	}
	
	/**traverses the accessibility tree and returns the first {@link XAccessibleContext} with the given role
	 * @param accessibleContext the context to start searching with
	 * @param role the role of the context that should be returned
	 * @return the first XAccessibleContext found with the given role or <code>null</code>
	 * */
	private XAccessibleContext getAccessibleForRole(XAccessibleContext accessibleContext,
			short role)
	{
		XAccessibleContext ret = null;
		XAccessibleContext accessibleContext2 = null;
		int count = accessibleContext.getAccessibleChildCount();
		for (int i = 0; i < count; i++)
		{
			if(ret != null)
				return ret;
			try
			{
				accessibleContext2 = accessibleContext.getAccessibleChild(i).getAccessibleContext();
			}
			catch (IndexOutOfBoundsException e)
			{
				return ret;
			} catch (com.sun.star.lang.IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(accessibleContext2.getAccessibleRole() == role)
			{
				ret = accessibleContext2;
				return ret;
			}
			else
				ret = getAccessibleForRole(accessibleContext2, role);
		}
		return ret;
	}

}

class AlexWorker extends Thread {
	public Socket s = null;
	public BufferedReader in;
	public PrintWriter out;
	private Logger myLogger;
	private String host;
	private int port;
	private boolean done = false;

	public AlexWorker(String host, int port) {
		this.host = host;
		this.port = port;
		new Thread(this).start();
	}
	
	public void alexStop() {
		//TODO: also fetch the interpretation mapping from Sally and save it
		//TODO: check if we are the last connection -- if yes, stop Sally
		this.done = true;
		try {
			this.s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int sendMessage(String str) {
		try {
			out.println(str);
		} catch (Exception ex) {
			myLogger.info("Sending message failed " + str);
			return 1;
		}
		return 0;
	}
	
	private void parse(Message m) {
		if (m.getAction().equalsIgnoreCase("init")) {
			//send the whoami message
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("type", "alex");
			map.put("doctype", "spreadsheet");
			map.put("setup", "desktop");
			Message whoami  = new Message(Message.randomReqId(),"whoami", map);
			sendMessage(Util.messageToJSON(whoami));
			
			map.clear();
			//send the alex.imap message
			map.put("imap", "test");
			Message imap = new Message(Message.randomReqId(), "alex.imap", map);
			sendMessage(Util.messageToJSON(imap));
			
		} else if (m.getAction().equalsIgnoreCase("alex.select")) {
			
		} else {
			//PANIC
		}
	}

	@Override
	public void run() {
		myLogger = Logger.getLogger(this.getClass());
		myLogger.info("Init ClientThread");

			
		//try to connect
		//if the socket is not open (yet),
		//wait for 1 second and try again.
		
		while (!done) {
			try {
				s = new Socket(this.host, this.port);
				done = true;
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					myLogger.debug("InterruptedException " + e.toString());
				}
			}
		}
		
		//make sure we are connected. otherwise, exit
		if (!done) return;
		
		//once we are connected
		//get the buffers right
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true); //make sure it automatically flushes
			String str = "";
			while ((str = in.readLine()) != null) {
				myLogger.debug("messageReceived " + str);				
				parse(Util.messageFromJSON(str));				
			}			
		} catch (Exception e) {
			e.printStackTrace();
			myLogger.debug(e.toString());
		} finally {
			try {
				this.s.close();
			} catch (IOException e) {
				myLogger.debug(e.toString());
			}
		}
	}
	
}
