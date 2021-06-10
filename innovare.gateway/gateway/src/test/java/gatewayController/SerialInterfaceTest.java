package gatewayController;

import org.junit.jupiter.api.Test;

import com.fazecast.jSerialComm.SerialPort;

public class SerialInterfaceTest {
	
	
	@Test
	public void testSerialPort() {
		
		
		System.out.println("TEST SERIAL PORT: ");
		
		SerialPort[] ports=SerialPort.getCommPorts();
		for(int i=0; i<ports.length; i++) {
			System.out.println(ports[i].toString());
		}
		
		
	}
	
	

}
