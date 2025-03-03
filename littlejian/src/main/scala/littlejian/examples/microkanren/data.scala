package littlejian.examples.microkanren

import littlejian._
import littlejian.data._
import littlejian.ext._

type MKData = (Unit | String | MKPair) | (MKVar | MKGoal | MKThunk | MKMap) | (MKRec | MKReg)

implicit val U$MKData: Unify[MKData] = U$Union[Unit, String, MKPair, MKVar, MKGoal, MKThunk, MKMap, MKRec, MKReg]

final case class MKVar(id: VarOr[Nat]) extends Product1[VarOr[Nat]]

implicit val U$MKVar: Unify[MKVar] = U$Product

final class MKPair(a: VarOr[MKData], b: VarOr[MKData]) extends Pair[MKData, MKData](a, b)

def cons(a: VarOr[MKData], b: VarOr[MKData]): MKPair = new MKPair(a, b)

implicit val U$MKPair: Unify[MKPair] = implicitly[Unify[Pair[MKData, MKData]]].asInstanceOf[Unify[MKPair]]

sealed trait MKMap

implicit val U$MKMap: Unify[MKMap] = U$Union[MKMapEmpty.type, MKMapCons].asInstanceOf[Unify[MKMap]]
implicit val U$VarOr$MKMap: Unify[VarOr[MKMap]] = U$VarOr(U$MKMap)

case object MKMapEmpty extends MKMap

implicit val U$MKMapEmpty: Unify[MKMapEmpty.type] = equalUnifier

final case class MKMapCons(key: VarOr[MKData], value: VarOr[MKData], tail: VarOr[MKMap]) extends MKMap with Product3[VarOr[MKData], VarOr[MKData], VarOr[MKMap]]

implicit val U$MKMapCons: Unify[MKMapCons] = U$Product

sealed trait MKThunkKind

object MKThunkKind {
  case object Top extends MKThunkKind

  case object Bind extends MKThunkKind

  case object MPlus extends MKThunkKind
}

implicit val U$MKThunkKind: Unify[MKThunkKind] = equalUnifier

final case class MKThunk(kind: VarOr[MKThunkKind], xs: VarOr[List[VarOr[MKData]]]) extends Product2[VarOr[MKThunkKind], VarOr[List[VarOr[MKData]]]]

implicit val U$MKThunk: Unify[MKThunk] = U$Product(U$VarOr(U$MKThunkKind), U$VarOr(U$List(U$VarOr(U$MKData))))

sealed trait MKGoal derives Unify

final case class MKGoalEq(u: VarOr[MKData], v: VarOr[MKData], env: VarOr[MKMap]) extends MKGoal derives Unify

final case class MKGoalCallFresh(f: VarOr[MKData], env: VarOr[MKMap]) extends MKGoal derives Unify

final case class MKGoalConj(g1: VarOr[MKData], g2: VarOr[MKData], env: VarOr[MKMap]) extends MKGoal derives Unify

final case class MKGoalDisj(g1: VarOr[MKData], g2: VarOr[MKData], env: VarOr[MKMap]) extends MKGoal derives Unify

final case class MKGoalTop(rand: VarOr[MKData], env: VarOr[MKMap]) extends MKGoal derives Unify

final case class MKRec(x: VarOr[MKData], exp: VarOr[MKData]) derives Unify

final case class MKReg(x: VarOr[MKData]) derives Unify

def list(xs: VarOr[MKData]*): VarOr[MKData] = xs.foldRight[VarOr[MKData]](())(cons)

def microo(x: VarOr[MKData], env: VarOr[MKMap]): Rel[MKData] = ???

def MKMapo(x: VarOr[MKData]): Rel[MKMap] = x.cast[MKMap]
