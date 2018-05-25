package paramwrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Formula {
	private ParamWrapper paramWrapper;
	boolean usePrism;
	File modelFile, propertyFile, resultsFile;
	
	public Formula(ParamWrapper paramWrapper) {
		this.paramWrapper = paramWrapper;
	}
	
	public String getCurrentFormula(String modelString, String property, ParamModel model) throws IOException {
		modelFile = writeModelFile(modelString);

		propertyFile = writePropertyFile(property);

		resultsFile = File.createTempFile("result", null);

		String formula;
		if (paramWrapper.getPrism() && !modelString.contains("const")) {
		    formula = paramWrapper.invokeModelChecker(modelFile.getAbsolutePath(),
		                                 propertyFile.getAbsolutePath(),
		                                 resultsFile.getAbsolutePath());
		} else if(usePrism) {
		    formula = paramWrapper.invokeParametricPRISM(model,
		                                    modelFile.getAbsolutePath(),
		                                    propertyFile.getAbsolutePath(),
		                                    resultsFile.getAbsolutePath());
		} else {
		    formula = paramWrapper.invokeParametricModelChecker(modelFile.getAbsolutePath(),
		                                           propertyFile.getAbsolutePath(),
		                                           resultsFile.getAbsolutePath());
		}
		return formula;
	}
	
	private File writePropertyFile(String property) throws IOException {
		File propertyFile = File.createTempFile("property", "prop");
		FileWriter propertyWriter = new FileWriter(propertyFile);
		propertyWriter.write(property);
		propertyWriter.flush();
		propertyWriter.close();
		return propertyFile;
	}

	private File writeModelFile(String modelString) throws IOException {
		File modelFile = File.createTempFile("model", "param");
		FileWriter modelWriter = new FileWriter(modelFile);
		modelWriter.write(modelString);
		modelWriter.flush();
		modelWriter.close();
		return modelFile;
	}
	
	
}
