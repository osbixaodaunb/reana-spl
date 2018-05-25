package fdtmc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InterfaceManager {
	FDTMC fdtmc;
	private Map<String, List<Interface>> interfaces;
	
	public InterfaceManager(FDTMC fdtmc) {
		this.fdtmc = fdtmc;
		interfaces = new LinkedHashMap<String, List<Interface>>();
	}
	
	public Interface createInterface(String id, State initial, State success, State error) {
	    Transition successTransition = fdtmc.createTransition(initial, success, "", id);
	    Transition errorTransition = fdtmc.createTransition(initial, error, "", "1 - " + id);
	    Interface newInterface = new Interface(id,
	                                           initial,
	                                           success,
	                                           error,
	                                           successTransition,
	                                           errorTransition);

	    List<Interface> interfaceOccurrences = null;
	    if (interfaces.containsKey(id)) {
	        interfaceOccurrences = interfaces.get(id);
	    } else {
	        interfaceOccurrences = new LinkedList<Interface>();
	        interfaces.put(id, interfaceOccurrences);
	    }
	    interfaceOccurrences.add(newInterface);
	    return newInterface;
	}
    /**
     * Inlines all interfaces (and respective transitions) from {@code fdtmc}
     * into this one.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    public void inlineInterfaces(FDTMC fdtmc, Map<State, State> statesOldToNew) {
        for (Map.Entry<String, List<Interface>> entry : fdtmc.getInterfaces().entrySet()) {
            List<Interface> newInterfaces = new LinkedList<Interface>();
            interfaces.put(entry.getKey(), newInterfaces);
            for (Interface iface : entry.getValue()) {
                Transition successTransition = fdtmc.transitionManager.inlineTransition(iface.getSuccessTransition(), statesOldToNew);
                Transition errorTransition = fdtmc.transitionManager.inlineTransition(iface.getErrorTransition(), statesOldToNew);
                Interface newInterface = new Interface(iface.getAbstractedId(),
                                                       statesOldToNew.get(iface.getInitial()),
                                                       statesOldToNew.get(iface.getSuccess()),
                                                       statesOldToNew.get(iface.getError()),
                                                       successTransition,
                                                       errorTransition);
                newInterfaces.add(newInterface);
            }
        }
    }
    
    public void inlineInInterface(Interface iface, FDTMC fragment, Map<State, State> statesMapping) {
        Map<State, State> fragmentStatesMapping = this.fdtmc.inlineStates(fragment);
        this.fdtmc.transitionManager.inlineTransitions(fragment, fragmentStatesMapping);

        State initialInlined = iface.getInitial();
        State initialFragment = fragment.getInitialState();
        State successInlined = iface.getSuccess();
        State successFragment = fragment.getSuccessState();
        State errorInlined = iface.getError();
        State errorFragment = fragment.getErrorState();

        this.fdtmc.createTransition(statesMapping.get(initialInlined),
                              fragmentStatesMapping.get(initialFragment),
                              "",
                              "1");
        this.fdtmc.createTransition(fragmentStatesMapping.get(successFragment),
                              statesMapping.get(successInlined),
                              "",
                              "1");
        if (errorFragment != null) {
        	this.fdtmc.createTransition(fragmentStatesMapping.get(errorFragment),
                                  statesMapping.get(errorInlined),
                                  "",
                                  "1");
        }
    }

	
	public Map<String, List<Interface>> getInterfaces(){
		return this.interfaces;
	}
}
