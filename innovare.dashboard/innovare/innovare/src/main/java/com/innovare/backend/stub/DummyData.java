package com.innovare.backend.stub;

import java.time.LocalDate;
import java.util.*;

import com.innovare.utils.Classification;

public class DummyData {
	
	private static final Map<Long, Classification> CLASSIFICATIONS = new HashMap<>();
	private static final Random random = new Random(1);

	private DummyData() {
	}

	public static void createClassifications() {
	}

	

	/* === CLASSIFICATION === */

	public static Collection<Classification> getClassifications() {
		return CLASSIFICATIONS.values();
	}

	private static Classification.Status getClassificationStatus() {
		return Classification.Status.values()[random
				.nextInt(Classification.Status.values().length)];
	}

	
	public static LocalDate getDate() {
		return LocalDate.now().minusDays(random.nextInt(20));
	}

	
}
