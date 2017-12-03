package com.github.hyla.depvar

import scala.collection.mutable

class DependentVarContext extends DependentVarProvider {

  private[this] val varValues = mutable.Map[String, String]()
  private[this] val primaryVars = mutable.Map[String, RegisteredVar[Option[String]]]()
  private[this] val dependentVars = mutable.Map[String, mutable.Set[RegisteredVar[_]]]()
  private[this] val varSources = mutable.Map[RegisteredVar[_], mutable.Set[String]]()

  def setValues(values: Map[String, String]) {
    varValues ++= values
    for (key <- values.keys) getPrimaryVar(key).invalidate()
    for (key <- values.keys; depVar <- dependentVars(key)) depVar.invalidate()
  }

  def setValue(entry: (String, String)): Unit = {
    setValues(Map(entry))
  }

  override def observeVars(varNames: String*): DependentVar[List[Option[String]]] = {
    val depVar = new RegisteredVar[List[Option[String]]](
      varNames.map(varName => getPrimaryVar(varName).apply()).toList)
    associateDepVarToPrimaryVars(depVar, varNames)
    depVar
  }

  private[this] def associateDepVarToPrimaryVars[T](depVar: RegisteredVar[T], varNames: Iterable[String]) {
    varNames.foreach(name => dependentVars.getOrElseUpdate(name, mutable.Set()) += depVar)
    varSources.getOrElseUpdate(depVar, mutable.Set()) ++= varNames
  }

  private[this] def getPrimaryVar(varName: String): RegisteredVar[Option[String]] =
    primaryVars.getOrElseUpdate(varName, {
      val primaryVar = new RegisteredVar[Option[String]](Option.apply(varValues(varName)))
      varSources.getOrElseUpdate(primaryVar, mutable.Set()) += varName
      primaryVar
    })

  private[this] class RegisteredVar[T](supplier: => T) extends DependentVar[T] {

    private[this] var cached: LazyHolder[T] = _
    invalidate()

    def invalidate() {
      cached = new LazyHolder[T](supplier)
    }

    override def apply(): T = cached.value

    override def map[R](fun: T => R): RegisteredVar[R] = {
      val depVar = new RegisteredVar(fun(this()))
      associateDepVarToPrimaryVars(depVar, varSources(this))
      depVar
    }
  }

  class LazyHolder[T](supplier: => T) {
    lazy val value: T = supplier
  }
}
