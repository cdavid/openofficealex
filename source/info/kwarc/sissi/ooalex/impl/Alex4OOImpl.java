package info.kwarc.sissi.ooalex.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
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

import com.sun.star.ucb.SendMediaTypes;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.XDesktop;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.script.provider.XScript;
import com.sun.star.script.provider.XScriptProvider;
import com.sun.star.script.provider.XScriptProviderSupplier;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.lib.uno.helper.WeakBase;


public final class Alex4OOImpl extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              info.kwarc.sissi.ooalex.XAlex4OO
{
    private final XComponentContext m_xContext;
    private static final String m_implementationName = Alex4OOImpl.class.getName();
    private static final String[] m_serviceNames = {
        "info.kwarc.sissi.ooalex.Alex4OO" };


    private Logger myLogger;
    private AlexWorker alex = null;
    
    public Alex4OOImpl( XComponentContext context )
    {
    	//CONSTRUTOR    	
        m_xContext = context;
        
        try {
    		myLogger = Logger.getLogger(this.getClass());
			Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "D:\\tmp\\log4j.log"));
			Logger.getRootLogger().setLevel(Level.DEBUG);						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        new AlexWorker("localhost", 54117);
        
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
