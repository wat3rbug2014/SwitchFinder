// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 07:55:04 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

/**
 * This object is for sending broadcast beacons only.  Beacons are are UDP
 * packets on port 10077.  They incorporate a SHA1 hash of the file and 
 * the timestamp for the last time it was modified.  A future version will
 * be done to use non-blocking I/O.  Note: This will be refactored because 
 * during initial development other operations were being performed for 
 * the file information, which have since been removed.
 * @author Douglas Gardiner
 */

public class Broadcast {
   
    // class variables

    private static final String DEF_IP_ADDR = "255.255.255.255"; 
	private DatagramSocket broadcastSocket;
	private DebugWindow debugger = null;
	private static long fileDate = 0;
	private static String filename = "switches.csv";
	private static final int PORT = 10077;
	Checks securityChecks = null;
	private static File switchFile = new File(filename);
    String workingAddress = DEF_IP_ADDR;
	
    // constructors

    /**
     * This object is for sending broadcast beacons only.  Beacons are are UDP
	 * packets on port 10077.  They incorporate a SHA1 hash of the file and 
	 * the timestamp for the last time it was modified.  A future version will
	 * be done to use non-blocking I/O.  Note: This will be refactored because 
	 * during initial development other operations were being performed for 
	 * the file information, which have since been removed.
     */

	/**
     * Creates a UDP Broadcast object with debugging enabled.
	 * @param securityChecks the reference to checks object
	 * @param passedframe is the reference to DebugWindow that the object
	 * will use.
     */

    public Broadcast(Checks securityChecks, DebugWindow passedframe) {

		this.securityChecks = securityChecks;
        debugger = passedframe;
        finishConstructor();
    }
    /**
     * Creates a UDP Broadcast object.
	 * @param securityChecks the reference to checks object.
     */

    public Broadcast(Checks securityChecks) {
    
        this.securityChecks = securityChecks;
		finishConstructor();
    }
    // methods 
   
	/**
     * Common method for the two constructors to complete
     * tasks for both.  Constructor overloading was not working
     * as intended.
     */

    private void finishConstructor() {
     
        switchFile = new File(filename);
        fileDate = switchFile.lastModified();
        update("File date is " + fileDate);
    }
	/**
     * Gets the SHA1 hash of the switch file.
     * @return String hex form of file hash.
     */

    public String getFileCRC() {
        
        return new Checks().getFileHash(filename);
    }	
	/**
     * Gets the last modified date for the switch file in milliseconds.
     * @return time in milliseconds for the last modified 
     * for the file.
     */

    public static long getFileDate() {

    	// method used to get the timestamp for a file, if it exists

        if (switchFile.exists()) {
            fileDate = switchFile.lastModified();
        } else {
            fileDate = 0;
        }
        return fileDate;
    }
	/**
     * Opens UDP port 10077 in broadcast mode.  Then it broadcasts 
	 * the message out to the subnet broadcast address.
     */

    public void sendMessage() {

        try {
            update("Start broadcast");
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            DatagramPacket message;
            InetAddress address = InetAddress.getByName(workingAddress);
            broadcastSocket.connect(address, PORT);
            update("Opened port to " + address.getHostAddress());
			update("hash " + securityChecks.toString());
			String fileDateStr = new Long(this.getFileDate()).toString();
            String rawMessage = fileDateStr + "," + this.getFileCRC();
 			rawMessage += securityChecks.getHostHashStr();
            byte[] sendBuff = (rawMessage.getBytes());
			update("broadcast size is " + sendBuff.length);
            message = new DatagramPacket(sendBuff, sendBuff.length);
            broadcastSocket.send(message);
            update("Sent message: " + rawMessage + " to " + workingAddress);
            broadcastSocket.disconnect();
            broadcastSocket.close();
        } catch (UnknownHostException uhe) {
            
        // its looking at localhost -?!
            update("Broadcast failure");
        } catch (SocketException se) {
            update("Broadcast failure");
        } catch (IOException ioe) {     
            update("Check cable or make sure wireless is turned on");             
        }
        update("End ");
    }
	/**
     * Opens UDP port 10077 in 
     * broadcast mode.  Then it broadcasts the message out 
	 * to the host address supplied as tempAddr.
	 * @param tempAddr the destination address of the host.
     */

    public void sendMessage(String tempAddr) {
	
		if (tempAddr == null || tempAddr.equals("")) {
		    workingAddress = DEF_IP_ADDR;
		} else {
		    workingAddress = tempAddr;
		}
		sendMessage();
    }
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */
	
	private void update(String message){
	
		if (debugger != null) {
			debugger.update(message);
		}
	}
}
