package gatewayController;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.fazecast.jSerialComm.SerialPort;
import com.innovare.control.SerialChannelReader;
import com.innovare.model.Sample;

public class SerialInterfaceTest {
	
	
	@Test
	public void testSerialPort() throws Exception {
		
		
		System.out.println("TEST SERIAL PORT: ");
		
		//SerialChannelReader scr= new SerialChannelReader();
		
		//ArrayList<Sample> sampls= scr.getMeasursFromSerial();
		
		
		//System.out.println(sampls.toString());
		String test= "Serial-Temp-0";
		String[] split = test.split("-");
		System.out.println(split[0]);
		System.out.println(split[1]);
		System.out.println(split[2]);
		
	}
	
	

}
