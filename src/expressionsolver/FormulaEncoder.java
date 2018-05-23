package expressionsolver;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;

import jadd.ADD;

public class FormulaEncoder {
	private static final Logger LOGGER = Logger.getLogger(FormulaEncoder.class.getName());
	public ExpressionSolver expSolver;
	public JEP parser;
	public SymbolTable symbolTable;
	public Set<String> variables;
	
	public FormulaEncoder(ExpressionSolver expSolver_) {
		this.expSolver = expSolver_;
	}

	public ADD encodeFormula(String formula){
		parser = expSolver.makeADDParser(expSolver.getJADD());
	    parser.parseExpression(formula);
	    if (parser.hasError()) {
	        LOGGER.warning("Parser error: " + parser.getErrorInfo());
	        return null;
	    }
	
	    parser.addVariableAsObject("true", expSolver.getJADD().makeConstant(1));
	    parser.addVariableAsObject("True", expSolver.getJADD().makeConstant(1));
	    parser.addVariableAsObject("false", expSolver.getJADD().makeConstant(0));
	    parser.addVariableAsObject("False", expSolver.getJADD().makeConstant(0));
	    symbolTable = parser.getSymbolTable();
	    
	    this.variables = new HashSet<String>(symbolTable.keySet());
	    this.variables.remove("true");
	    this.variables.remove("True");
	    this.variables.remove("false");
	    this.variables.remove("False");
	
	    for (Object var : this.variables) {
	        String varName = (String) var;
	        ADD variable = expSolver.getJADD().getVariable(varName);
	        parser.addVariableAsObject(varName, variable);
	    }
	    return (ADD) parser.getValueAsObject();
	}
}
