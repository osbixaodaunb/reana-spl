package jadd;

import java.util.HashSet;
import java.util.Set;

import org.bridj.Pointer;

import bigcudd.BigcuddLibrary;

public class VariableGetter {
	public ADD add;
	public Set<String> variables;
	public Pointer<Integer> variablesPtr;
	public int numVars;
	public int[] variablesPresence;
	
	public VariableGetter(ADD add_) {
		this.add = add_;
	}
	
	public Set<String> getVariables() {
        variables = new HashSet<String>();

        variablesPtr = BigcuddLibrary.Cudd_SupportIndex(add.getDD(), this.add.getFunction());
        numVars = BigcuddLibrary.Cudd_ReadSize(add.getDD());
        variablesPresence = variablesPtr.getInts(numVars);
        fillVariables();
        
        return variables;
    }

	private void fillVariables() {
		for (short i = 0; i < numVars; i++) {
            if (variablesPresence[i] == 1) {
                variables.add(add.getVariableStore().getName(i));
            }
        }
	}
}
