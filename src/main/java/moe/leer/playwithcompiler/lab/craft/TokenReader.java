package moe.leer.playwithcompiler.lab.craft;

/**
 * @author leer
 * Created at 11/11/19 3:10 PM
 */
public interface TokenReader {
  /**
   * 返回Token流中下一个Token，并从流中取出。 如果流已经为空，返回null;
   */
  Token read();

  /**
   * 返回Token流中下一个Token，但不从流中取出。 如果流已经为空，返回null;
   */
  Token peek();

  /**
   * Token流回退一步。恢复原来的Token。
   */
  void unread();

  /**
   * @see #setPosition(int)
   */
  default void unread2(int pos) {
    setPosition(pos);
  }

  /**
   * 获取Token流当前的读取位置。
   *
   * @return
   */
  int getPosition();

  /**
   * 设置Token流当前的读取位置
   *
   * @param position
   */
  void setPosition(int position);
}
