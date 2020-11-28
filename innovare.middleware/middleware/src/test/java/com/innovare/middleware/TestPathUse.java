package com.innovare.middleware;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TestPathUse {
	
	
	@Test
	public void testPathUsed() {
		File f= new File(System.getProperty("user.home")+"/InnovareZip/");
		System.out.println(f.getAbsolutePath());
		f.mkdir();
	}
	

}
