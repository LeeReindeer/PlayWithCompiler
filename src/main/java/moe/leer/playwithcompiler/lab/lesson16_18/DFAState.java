package moe.leer.playwithcompiler.lab.lesson16_18;

import java.util.Collections;
import java.util.Set;

/**
 * @author leer
 * Created at 11/27/19 8:14 PM
 */
public class DFAState extends State {
  //  DFA状态包含的NFA状态集合
  private Set<State> NFAStates;

  public DFAState(Set<State> NFAStates) {
    this.NFAStates = NFAStates;
  }

  protected Set<State> states() {
    return Collections.unmodifiableSet(NFAStates);
  }

  /**
   * 提供一个对象作为迁移条件，看能否迁移到下一个状态
   *
   * @param obj，做词法分析
   * @return
   */
  protected DFAState getNextState(Object obj) {
    for (Transition transition : transitions()) {
      if (transition.match(obj)) {
        return (DFAState) getState(transition);
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder rtn = new StringBuilder(super.toString());
    rtn.append("\tNFA states: ");
    int i = 0;
    for (State state : NFAStates) {
      if (i++ > 0) {
        rtn.append(", ");
      }
      rtn.append(state.getName());
      if (state.getGrammarNode() != null && state.getGrammarNode().isNamedNode()) {
        rtn.append("(").append(state.getGrammarNode().getName()).append(")");
      }
    }
    rtn.append("\n");
    return rtn.toString();
  }

  @Override
  public boolean isAcceptable() {
    return NFAStates.stream().anyMatch(State::isAcceptable);
  }
}
