package com.github.hyla.depvar

import shapeless.{:+:, CNil, Coproduct, Generic, HList, Poly, Poly1}
import shapeless.syntax.std.tuple._
import shapeless.syntax.std.function._
import shapeless.ops.function._


trait DependentVarProvider {

  type Value = Option[String]

  def observeVars(varNames: String*): DependentVar[List[Option[String]]]

  def observeVar(varName: String, defaultValue: => String = null): DependentVar[String] =
    observeVars(varName).map(_.headOption.flatten.getOrElse(defaultValue))

  def observeOption(varName: String): DependentVar[Option[String]] =
    observeVars(varName).map(_.headOption.flatten)

  // java way, uses nulls
  def observeValues(varNames: String*): DependentVar[List[String]] =
    observeVars(varNames: _*).map(_.map(_.orNull))

  def observeV(varName1: String, varName2: String): DependentVar[(Value, Value)] = ???

//  def applyProduct[P <: Product, F, L <: HList, R](p: P)(f: F)(implicit gen: Generic.Aux[P, L], fp: FnToProduct.Aux[F, L => R]) =
//    f.toProduct(gen.to(p))
//
//  def observeTuples[F, P <: Product, L <: HList](p: P)(f: F) = {
//    p.map(observe)
//  }
//
//  val observe = new Poly1 {
//    implicit def caseString = at[String] { name => observeVar(name) }
//  }
//
//  {
//    val x = applyProduct(1, 2, 3) {
//      (x: String, y: Int, z: Int) => x + y + z
//    }
//  }
//
//  type ISB = Int :+: String :+: Boolean :+: CNil
//  val isb: ISB = Coproduct[ISB]("foo")
//
//  def foo(x: (Int, String)) = ???
//  foo(1, "x")
}
