package com.github.hyla.depvar

trait DependentVar[T] {

  def apply(): T

  def map[R](fun: T => R): DependentVar[R]
}
