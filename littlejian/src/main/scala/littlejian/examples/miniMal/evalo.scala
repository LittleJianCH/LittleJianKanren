package littlejian.examples.miniMal

import littlejian.data._
import littlejian._
import littlejian.ext._

def resolveLet(clauses: VarOr[Data], blockEnvId: VarOr[EnvId], envIn: VarOr[WholeEnv], counterIn: VarOr[EnvVar], counterOut: VarOr[EnvVar], envOut: VarOr[WholeEnv]): Goal = conde(
  (clauses === LList.empty) && (envIn === envOut),
  for {
    (id, v, tail) <- clauses.is[Data, Data, LList[Data]]((id: VarOr[Data], v: VarOr[Data], tail: VarOr[LList[Data]]) => id :: (v :: tail))
    id <- id.cast[String]
    counterOut0 <- fresh[EnvVar]
    envOut0 <- fresh[WholeEnv]
    evaled <- evalo(v, blockEnvId, envIn, counterIn, counterOut0, envOut0)
    envOut1 <- setEnv(blockEnvId, id, evaled, envOut0)
    _ <- resolveLet(tail.asInstanceOf[VarOr[Data]], blockEnvId, envOut1, counterOut0, counterOut, envOut)
  } yield ()
)

def evalo(ast: VarOr[Data], envId: VarOr[EnvId], envIn: VarOr[WholeEnv], counterIn: VarOr[EnvVar], counterOut: VarOr[EnvVar], envOut: VarOr[WholeEnv]): Rel[Data] = conde(
  for {
    id <- ast.cast[String]
    value <- envGet(envId, id, envIn)
    _ <- counterOut === counterIn && envOut === envIn
  } yield value,
  for {
    (id, a) <- ast.is[Data, Data](LList("def", _, _))
    ids <- id.cast[String]
    envOut0 <- fresh[WholeEnv]
    v <- evalo(a, envId, envIn, counterIn, counterOut, envOut0)
    _ <- setEnv(envId, ids, v, envOut0, envOut)
  } yield (),
  for {
    f <- ast.is[Data](LList("~", _))
    v <- evalo(f, envId, envIn, counterIn, counterOut, envOut)
  } yield Macro(v),
  for {
    v <- ast.is[Data](LList("`", _))
    _ <- counterIn === counterOut && envIn === envOut
  } yield v,
  for {
    (params, body) <- ast.is[Data, Data](LList("fn", _, _))
  } yield ???,
  for {
    (clauses, body) <- ast.is[Data, Data](LList("let", _, _))
  } yield ???,
  for {
    xs <- ast.is[LList[Data]]("do" :: _)
  } yield ???,
)