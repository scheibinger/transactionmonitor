/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.MyLogger;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Administrator
 */
public class MyLogger extends Logger {

	private static JTextArea outputTextArea = null;
    private static MyLogger logger = null;

	protected MyLogger(String s){
		super(s, null);
	}

    public static MyLogger getLogger() {
        if (logger == null) {
            logger = new MyLogger("tranasctionLoger");
        }
        return logger;
    }


	public static void setOutputTextArea(JTextArea ota) {
		outputTextArea = ota;
	}

	public static JTextArea getOutputTextArea() {
		return outputTextArea;
	}


	public static void log(String msg) {
		MyLogger.getLogger().log(Level.ALL, msg);
		outputTextArea.append(msg +"\n");
	}

}
