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
		
		Result result = diff
				.detectChangeAtCommit("f36ba8de", 
				Classifier.API);
		
		List<Change> changes = result.getChangeMethod().stream()
			.filter(change -> Category.METHOD_PUSH_DOWN
				.equals(change.getCategory()))
			.filter(change -> "com.github.mikephil.charting.data.Entry"
				.equals(change.getPath()))
			.filter(change -> "public float[] getVals()"
				.equals(change.getElement()))
			.collect(Collectors.toList());
		
		assertEquals(changes.size(), 1, 
				"There is no Push Down Method operation");
		assertTrue(changes.get(0).isBreakingChange(), 
				"Push Down Method is a breaking change");
		assertEquals(changes.get(0).getElementType(), 
				ElementType.METHOD, 
				"There is no change at the method level");
		assertFalse(changes.get(0).isDeprecated(), 
				"There is no deprecated annotation");
		assertTrue(changes.get(0).containsJavadoc(), 
				"There is JavaDoc annotation");
		
	}
	
	@Test
	void testDetectPullUp() {
		Result result = diff.detectChangeAtCommit("2b886a46", Classifier.API);
		
		List<Change> changes = result.getChangeMethod().stream()
			.filter(change -> 
				Category.METHOD_PULL_UP.equals(change.getCategory()))
			.filter(change -> 
				"com.github.mikephil.charting.charts.BarLineChartBase"
				.equals(change.getPath()))
			.filter(change -> 
				"public Highlight getHighlightByTouchPoint(float x, float y)"
				.equals(change.getElement()))
			.collect(Collectors.toList());
		
		assertEquals(changes.size(), 1, 
				"There is no Pull Up Method operation");
		assertFalse(changes.get(0).isBreakingChange(), 
				"Pull Up Method operation is not breaking change");
		assertEquals(changes.get(0).getElementType(), 
				ElementType.METHOD, 
				"There is no change at the method level");
		assertFalse(changes.get(0).isDeprecated(), 
				"There is no deprecated annotation");
		assertTrue(changes.get(0).containsJavadoc(), 
				"There is JavaDoc annotation");
		
	}
	
	@Test
	void testDetectRename() {
		Result result = diff.detectChangeAtCommit("4b00e834", Classifier.API);

		List<Change> changes = result.getChangeMethod().stream()
			.filter(change -> Category.METHOD_RENAME
					.equals(change.getCategory()))
			.filter(change -> "com.github.mikephil.charting.highlight.ChartHighlighter"
				.equals(change.getPath()))
			.filter(change -> "protected SelectionDetail getDetail(IDataSet set, int dataSetIndex, float xVal, DataSet.Rounding rounding)"
				.equals(change.getElement()))
			.collect(Collectors.toList());
		
		assertEquals(changes.size(), 1, 
				"There is no Rename Method operation");
		assertTrue(changes.get(0).isBreakingChange(), 
				"Rename Method operation is breaking change");
		assertEquals(changes.get(0).getElementType(), 
				ElementType.METHOD, 
				"There is no change at the method level");
		assertFalse(changes.get(0).isDeprecated(), 
				"There is no deprecated annotation");
		assertTrue(changes.get(0).containsJavadoc(), 
				"There is JavaDoc annotation");
		
	}
	
	@Test
	void testDetectInline() {
		Result result = diff.detectChangeAtCommit("4ab4c0ba", Classifier.API);

		List<Change> changes = result.getChangeMethod().stream()
			.filter(change -> Category.METHOD_INLINE
					.equals(change.getCategory()))
			.filter(change -> "com.xxmassdeveloper.mpchartexample.BarChartActivity"
				.equals(change.getPath()))
			.filter(change -> "protected int getLayout()"
				.equals(change.getElement()))
			.collect(Collectors.toList());
		
		assertEquals(changes.size(), 1, 
				"There is no Inline Method operation");
		assertTrue(changes.get(0).isBreakingChange(), 
				"Inline Method operation is breaking change");
		assertEquals(changes.get(0).getElementType(), 
				ElementType.METHOD, 
				"There is no change at the method level");
		assertFalse(changes.get(0).isDeprecated(), 
				"There is no deprecated annotation");
		assertFalse(changes.get(0).containsJavadoc(), 
				"There is no JavaDoc annotation");
	}

}
