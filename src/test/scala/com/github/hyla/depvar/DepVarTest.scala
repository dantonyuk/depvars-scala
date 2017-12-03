package com.github.hyla.depvar

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@ContextConfiguration(classes = Array(classOf[DepVarConfig]))
@RunWith(classOf[SpringJUnit4ClassRunner])
class DepVarTest {

  @Autowired
  var dependentVarContext: DependentVarContext = _

  @Autowired
  var beanUsingDepVar: BeanUsingDepVar = _

  @Test
  def testValues() {
    dependentVarContext.setValues(
      Map("first" -> "1", "second" -> "2", "third" -> "3"))

    assertEquals("1,2,3", beanUsingDepVar.getState)
    assertEquals("1,2,3", beanUsingDepVar.getState)

    dependentVarContext.setValues(
      Map("first" -> "4", "second" -> "5", "third" -> "6"))

    assertEquals("4,5,6", beanUsingDepVar.getState)
  }

  @Test
  def testCallNumbers() {
    var callCount = 0
    val state = dependentVarContext
        .observeValues("first", "second", "third")
        .map {
          case List(v1, v2, v3) => (v1.toInt, v2.toInt, v3.toInt)
        }
        .map {
          case (v1, v2, v3) =>
            callCount += 1
            v1 + v2 + v3
        }

    dependentVarContext.setValues(
      Map("first" -> "1", "second" -> "2", "third" -> "3"))

    assertEquals(0, callCount)
    assertEquals(6, state())
    assertEquals(1, callCount)

    dependentVarContext.setValues(
      Map("first" -> "4", "second" -> "5", "third" -> "6"))

    assertEquals(15, state())
    assertEquals(2, callCount)

    state()
    state()
    state()

    // no more calls to supplier
    assertEquals(2, callCount)
  }
}
