package CommPackage;

import java.io.OutputStream;

public class CommWriter implements Runnable {
    
    private OutputStream serialOutStream;
        
    public CommWriter(OutputStream out)
    {
        this.serialOutStream = out;
    }
        
    @Override
    public void run()
    {
        try {
            Thread.sleep(1000); // we should start writing after 'read' thread has started
        } catch (Exception e) {
            System.out.println("Writer<>: ERROR: exception in run() at initial delay");
        }

        while (true)
        {
            try {
                // we should work only if reader is not active
                if (!CommProtocol.isReaderActive) {
                    
                    // raise writer flag to block writer
                    CommProtocol.isWriterActive = true;

                    if (CommProtocol.isMotorAlive) { // if motor is alive/connected
                        // send state = querying
                        this.serialOutStream.write(0);

                        // give some time for the uC
                        Thread.sleep(5);

                        // send PWM and direction
                        this.serialOutStream.write(CommProtocol.pwmAndDir);

                        System.out.println("Writer<>: PWM and direction are sent!");
                    }
                    else { // if motor is disconnected
                        System.out.println("Writer<>: motor is not alive (dead)");
                    }

                    // reset writer flag to allow writer to run
                    CommProtocol.isWriterActive = false;
                }
                else {
                    System.out.println("Writer<>: waiting for Reader<> to finish");
                }
                
                Thread.sleep(1000);
            }
            catch (Exception e){
                // reset writer flag to allow writer to run
                CommProtocol.isWriterActive = false;

                System.out.println("Writer<>: ERROR: exception in main loop");
            }
        }
    }        
}
