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
		modelFile = File.createTempFile("model", "param");
		writeFile(modelString, modelFile);
		propertyFile = File.createTempFile("property", "prop");
		writeFile(property, propertyFile);
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
		
	private void writeFile(String modelString, File file) throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(modelString);
		fileWriter.flush();
		fileWriter.close();
	}
	
	
}
