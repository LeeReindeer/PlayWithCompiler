package moe.leer.playwithcompiler.lab.lesson16_18;

import java.util.LinkedList;
import java.util.List;

/**
 * @author leer
 * Created at 11/26/19 3:33 PM
 */
public class CharSet {
  // 字符集范围
  protected Character fromChar;

  protected Character toChar;

  // 是否取补集，[^a]
  protected boolean exclude = false;

  protected List<CharSet> subSets;

  //ascii表，也就是0到127
  public static List<Character> ascii = ascii();
  public static List<Character> letterAndDigits = letterAndDigits();

  //整个字母表
  public static List<Character> Alphabet = ascii;


  //////////////////
  // 一些常量
  public static CharSet digit = new CharSet('0', '9');        //数字
  public static CharSet smallLetter = new CharSet('a', 'z');   //小写字母
  public static CharSet capitalLeter = new CharSet('A', 'Z');  //大写字母
  public static CharSet letter = initLetter();                //字母，包括大写和小写
  public static CharSet letterOrDigit = initLetterOrDigit();  //字母和数字
  public static CharSet whiteSpace = initWhiteSpace();        //空白字符

  protected CharSet() {
  }

  protected CharSet(Character fromChar) {
    this(fromChar, fromChar, false);
  }

  protected CharSet(Character fromChar, Character toChar) {
    this(fromChar, toChar, false);
  }

  protected CharSet(Character fromChar, Character toChar, boolean exclude) {
    this.fromChar = fromChar;
    this.toChar = toChar;
    this.exclude = exclude;
  }

  protected void addSubSet(CharSet charSet) {
    if (subSets == null) {
      subSets = new LinkedList<CharSet>();
    }
    subSets.add(charSet);
  }

  public boolean match(Character ch) {
    boolean result = false;
    if (subSets != null) {
      for (CharSet subSet : subSets) {
        result = subSet.match(ch);
        if (result) break;
      }
    } else {
      result = (ch >= fromChar && ch <= toChar);
    }
    if (exclude) result = !result;
    return result;
  }

  /**
   * 返回一个等价的集合，但是显示的时候更简短。
   * 比如，a|b|...z就显示成[a-z]就行了。
   * @return
   */
  public CharSet getShorterForm(){
    if (this.equals(digit)){
      return digit;
    }
    else if (this.equals(smallLetter)){
      return smallLetter;
    }
    else if (this.equals(capitalLeter)){
      return capitalLeter;
    }
    else if (this.equals(letter)){
      return letter;
    }
    else if (this.equals(letterOrDigit)){
      return letterOrDigit;
    }
    else {
      CharSet charSet = getSupplementarySet();
      charSet.exclude = true;
      return charSet;
    }
  }

  //计算补集
  private CharSet getSupplementarySet(){
    CharSet charSet = new CharSet();
    //for (Character ch : Alphabet){
    for (Character ch : letterAndDigits){  //TODO 需要知道该词法更准确的字符集
      if (!match(ch)){
        charSet.addSubSet(new CharSet(ch));
      }
    }

    if (charSet.subSets.size() == 0){
      charSet = letterOrDigit;
    }

    return charSet;
  }

  @Override
  public String toString() {
    if (subSets != null){
      StringBuffer sb = new StringBuffer();
      //sb.append('(');
      if (exclude){
        sb.append('^');
        if (subSets.size()>1){
          sb.append('(');
        }
      }
      for (int i = 0; i< subSets.size(); i++){
        if (i > 0){
          sb.append("|");
        }
        sb.append(subSets.get(i));
      }
      if (exclude && subSets.size()>1){
        sb.append(')');
      }
      //sb.append(')');
      return sb.toString();
    }
    else if (fromChar == toChar) {
      return String.valueOf(fromChar);
    } else {
      if (exclude) {
        return "[^" + fromChar + "-" + toChar + "]";
      } else {
        return "[" + fromChar + "-" + toChar + "]";
      }
    }
  }

  @Override
  public boolean equals(Object obj){
    CharSet charSet = (CharSet)obj;
    for (Character ch: Alphabet){
      if (charSet.match(ch)){
        if (!this.match(ch)){
          return false;
        }
      }
      else{
        if (this.match(ch)){
          return false;
        }
      }
    }
    return true;
  }

  //是不是空集
  public boolean isEmpty(){
    if(subSets!= null){
      boolean empty = true;
      for (CharSet charSet : subSets){
        if (!charSet.isEmpty()){
          empty = false;
          break;
        }
      }
      return empty;
    }
    else{
      return fromChar == null;
    }
  }


  /**
   * 初始化字母表。目前支持整个ASCII表，128个值。
   *
   * @return
   */
  private static List<Character> ascii() {
    List<Character> Alphabet = new LinkedList<Character>();

    for (int i = 0; i < 128; i++) {
      Alphabet.add((char) i);
    }
    return Alphabet;
  }

  /**
   * 包含字母和数字的字母表
   *
   * @return
   */
  private static List<Character> letterAndDigits() {
    List<Character> Alphabet = new LinkedList<Character>();

    for (char i = '0'; i <= '9'; i++) {
      Alphabet.add((char) i);
    }

    for (char i = 'A'; i <= 'Z'; i++) {
      Alphabet.add((char) i);
    }

    for (char i = 'a'; i <= 'z'; i++) {
      Alphabet.add((char) i);
    }
    return Alphabet;
  }

  private static CharSet initLetterOrDigit() {
    CharSet charSet = new CharSet();
    charSet.addSubSet(digit);
    charSet.addSubSet(smallLetter);
    charSet.addSubSet(capitalLeter);
    return charSet;
  }

  private static CharSet initLetter() {
    CharSet charSet = new CharSet();
    charSet.addSubSet(smallLetter);
    charSet.addSubSet(capitalLeter);
    return charSet;
  }

  private static CharSet initWhiteSpace() {
    CharSet charSet = new CharSet();
    charSet.addSubSet(new CharSet(' '));
    charSet.addSubSet(new CharSet('\t'));
    charSet.addSubSet(new CharSet('\n'));
    return charSet;
  }

}
