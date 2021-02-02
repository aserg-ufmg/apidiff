package apidiff.internal.analysis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import apidiff.APIDiff;
import apidiff.Change;
import apidiff.Result;
import apidiff.enums.Category;
import apidiff.enums.Classifier;
import apidiff.enums.ElementType;

class MethodDiffTest {
	
	private APIDiff diff;

	@BeforeEach
	void setUp() throws Exception {
		diff = new APIDiff("PhilJay/MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart.git");
		diff.setPath("./dataset");
		
	}

	@Test
	void testDetectPushDown() {
		
		Result result = diff.detectChangeAtCommit("f36ba8de48ecc748a498d87595597edfb7abb0e8", Classifier.API);
		
		List<Change> changes = result.getChangeMethod().stream()
			.filter(change -> Category.METHOD_PUSH_DOWN.equals(change.getCategory()))
			.filter(change -> "com.github.mikephil.charting.data.Entry".equals(change.getPath()))
			.filter(change -> "public float[] getVals()".equals(change.getElement()))
			.collect(Collectors.toList());
		
		assertEquals(changes.size(), 1, "The parser should detect a single push down operation");
		assertTrue(changes.get(0).isBreakingChange(), "The parser should detect a breaking change");
		assertFalse(changes.get(0).isDeprecated(), "The parser should not identify deprecated annotation");
		assertTrue(changes.get(0).containsJavadoc(), "The parser should identify JavaDoc annotation");
		assertEquals(changes.get(0).getElementType(), ElementType.METHOD, "The change should be at the method level");
	}

}
