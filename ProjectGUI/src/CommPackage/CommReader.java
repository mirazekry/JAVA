package CommPackage;

import java.io.InputStream;
import java.io.OutputStream;

public class CommReader implements Runnable{
    // low level I/O streams, used to send or receive data from uC
    private InputStream serialInStream;
    private OutputStream serialOutStream;
        
    public CommReader(InputStream in, OutputStream out)
    {
        this.serialInStream = in;
        this.serialOutStream = out;
    }
        
    @Override
    public void run ()
    {
        byte[] buffer = new byte[1];

        int len = -1;
           
        try {
            Thread.sleep(500); // we should start writing after 'read' thread has started
        } catch (Exception e) {
            System.out.println("Reader<>: ERROR: exception in run() at initial delay");
        }

        while (true)
        {
            try {
                // we should work only if writer is not active
                if (!CommProtocol.isWriterActive) {
                    // raise reader flag to block writer
                    CommProtocol.isReaderActive = true;

                    // send state = querying
                    this.serialOutStream.write(1);

                    // give some time for the uC
                    Thread.sleep(5);

                    // read response from uC
                    if (this.serialInStream.available() > 0) { // if a byte (or more) is available
                                                   // this method won't block thread when
                                                   // uC is not responding
                        len = this.serialInStream.read(buffer);
                        // if data received from uC is correct
                        if (buffer[0] == (byte)0xA5) { // NOTE: char cast is very important,
                                                       //       otherwise comparison will always be false
                            CommProtocol.isMotorAlive = true;
                            System.out.println("Reader<>: uC responded!");
                        }
                        else { // if data received from uC is wrong
                            CommProtocol.isMotorAlive = false;
                            System.out.println("Reader<>: uC didn't respond");
                        }
                    }
                    else // we didn't get data from uC
                    {
                        CommProtocol.isMotorAlive = false;
                        System.out.println("Reader<>: connection is lost");
                    }

                    // reset reader flag to allow writer to run
                    CommProtocol.isReaderActive = false;
                }
                else {
                    System.out.println("Reader<>: waiting for Writer<> to finish");
                }
                
                Thread.sleep(1000);
            }
            catch (Exception e) {
                CommProtocol.isMotorAlive = false;

                // reset reader flag to allow writer to run
                CommProtocol.isReaderActive = false;

                System.out.println("Reader<>: ERROR: exception in main loop");
            }
        }
    }
}
