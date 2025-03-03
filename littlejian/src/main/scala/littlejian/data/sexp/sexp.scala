package littlejian.data.sexp

import littlejian.*
import littlejian.data._
import littlejian.ext.*

import scala.annotation.tailrec

type SExp = Cons | Unit | String

object SExp {
  def parse(s: String): SExp = {
    val (rest, r) = doParse(s)
    val s2 = rest.strip()
    if(s2.isEmpty) r else throw new Exception(s"parse error: remaining $s2")
  }

  private def doParse(s: String): (String, SExp) = {
    val (s1, s2) = s.strip().span(x=> x.isLetterOrDigit || x == '_' || x == '-')
    if(s1.nonEmpty) return (s2, s1)
    if(s2.isEmpty) throw new Exception("Invalid SExp")
    val c = s2.head
    val s3 = s2.tail
    c match {
      case '(' => {
        val xs = Seq.newBuilder[SExp]
        var rest = s3
        while(!(rest.isEmpty || rest.head == ')' || rest.head == '.')){
          val (s4, x) = doParse(rest)
          xs += x
          rest = s4.strip()
        }
        rest.head match {
          case ')' => (rest.tail, convertList(xs.result()))
          case '.' => {
            val (s4, x) = doParse(rest.tail)
            val s5 = s4.strip()
            if(s5.head != ')') throw new Exception("Invalid SExp")
            (s5.tail, convertListDot(xs.result(), x))
          }
        }
      }
      case '\'' => {
        val (s4, x) = doParse(s3)
        (s4, list("quote", x))
      }
    }
  }
}

implicit val U$SExp: Unify[SExp] = U$Union[Cons, Unit, String]

implicit val I$SExp: Inspect[SExp] = I$Union(I$Cons, I$Unit, I$String)

final class Cons(a: VarOr[SExp], d: VarOr[SExp]) extends Pair[SExp, SExp](a, d) {
}
implicit val U$Cons: Unify[Cons] = implicitly[Unify[Pair[SExp, SExp]]].asInstanceOf[Unify[Cons]]

implicit val I$Cons: Inspect[Cons] = implicitly[Inspect[Pair[SExp, SExp]]].asInstanceOf[Inspect[Cons]]

def cons(a: VarOr[SExp], d: VarOr[SExp]): SExp = Cons(a, d)
def car(x: VarOr[SExp]): VarOr[SExp] = x match {
  case Pair(v, _) => v
  case _ => throw new IllegalArgumentException("car: not a cons cell")
}

private def convertList(xs: Seq[VarOr[SExp]]): SExp = if (xs.isEmpty) () else cons(xs.head, convertList(xs.tail))
private def convertListDot(xs: Seq[VarOr[SExp]]): VarOr[SExp] = {
  val head = xs.head
  val tail = xs.tail
  if(tail.isEmpty) head else cons(head, convertListDot(tail))
}
private def convertListDot(xs: Seq[VarOr[SExp]], x: SExp): SExp = convertListDot(xs :+ x).asInstanceOf[SExp]
object list {
  def apply(xs: VarOr[SExp]*): SExp = convertList(xs)
  def unapplySeq(x: VarOr[SExp]): Option[Seq[VarOr[SExp]]] = x match {
    case Pair(a, d) => unapplySeq(d).map(a +: _)
    case () => Some(Seq())
    case _ => None
  }
}
def listDot(xs: VarOr[SExp]*) = convertListDot(xs)

def mapo(f: VarOr[SExp] => Rel[SExp], xs: VarOr[SExp]): Rel[SExp] = conde(
  begin(xs === (), ()),
  for {
    (head, tail) <- xs.is(cons)
    result <- cons call(f(head), mapo(f, tail))
  } yield result
)
