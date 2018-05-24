package fdtmc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransitionManager {
	FDTMC fdtmc;
	private Map<State, List<Transition>> transitionSystem;
	
	public TransitionManager(FDTMC fdtmc) {
		this.fdtmc = fdtmc;
		transitionSystem = new LinkedHashMap<State, List<Transition>>();
	}
	
	public Transition createTransition(State source, State target, String action, String reliability) {
	    if (source == null) {
	        return null;
	    }

	    List<Transition> l = getTransition(source);

		Transition newTransition = new Transition(source, target, action, reliability);
		boolean success = l.add(newTransition);
		transitionSystem.put(source, l);
		return success ? newTransition : null;
	}

	private List<Transition> getTransition(State source) {
		List<Transition> l = transitionSystem.get(source);
		if (l == null) {
			l = new LinkedList<Transition>();
		}
		return l;
	}
	
	public Transition getTransitionByActionName(String action) {
		//para cada Lista de adjacencias de cada nodo
		Collection<List<Transition>> stateAdjacencies = transitionSystem.values();
		Iterator<List<Transition>> iteratorStateAdjacencies = stateAdjacencies.iterator();
		while (iteratorStateAdjacencies.hasNext()) {
			List<Transition> transitions = iteratorStateAdjacencies.next();
			Iterator <Transition> iteratorTransitions = transitions.iterator();
			//Percorrer a lista de transicoes e comparar os labels das transicoes
			while (iteratorTransitions.hasNext()) {
				Transition t = iteratorTransitions.next();
				if (t.getActionName().equals(action))
					return t;
			}
		}
		return null;
	}
	
	public Transition inlineTransition(Transition transition, Map<State, State> statesOldToNew) {
        State newSource = statesOldToNew.get(transition.getSource());
        State newTarget = statesOldToNew.get(transition.getTarget());
        return createTransition(newSource,
                                newTarget,
                                transition.getActionName(),
                                transition.getProbability());
    }
	
	/**
     * Inlines all transitions from {@code fdtmc} that are not part of an interface.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    public void inlineTransitions(FDTMC fdtmc, Map<State, State> statesOldToNew) {
        Set<Transition> interfaceTransitions = getInterfaceTransitions();
        for (Map.Entry<State, List<Transition>> entry : fdtmc.getTransitions().entrySet()) {
            List<Transition> transitions = entry.getValue();
            if (transitions != null) {
                for (Transition transition : transitions) {
                    if (!interfaceTransitions.contains(transition)) {
                        inlineTransition(transition, statesOldToNew);
                    }
                }
            }
        }
    }
    
    private Set<Transition> getInterfaceTransitions() {
        Set<Transition> transitions = new HashSet<Transition>();
        fdtmc.getInterfaces().values().stream().flatMap(List<Interface>::stream)
                .forEach(iface -> {
                    transitions.add(iface.getSuccessTransition());
                    transitions.add(iface.getErrorTransition());
                });
        return transitions;
    }
    
    public Map<State, List<Transition>> getTransitionSystem(){
    	return this.transitionSystem;
    }
}
