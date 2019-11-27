package moe.leer.playwithcompiler.lab.lesson16_18;

import java.util.*;

/**
 * @author leer
 * Created at 11/26/19 4:00 PM
 */
public class Regex {
  public static void main(String[] args) {
//    GrammarNode rootNode1 = sampleGrammar1();
//    rootNode1.dump();
    GrammarNode rootNode2 = sampleGrammar2();
    rootNode2.dump();
    State[] states = regexToNFA(rootNode2);
    states[0].dump();

    //转换成DFA
    System.out.println("\nNFA to DFA:");
    List<DFAState> dfaStates = NFAtoDFA(states[0], CharSet.letterAndDigits);
    dfaStates.get(0).dump();
//    matchWithNFA(states[0], "abc");
//    matchWithNFA(states[0], "d");
  }

  /**
   * @param node root node
   * @return begin state and end state
   */
  public static State[] regexToNFA(GrammarNode node) {
    State beginState = null;
    State endState = null;

    switch (node.getType()) {
      case Or -> {
        beginState = new State();
        endState = new State(true);
        for (GrammarNode child : node.children()) {
          State[] childState = regexToNFA(child);
          // beginState -> childState[0] -> .... -> childState[1] -> endState
          beginState.addTransition(CharTransition.epsilonTransition(), childState[0]);
          childState[1].addTransition(CharTransition.epsilonTransition(), endState);
          childState[1].setAcceptable(false);
        }
      }
      case And -> {
        State[] lastState = null;
        for (int i = 0; i < node.getChildCount(); i++) {
          State[] childState = regexToNFA(node.getChild(i));
          if (lastState != null) {
            // lastState[1] -> childState[0]
            lastState[1].copyTransitions(childState[0]);
            lastState[1].setAcceptable(false);
          }
          lastState = childState;
          if (i == 0) {
            beginState = childState[0];
            endState = childState[1];
          } else {
            endState = childState[1];
          }
        }
      }
      case Char -> {
        beginState = new State();
        endState = new State(true);
        // beginState --charset->
        beginState.addTransition(new CharTransition(node.getCharSet()), endState);
      }
    }
    State[] result = null;
    if (node.getMinTimes() != 1 || node.getMaxTimes() != 1) {
      result = repeat(beginState, endState, node);
    } else {
      result = new State[]{beginState, endState};
    }

    //为命了名的语法节点做标记，后面将用来设置Token类型。
    if (node.getName() != null) {
      result[1].setGrammarNode(node);
    }
    return result;
  }


  /**
   * repeat ?, *, +
   *
   * @return
   */
  private static State[] repeat(State from, State to, GrammarNode node) {
    State begin = null;
    State end = null;

    // loop
    if (node.getMaxTimes() == -1 || node.getMaxTimes() > 1) {
      to.addTransition(new CharTransition(node.getMaxTimes()), from);
    }

    if (node.getMinTimes() == 0) {
      begin = new State();
      end = new State(true);
      // begin -> from
      begin.addTransition(CharTransition.epsilonTransition(), from);
      to.addTransition(CharTransition.epsilonTransition(), end);
      to.setAcceptable(false);

      begin.addTransition(CharTransition.epsilonTransition(), end);
    } else {
      begin = from;
      end = to;
    }
    return new State[]{begin, end};
  }

  public static boolean matchWithNFA(State state, String string) {
    char[] chars = string.toCharArray();
    int index = matchWithNFA(state, chars, 0);
    boolean matched = index == chars.length;
    System.out.println("matched? " + matched);
    return matched;
  }

  public static List<DFAState> NFAtoDFA(State startState, List<Character> charsets) {
    List<DFAState> dfaStates = new LinkedList<>();
    Deque<DFAState> workList = new LinkedList<>();

    // 避免重复计算
    Map<State, Set<State>> calculatedClosures = new HashMap<State, Set<State>>();

    DFAState s0 = new DFAState(calcClosure(startState, calculatedClosures));
    dfaStates.add(s0);
    workList.add(s0);


    while (!workList.isEmpty()) {
      DFAState q = workList.removeLast();
      for (Character ch : charsets) {
        Set<State> nextStateSet = move(q.states(), ch);
        if (nextStateSet.isEmpty()) continue;

        calcClosure(nextStateSet, calculatedClosures);

        DFAState dfaState = findDFAState(dfaStates, nextStateSet);
        Transition transition = null;
        if (dfaState == null) {
          dfaState = new DFAState(nextStateSet);
          dfaStates.add(dfaState);
          workList.addLast(dfaState);
          transition = new CharTransition(new CharSet(ch));
          // q -ch-> dfaState
          q.addTransition(transition, dfaState);
        } else {
          transition = dfaState.getTransitionTo(dfaState);
          if (transition == null) {
            transition = new CharTransition(new CharSet(ch));
            q.addTransition(transition, dfaState);
          }
        }
      }
    }

    return dfaStates;
  }

  /**
   * 计算从某个状态集合，在接收某个字符以后，会迁移到哪些新的集合
   * 不包括空集转移
   *
   * @param states
   * @param ch
   * @return
   */
  private static Set<State> move(Set<State> states, Character ch) {
    Set<State> rtn = new HashSet<State>();
    for (State state : states) {
      for (Transition transition : state.transitions()) {
        if (transition.match(ch)) {
          State nextState = state.getState(transition);
          rtn.add(nextState);
        }
      }
    }
    return rtn;
  }

