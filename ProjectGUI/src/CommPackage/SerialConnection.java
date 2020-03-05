package CommPackage;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;

public class SerialConnection extends Thread {
    // this method will loop until a COM port is connected
    // after that, this thread will be suspended (killed)
    // since it has finished its job
    @Override
    public void run() {
        boolean isSerialPortConnected = false;
        
        while (!isSerialPortConnected) {
            boolean isSerialPortFound = false;

            CommPortIdentifier serialPortId = null;

            // get all connected ports
            Enumeration enumComm = CommPortIdentifier.getPortIdentifiers();

            // loop/iterate over each port
            while (enumComm.hasMoreElements())
            {
                // get port ID (similar to a handle)
                serialPortId = (CommPortIdentifier)enumComm.nextElement();

                // if port type is 'serial'
                if (serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)
                {
                    isSerialPortFound = true;

                    System.out.println("Serial<>: Found serial port @ " + serialPortId.getName());

                    // break when the 1st serial port is found
                    break;
                }
            }

            if (isSerialPortFound)
            {
                try
                {
                    isSerialPortConnected = new CommProtocol().connect(serialPortId);
                    if (isSerialPortConnected) {
                        System.out.println("Serial<>: COM port connected!");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Serial<>: ERROR: exception when calling SerialConnection().connect(...)");
                }
            }
            else {
                System.out.println("Serial<>: no serial port is connected");
            }
            
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Serial<>: ERROR: exception in sleep in connection loop");
            }
        }
        
        // we get here when a COM port is connected
        // stop this thread since its now useless
        this.stop();
    }
}
