package moe.leer.playwithcompiler.lab.lesson16_18;

/**
 * @author leer
 * Created at 11/27/19 3:01 PM
 */
public abstract class Transition {
  //对于重复的情况，最多可以重复几次。
  //这是把GrammarNode中的maxTimes属性转义到这里来了。
  //对于 ？maxTimes = 1，对于+和*，maxTimes=-1
  protected int maxTimes = 1;

  public abstract boolean match(Object object);

  /**
   * 是否是一个Epsilon转换
   * @return
   */
  public abstract boolean isEpsilon();

  public int getMaxTimes() {
    return maxTimes;
  }

  public void setMaxTimes(int maxTimes) {
    this.maxTimes = maxTimes;
  }
}
