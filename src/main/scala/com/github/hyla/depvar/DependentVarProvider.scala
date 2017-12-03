package com.github.hyla.depvar

trait DependentVarProvider {

  def observeVars(varNames: String*): DependentVar[List[Option[String]]]

  def observeVar(varName: String, defaultValue: => String = null): DependentVar[String] =
    observeVars(varName).map(_.headOption.flatten.getOrElse(defaultValue))

  def observeOption(varName: String): DependentVar[Option[String]] =
    observeVars(varName).map(_.headOption.flatten)

  // java way, uses nulls
  def observeValues(varNames: String*): DependentVar[List[String]] =
    observeVars(varNames: _*).map(_.map(_.orNull))
}
