package fdtmc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FDTMC {

    public static final String INITIAL_LABEL = "initial";
    public static final String SUCCESS_LABEL = "success";
    public static final String ERROR_LABEL = "error";

	private Set<State> states;
    private State initialState;
    private State successState;
    private State errorState;
	private String variableName;
	private int index;
	public TransitionManager transitionManager;
	public InterfaceManager interfaceManager;


	public FDTMC() {
		states = new LinkedHashSet<State>();
		initialState = null;
		variableName = null;
		index = 0;
		transitionManager = new TransitionManager(this);
		interfaceManager = new InterfaceManager(this);
	}

	public Collection<State> getStates() {
		return states;
	}

	public void setVariableName(String name) {
		variableName = name;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getVariableIndex() {
		return index;
	}

	public State createState() {
		State temp = new State();
		temp.setVariableName(variableName);
		temp.setIndex(index);
		states.add(temp);
		transitionManager.getTransitionSystem().put(temp, null);
		if (index == 0)
			initialState = temp;
		index++;
		return temp;
	}

	public State createState(String label) {
		State temp = createState();
		temp.setLabel(label);
		return temp;
	}

    public State createInitialState() {
        State initial = createState();
        setInitialState(initial);
        return initial;
    }

    private void setInitialState(State initialState) {
        if (this.initialState != null) {
            this.initialState.setLabel(null);
        }
        this.initialState = initialState;
        initialState.setLabel(INITIAL_LABEL);
    }

    public State getInitialState() {
        return initialState;
    }

    public State createSuccessState() {
        State success = createState();
        setSuccessState(success);
        return success;
    }

    private void setSuccessState(State successState) {
        this.successState = successState;
        successState.setLabel(SUCCESS_LABEL);
    }

    public State getSuccessState() {
        return successState;
    }

    public State createErrorState() {
        State error = createState();
        setErrorState(error);
        return error;
    }

    private void setErrorState(State errorState) {
        this.errorState = errorState;
        errorState.setLabel(ERROR_LABEL);
    }

    public State getErrorState() {
        return errorState;
    }

	public Transition createTransition(State source, State target, String action, String reliability) {
	    return transitionManager.createTransition(source, target, action, reliability);
	}

	/**
	 * Creates an explicit interface to another FDTMC.
	 *
	 * An interface is an FDTMC fragment with 3 states (initial, success, and error)
	 * and 2 transitions (initial to success with probability {@code id} and initial
	 * to error with probability 1 - {@code id}).
	 *
	 * @param id Identifier of the FDTMC to be abstracted away.
	 * @param initial Initial state of the interface.
	 * @param success Success state of the interface.
	 * @param error Error state of the interface.
	 */
	public Interface createInterface(String id, State initial, State success, State error) {
	    return interfaceManager.createInterface(id, initial, success, error);
	}

	public State getStateByLabel(String label) {
		Iterator <State> it = states.iterator();
		while (it.hasNext()){
			State s = it.next();
			if (s.getLabel().equals(label))
				return s;
		}
		return null;
	}

	public Transition getTransitionByActionName(String action) {
		return transitionManager.getTransitionByActionName(action);
	}

	@Override
	public String toString() {
		String msg = new String();

		Set<State> tmpStates = transitionManager.getTransitionSystem().keySet();
		Iterator <State> itStates = tmpStates.iterator();
		while (itStates.hasNext()) {
			State temp = itStates.next();
			List<Transition> transitionList = transitionManager.getTransitionSystem().get(temp);
			if (transitionList != null) {
				Iterator <Transition> itTransitions = transitionList.iterator();
				while (itTransitions.hasNext()) {
					Transition t = itTransitions.next();
					String stateMsg = temp.getVariableName() + "=" + temp.getIndex() + ((temp.getLabel() != null) ? "(" + temp.getLabel() + ")" : "");
					String transitionNameProbabilityMsg = t.getActionName() + " / " + t.getProbability();
					String transitionTargetNameMsg = t.getTarget().getVariableName();
					String transitionTargetLabelMsg = t.getTarget().getIndex() + ((t.getTarget().getLabel() != null) ? "(" + t.getTarget().getLabel() + ")" : "");
					msg +=  stateMsg + " --- " + transitionNameProbabilityMsg + " ---> " + 
							transitionTargetNameMsg + "=" + transitionTargetLabelMsg +  "\n";
							
				}
			}
		}
		return msg;
	}

	/**
	 * Two FDTMCs are deemed equal whenever:
	 *     - their states are equal;
	 *     - their initial, success, and error states are equal;
	 *     - the transitions with concrete values are equal;
	 *     - the transitions with variable names have equal source and target states; and
	 *     - the abstracted interfaces are equal.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (obj != null && obj instanceof FDTMC) {
	        FDTMC other = (FDTMC) obj;
	        LinkedList<List<Interface>> thisInterfaces = new LinkedList<List<Interface>>(interfaceManager.getInterfaces().values());
            LinkedList<List<Interface>> otherInterfaces = new LinkedList<List<Interface>>(other.interfaceManager.getInterfaces().values());
            final boolean isState = states.equals(other.states);
            final boolean isInitialState = getInitialState().equals(other.getInitialState());
            final boolean isSuccessState = getSuccessState().equals(other.getSuccessState());
            final boolean isErrorState = getErrorState().equals(other.getErrorState());
            final boolean isTransitionSystem = transitionManager.getTransitionSystem().equals(other.transitionManager.getTransitionSystem());
            final boolean isInterface = thisInterfaces.equals(otherInterfaces);
            return isState
	                && isInitialState
	                && isSuccessState
	                && isErrorState
	                && isTransitionSystem
	                && isInterface;
	    }
	    return false;
	}

	@Override
    public int hashCode() {
        return states.hashCode() + transitionManager.getTransitionSystem().hashCode() + interfaceManager.getInterfaces().hashCode();
    }

    public Map<State, List<Transition>> getTransitions() {
		return transitionManager.getTransitionSystem();
	}

	/**
	 * Inlines the given FDTMCs whenever there is an interface corresponding
	 * to the string in the respective index.
	 *
	 * @param indexedModels
	 * @return a new FDTMC which represents this one with the ones specified
	 *         in {@code indexedModels} inlined.
	 */
    public FDTMC inline(Map<String, FDTMC> indexedModels) {
        FDTMC inlined = new FDTMC();
        Map<State, State> statesMapping = copyForInlining(inlined);

        for (Map.Entry<String, List<Interface>> entry: interfaceManager.getInterfaces().entrySet()) {
            String dependencyId = entry.getKey();
            if (indexedModels.containsKey(dependencyId)) {
                FDTMC fragment = indexedModels.get(dependencyId);
                for (Interface iface: entry.getValue()) {
                    inlined.interfaceManager.inlineInInterface(iface,
                                              fragment,
                                              statesMapping);
                }
            }
        }
        return inlined;
    }

    /**
     * Returns a copy of this FDTMC decorated with "presence transitions",
     * i.e., a new initial state with a transition to the original initial
     * state parameterized by the {@code presenceVariable} and a complement
     * transition ({@code 1 - presenceVariable}) to the success state
     * ("short-circuit").
     *
     * @param presenceVariable
     * @return
     */
    public FDTMC decoratedWithPresence(String presenceVariable) {
        FDTMC decorated = copy();

        State oldInitial = decorated.getInitialState();
        State newInitial = decorated.createInitialState();
        // Enter the original chain in case of presence
        decorated.createTransition(newInitial,
                                   oldInitial,
                                   "",
                                   presenceVariable);
        // Short-circuit in case of absence
        decorated.createTransition(newInitial,
                                   decorated.getSuccessState(),
                                   "",
                                   "1-"+presenceVariable);
        return decorated;
    }

    /**
     * Returns an FDTMC with a transition to {@code ifPresent} annotated by
     * {@code presenceVariable} and a complement one ({@code 1 - ifPresent})
     * to {@code ifAbsent}. Of course, {@code presenceVariable} is meant to
     * be resolved with a value of 0 or 1.
     *
     * The success states of both {@code ifPresent} and {@code ifAbsent} are
     * linked to a new success state.
     *
     * @param presenceVariable
     * @param ifPresent
     * @param ifAbsent
     * @return
     */
    public static FDTMC ifThenElse(String presenceVariable, FDTMC ifPresent, FDTMC ifAbsent) {
        // TODO Handle ifAbsent.
        return ifPresent.decoratedWithPresence(presenceVariable);
    }

    /**
     * Prepares {@code destination} FDTMC to be an inlined version of this one.
     * @param destination
     * @return a mapping from states in this FDTMC to the corresponding states
     *      in the copied one ({@code destination}).
     */
    private Map<State, State> copyForInlining(FDTMC destination) {
        destination.variableName = this.getVariableName();

        Map<State, State> statesMapping = destination.inlineStates(this);
        setCommonStates(destination, statesMapping);

        destination.transitionManager.inlineTransitions(this, statesMapping);
        return statesMapping;
    }

    /**
     * Copies this FDTMC.
     * @return a new FDTMC which is a copy of this one.
     */
    private FDTMC copy() {
        FDTMC copied = new FDTMC();
        copied.variableName = this.getVariableName();

        Map<State, State> statesMapping = copied.inlineStates(this);
        setCommonStates(copied, statesMapping);

        copied.transitionManager.inlineTransitions(this, statesMapping);
        copied.interfaceManager.inlineInterfaces(this, statesMapping);
        return copied;
    }

	private void setCommonStates(FDTMC copied, Map<State, State> statesMapping) {
		copied.setInitialState(statesMapping.get(this.getInitialState()));
        copied.setSuccessState(statesMapping.get(this.getSuccessState()));
        copied.setErrorState(statesMapping.get(this.getErrorState()));
	}

    /**
     * Inlines all states from {@code fdtmc} stripped of their labels.
     * @param fdtmc
     * @return
     */
    public Map<State, State> inlineStates(FDTMC fdtmc) {
        Map<State, State> statesOldToNew = new HashMap<State, State>();
        for (State state: fdtmc.getStates()) {
            State newState = this.createState();
            statesOldToNew.put(state, newState);
        }
        return statesOldToNew;
    }
    
    public Map<String, List<Interface>> getInterfaces(){
    	return interfaceManager.getInterfaces();
    }
}