  //根据NFA State集合，查找是否已经存在一个DFAState，包含同样的NFA状态集合
  private static DFAState findDFAState(List<DFAState> dfaStates, Set<State> states) {
    DFAState dfaState = null;
    for (DFAState dfaState1 : dfaStates) {
      if (sameStateSet(dfaState1.states(), states)) {
        dfaState = dfaState1;
        break;
      }
    }
    return dfaState;
  }

  //比较两个NFA state的集合是否相等
  private static boolean sameStateSet(Set<State> stateSet1, Set<State> stateSet2) {
    if (stateSet1.size() != stateSet2.size())
      return false;
    else {
      return stateSet1.containsAll(stateSet2);
    }
  }

  /**
   * 计算一个状态集合的闭包，包括这些状态以及可以通过epsilon到达的状态。
   *
   * @param states
   * @param calculatedClosures
   */
  private static void calcClosure(Set<State> states, Map<State, Set<State>> calculatedClosures) {
    Set<State> newStates = new HashSet<State>();
    for (State state : states) {
      Set<State> closure = calcClosure(state, calculatedClosures);
      newStates.addAll(closure);
    }

    states.addAll(newStates);
  }

  // DP
  private static Set<State> calcClosure(State state, Map<State, Set<State>> savedClosures) {
    if (savedClosures.containsKey(state)) {
      return savedClosures.get(state);
    }
    Set<State> closure = new HashSet<>();
    closure.add(state); // add self
    for (Transition transition : state.transitions()) {
      if (transition.isEpsilon()) {
        State next = state.getState(transition);
        closure.add(next);
        closure.addAll(calcClosure(next, savedClosures));
      }
    }
    return closure;
  }

  /**
   * 用NFA来匹配字符串
   *
   * @param state      当前所在的状态
   * @param chars      要匹配的字符串，用数组表示
   * @param matchedPos 当前匹配字符开始的位置。
   * @return 匹配后，新index的位置。指向匹配成功的字符的下一个字符。
   */
  private static int matchWithNFA(State state, char[] chars, int matchedPos) {
    System.out.println("trying state : " + state.getName() + ", index =" + matchedPos);
    int index2 = matchedPos;
    for (Transition transition : state.transitions()) {
      State next = state.getState(transition);
      // 先匹配epsilon，让*,+匹配尽可能多的字符
      if (transition.isEpsilon()) {
        // 下一个状态继续匹配当前字符
        index2 = matchWithNFA(next, chars, matchedPos);
        if (index2 == chars.length) {
          break;
        }
      } else if (transition.match(chars[matchedPos])) {
        index2++;
        if (index2 < chars.length) {
          // 下一个状态匹配下一个字符
          index2 = matchWithNFA(next, chars, matchedPos + 1);
        } else {
          if (acceptable(next)) {
            break;
          } else {
            index2 = -1;
          }
        }
      }

    }
    return index2;
  }

  /**
   * 查找当前状态是不是一个接受状态，或者可以通过epsilon迁移到一个接受状态。
   *
   * @param state
   * @return
   */
  private static boolean acceptable(State state) {
    if (state.isAcceptable()) {
      return true;
    }
    boolean result = false;

    for (Transition transition : state.transitions()) {
      if (transition.isEpsilon()) {
        State next = state.getState(transition);
        if (next.isAcceptable()) {
          result = true;
          break;
        } else {
          result = acceptable(next);
          if (result) break;
        }
      }
    }

    return result;
  }

  /**
   * 创建一个示例用的正则表达式：
   * int | [a-zA-z][a-zA-Z0-9]* | [0-9]*
   *
   * @return
   */
  private static GrammarNode sampleGrammar1() {
    GrammarNode node = new GrammarNode("regex1", GrammarNodeType.Or);

    //int关键字
    GrammarNode intNode = node.createChild("int", GrammarNodeType.And);
    intNode.createChild(new CharSet('i'));
    intNode.createChild(new CharSet('n'));
    intNode.createChild(new CharSet('t'));

    //标识符
    GrammarNode idNode = node.createChild("id", GrammarNodeType.And);
    GrammarNode firstLetter = idNode.createChild(CharSet.letter);

    GrammarNode letterOrDigit = idNode.createChild(CharSet.letterOrDigit);
    letterOrDigit.setRepeatTimes(0, -1);


    //数字字面量
    GrammarNode literalNode = node.createChild("digit", CharSet.digit);
    literalNode.setRepeatTimes(0, -1);

    return node;
  }

  /**
   * a(b|c)*
   *
   * @return
   */
  private static GrammarNode sampleGrammar2() {
    GrammarNode node = new GrammarNode("regex2", GrammarNodeType.And);
    GrammarNode aNode = node.createChild("a", new CharSet('a'));
    GrammarNode orNode = node.createChild("bOrc", GrammarNodeType.Or);
    orNode.setRepeatTimes(0, -1);
    orNode.createChild(new CharSet('b'));
    orNode.createChild(new CharSet('c'));
    return node;
  }
}
