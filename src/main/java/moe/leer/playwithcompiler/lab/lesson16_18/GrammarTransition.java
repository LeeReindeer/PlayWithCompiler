package moe.leer.playwithcompiler.lab.lesson16_18;

/**
 * @author leer
 * Created at 11/27/19 3:03 PM
 */
public class GrammarTransition extends Transition {
  // state transfer condition
  protected String condition;


  public GrammarTransition(int maxTimes) {
    super.maxTimes = maxTimes;
  }

  public GrammarTransition() {
  }

  public GrammarTransition(String condition) {
    this.condition = condition;
  }

  @Override
  public boolean match(Object grammarName) {
    if (!(grammarName instanceof String)) return false;
    if (isEpsilon()) return false;
    return condition.equals(grammarName);
  }

  public boolean isEpsilon(){
    return condition==null;
  }

  @Override
  public String toString(){
    if (isEpsilon()){
      return "ε";
    }
    else{
      return condition;
    }
  }
}
