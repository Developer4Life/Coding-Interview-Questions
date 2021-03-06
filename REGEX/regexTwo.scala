// Part 1 about Regular Expression Matching
//==========================================
abstract class Rexp
case object ZERO extends Rexp
case object ONE extends Rexp
case class CHAR(c: Char) extends Rexp
case class ALT(r1: Rexp, r2: Rexp) extends Rexp
case class SEQ(r1: Rexp, r2: Rexp) extends Rexp
case class STAR(r: Rexp) extends Rexp
import scala.language.implicitConversions
import scala.language.reflectiveCalls
def charlist2rexp(s: List[Char]): Rexp = s match {
  case Nil => ONE
  case c :: Nil => CHAR(c)
  case c :: s => SEQ(CHAR(c), charlist2rexp(s))
}

implicit def string2rexp(s: String): Rexp = charlist2rexp(s.toList)
implicit def RexpOps(r: Rexp) = new {
  def |(s: Rexp) = ALT(r, s)
  def % = STAR(r)
  def ~(s: Rexp) = SEQ(r, s)
}

implicit def stringOps(s: String) = new {
  def |(r: Rexp) = ALT(s, r)
  def |(r: String) = ALT(s, r)
  def % = STAR(s)
  def ~(r: Rexp) = SEQ(s, r)
  def ~(r: String) = SEQ(s, r)
}

/** ( 1.a ) =>
  * This function checks whether a regular expression can match the empty string.
  * @param r - regular expression passed
  * @return true/false according to the string passed
  */
def nullable(r: Rexp): Boolean = r match {
  case ZERO => false
  case ONE => true
  case CHAR(d) => false
  case ALT(r1, r2) => nullable(r1) || nullable(r2)
  case SEQ(r1, r2) => nullable(r1) && nullable(r2)
  case STAR(b) => true
}

/** ( 1.b ) =>
  * This function calculates the derivative of a regular expression w.r.t. a character.
  * @param c - char is passed to calculate the derivative
  * @param r - regular expression passed
  * @return regex
  */
def der(c: Char, r: Rexp): Rexp = r match {
  case ZERO => ZERO
  case ONE => ZERO
  case CHAR(d) => if (c == d) ONE else ZERO
  case ALT(r1, r2) => ALT(der(c, r1), der(c, r2))
  case SEQ(r1, r2) => if (nullable(r1)) ALT(SEQ(der(c, r1), r2), der(c, r2)) else SEQ(der(c, r1), r2)
  case STAR(r) => SEQ(der(c, r), STAR(r))
}

/** ( 1.c ) =>
  * this function simplifies a regular expression; however it does not simplify inside STAR-regular expressions.
  * @param r - regular expression passed
  * @return regex
  */
def simp(r: Rexp): Rexp = r match {

  case SEQ(reg1, reg2) => (reg1, reg2) match {
    case _ if simp(reg1) == ZERO || simp(reg2) == ZERO => ZERO
    case _ if simp(reg2) == ONE => simp(reg1)
    case _ if reg1 == ONE => simp(reg2)
    case _ => SEQ(simp(reg1), simp(reg2))
  }
  case ALT(reg1, reg2) => (reg1, reg2) match {
    case _ if simp(reg2) == ZERO => simp(reg1)
    case _ if simp(reg1) == ZERO => simp(reg2)
    case _ if simp(reg1) == simp(reg2) => simp(reg1)
    case _ => ALT(simp(reg1), simp(reg2))
  }
  case _ => r
}

/** ( 1.d.a ) =>
  * This function calculates the derivative w.r.t. a string.
  * @param s - List[Char] is passed to check each char recursively
  * @param r - regular expression is passed to test a piece of char
  * @return regex
  */
def ders(s: List[Char], r: Rexp): Rexp = s match {
  case Nil => r
  case c :: cs => ders(cs, simp(der(c, simp(r))))
}

/** ( 1.d.b ) =>
  * This function checks whether the string matches the regular expression.
  * @param r - regular expression is passed to test a piece of char within string
  * @param s - String is passed to check each char recursively
  * @return true/false
  */
def matcher(r: Rexp, s: String): Boolean = {
  nullable(ders(s.toList, r))
}

/** ( 1.e ) =>
  * This function searches (from the left to right) in string s1 all the non-empty substring that match the
  * regular expression — these substring are assumed to be the longest substring matched by the regular expression and
  * assumed to be non-overlapping. All these substring in s1 are replaced by s2.
  */
def replace(r: Rexp, s1: String, s2: String): String = {
  def callBack(regex: Rexp, string1: String, string2: String, tuple: (String, Int, Int)): String = string1 match {
    case _ if string1.length != 0 =>  tuple match {
      case _ if tuple._2 > string1.length => if (tuple._3 > 0) callBack(regex, string1.drop (tuple._3), string2, (tuple._1 + string2, 1, 0) ) else callBack(regex, string1.drop (1), string2, (tuple._1 + string1(0), 1, 0) )
      case _ => if (matcher (regex, string1.substring (0, tuple._2) )) callBack(regex, string1, string2, (tuple._1, tuple._2 + 1, tuple._2) ) else callBack(regex, string1, string2, (tuple._1, tuple._2 + 1, tuple._3) )
    }
    case _ => tuple._1
  }
  callBack(r, s1, s2, ("", 1, 0))
}

// Part 2 about Regular Expression Matching
//==========================================
import scala.annotation.tailrec

/** ( 2.a ) =>
  * This function will iterate f n-times starting with the argument x.
  */
@tailrec
def iterT[A](n: Int, f: A => A, x: A): A = { if ( n == 0) x else iterT( n - 1 , f , f(x) ) }

/** ( 2.b ) =>
  * This function will iterate f n-times starting with the argument x.
  */
def size(r: Rexp) : Int = r match {
  case ZERO        => 1
  case ONE         => 1
  case CHAR(c)     => 1
  case ALT(r1,r2)  => 1 + size(r1) + size(r2)
  case SEQ(r1,r2)  => 1 + size(r1) + size(r2)
  case STAR(r)     => 1 + size(r)
}

/** ( 2.c ) =>
  * This function will iterate f n-times starting with the argument x.
  */
@tailrec
def fixpT[A](f: A => A, x: A): A = { if ( f( x ) == x ) x else fixpT( f , f( x ) ) }

//the Collatz function from CW 6 defined as fixpoint
def ctest(n: Long): Long = if (n == 1) 1 else if (n % 2 == 0) n / 2 else 3 * n + 1

// should all produce 1
fixpT(ctest, 97L)
fixpT(ctest, 871L)
fixpT(ctest, 77031L)

// the same function on strings using the regular expression
// matcher
def foo(s: String): String = {
  if (matcher("a", s)) "a" else
  if (matcher("aa" ~ STAR("aa"), s)) s.take(s.length / 2)
  else "a" ++ s * 3
}

// should all produce "a"
fixpT(foo, "a" * 97)
fixpT(foo, "a" * 871)
