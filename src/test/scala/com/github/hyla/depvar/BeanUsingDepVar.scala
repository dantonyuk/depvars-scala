package com.github.hyla.depvar

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

object BeanUsingDepVar {

  case class InternalState(f1: String, f2: String, f3: String) {
    override def toString: String = s"$f1,$f2,$f3"
  }
}

@Component
class BeanUsingDepVar {

  import com.github.hyla.depvar.BeanUsingDepVar._

  @Autowired
  var dependentVarProvider: DependentVarProvider = _

  private[this] var state: DependentVar[InternalState] = _

  @PostConstruct
  def init() {
    state = dependentVarProvider.observeVars("first", "second", "third") map {
      case List(Some(v1), Some(v2), Some(v3)) => InternalState(v1, v2, v3)
    }
  }

  def getState = state().toString
}
