package moe.leer.playwithcompiler.lab.lesson16_18;

import java.util.*;

/**
 * @author leer
 * Created at 11/27/19 3:00 PM
 */
public class State {
  private static int count = 0;

  // 状态名，为自增数字
  private String name;

  private boolean acceptable;


  //到其他状态的转移条件
  private List<Transition> transitions = new LinkedList<Transition>();

  //转移条件 -> 状态
  private Map<Transition, State> transition2State = new HashMap<Transition, State>();

  //跟这个节点关联的语法
  //用于词法分析时，分辨是哪个词法的结束状态
  private GrammarNode grammarNode = null;


  public State() {
    name = String.valueOf(count++);
  }

  public State(boolean acceptable) {
    this();
    this.acceptable = acceptable;
  }

  public void addTransition(Transition transition, State toState) {
    transitions.add(transition);
    transition2State.put(transition, toState);
  }

  /**
   * 把另一个状态的连线全部拷贝成自己的。
   * 这相当于把State这个节点替换成自己
   *
   * @param state
   */
  public void copyTransitions(State state) {
    this.transitions = state.transitions;
    this.transition2State = state.transition2State;
  }

  public State getState(Transition transition) {
    return transition2State.get(transition);
  }


  /**
   * 获得到某个状态的Transition。  //TODO 这里假设每个两个状态之间只可能有一个Transition
   *
   * @param toState
   * @return
   */
  public Transition getTransitionTo(State toState) {
    for (Transition transition : transitions) {
      if (transition2State.get(transition) == toState) {
        return transition;
      }
    }
    return null;
  }

  public List<Transition> transitions() {
    return Collections.unmodifiableList(transitions);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(name);
    if (transitions.size() > 0) {
      for (Transition transition : transitions) {
        State state = transition2State.get(transition);
        sb.append("\t").append("--").append(transition).append("-").append("->\t").append(state.name).append('\n');
      }
    } else {
      sb.append("\t(end)").append('\n');
    }

    if (isAcceptable()) {
      sb.append("\tacceptable\n");
    }
    return sb.toString();
  }

  public void dump() {
    dump(this, new HashSet<>());
  }

  private static void dump(State state, Set<State> dumpedState) {
    System.out.println(state);
    dumpedState.add(state);
    for (Transition transition : state.transitions()) {
      State next = state.getState(transition);
      if (!dumpedState.contains(next)) {
        dump(next, dumpedState);
      }
    }
  }

  public GrammarNode getGrammarNode() {
    return grammarNode;
  }

  public void setGrammarNode(GrammarNode grammarNode) {
    this.grammarNode = grammarNode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAcceptable() {
    return acceptable;
  }

  public void setAcceptable(boolean acceptable) {
    this.acceptable = acceptable;
  }
}
