package CommPackage;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;

public class CommProtocol {
    // PWM and direction, this is sent to the uC and received from the GUI
    public static byte pwmAndDir = 0;
    
    // indicator for the motor (connection) state
    public static boolean isMotorAlive = false;
    
    // semaphores to synchronize between reader and writer
    public static boolean isReaderActive = false;
    public static boolean isWriterActive = false;
    
    public CommProtocol()
    {
        super(); // construction of parent class
    }
    
    public boolean connect(CommPortIdentifier serialPortId) throws Exception
    {
        // if port is in-use
        if (serialPortId.isCurrentlyOwned())
        {
            System.out.println("Protocol<>: port is currently in use");
            
            return false;
        }
        else // if port is not in-use (if it's free)
        {
            // open port for communication
            CommPort commPort = serialPortId.open(this.getClass().getName(), 2000);
            
            // if opened port has a type 'SerialPort'
            if (commPort instanceof SerialPort)
            {
                SerialPort serialPort = (SerialPort)commPort;
                
                // UART configuration: 8N1 (8-bit data, no parity, 1 stop bit)
                serialPort.setSerialPortParams(9600,
                                               SerialPort.DATABITS_8,
                                               SerialPort.STOPBITS_1,
                                               SerialPort.PARITY_NONE);
                
                // get the low level I/O streams of the serial port
                InputStream serialIn = serialPort.getInputStream();
                OutputStream serialOut = serialPort.getOutputStream();
                
                // pass these I/O streams to the reader and the writer
                CommReader readSerial = new CommReader(serialIn, serialOut);
                CommWriter writeSerial = new CommWriter(serialOut);
                
                // start reader and writer threads
                new Thread(readSerial).start();
                new Thread(writeSerial).start();
                
                System.out.println("Protocol<>: reader and writer threads are started");
                
                return true;
            }
            else // if opened port has a different type than othan 'SerialPort'
            {
                System.out.println("Protocol<>: only serial ports are handled");
                
                return false;
            }
        }
    }
}
