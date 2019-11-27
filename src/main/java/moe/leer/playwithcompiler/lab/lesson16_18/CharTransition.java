package moe.leer.playwithcompiler.lab.lesson16_18;

/**
 * @author leer
 * Created at 11/27/19 3:20 PM
 */
public class CharTransition extends Transition {

  protected CharSet condition = new CharSet();

  public CharTransition(int maxTimes) {
    super.maxTimes = maxTimes;
  }

  public CharTransition(CharSet condition) {
    this.condition = condition;
  }

  public CharTransition() {
  }

  @Override
  public boolean match(Object character) {
    if (!(character instanceof Character)) return false;
    if (isEpsilon()) return false;
    return condition.match((Character) character);
  }

  public static CharTransition epsilonTransition() {
    return new CharTransition();
  }

  @Override
  public boolean isEpsilon() {
    return condition.isEmpty();
  }

  @Override
  public String toString() {
    if (isEpsilon()) {
      return "ε";
    } else {
      CharSet charSet = condition;
      if (charSet.subSets != null && charSet.subSets.size() >= 10) {
        charSet = charSet.getShorterForm();
      }
      return charSet.toString();
    }
  }
}
