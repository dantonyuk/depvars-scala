package com.github.hyla.depvar

import org.junit.{Assert, Test}

class MyTest {

  @Test
  def parseRepeaters(): Unit = {
    import fastparse.all._

    sealed trait Expr
    case class SExpr(value: String) extends Expr
    case class PExpr(times: Int, value: String) extends Expr

    object Parser {
      def expr: P[String] = P(letters | parens).rep map { _.mkString }
      def letters: P[String] = P(CharIn('a' to 'z')).rep(1).!
      def parens: P[String] = P(number ~ "[" ~ expr ~ "]").map { case (n, s) => s * n }
      def number: P[Int] = P(CharIn('0' to '9').rep(1).!.map(_.toInt))
    }

    Assert.assertEquals(123, Parser.number.parse("123").get.value)
    Assert.assertEquals("abc", Parser.letters.parse("abc").get.value)
    Assert.assertEquals("ababab", Parser.parens.parse("3[ab]").get.value)
    Assert.assertEquals("abcccdefgfgfgfgh", (Parser.expr ~ End).parse("ab3[c]de4[fg]h").get.value)
    Assert.assertEquals("abcdefgfgfgcdefgfgfgh", (Parser.expr ~ End).parse("ab2[cde3[fg]]h").get.value)
  }

  @Test
  def testMonoid(): Unit = {
    import language.higherKinds

    trait Monoid[A] {
      def identity: A
      def compose(x: A, y: A): A
    }

    object PlusMonoid extends Monoid[Int] {
      override def identity: Int = 0
      override def compose(x: Int, y: Int): Int = x + y
    }

    object ConcatMonoid extends Monoid[String] {
      override def identity: String = ""
      override def compose(x: String, y: String): String = x + y
    }

    trait Functor[F[_]] {
      def map[A, B](x: F[A])(f: A => B): F[B]
    }

    implicit object OptionFunctor extends Functor[Option] {
      override def map[A, B](x: Option[A])(f: A => B): Option[B] = x.map(f)
    }

    implicit object ListFunctor extends Functor[List] {
      override def map[A, B](x: List[A])(f: A => B): List[B] = x.map(f)
    }

    trait Applicative[F[_]] extends Functor[F] {
      def pure[A](a: A): F[A]
      def ap[A, B](x: F[A])(f: F[A => B]): F[B]

      override def map[A, B](x: F[A])(f: A => B): F[B] = ap(x)(pure(f))
    }

    implicit object ListApplicative extends Applicative[List] {
      override def pure[A](a: A): List[A] = Nil
      override def ap[A, B](xs: List[A])(fs: List[A => B]): List[B] = for (x <- xs; f <- fs) yield f(x)
    }

    trait Monad[F[_]] extends Applicative[F] {
      def flatten[A](x: F[F[A]]): F[A] = flatMap(x)(x => x)
      def flatMap[A, B](x: F[A])(f: A => F[B]): F[B] = flatten(map(x)(f))
      override def ap[A, B](xs: F[A])(fs: F[A => B]): F[B] = flatMap(xs)(x => map(fs)(f => f(x)))
      override def map[A, B](x: F[A])(f: A => B): F[B] = ap(x)(pure(f))
    }

    implicit object MaybeMonad extends Monad[Option] {
      override def pure[A](a: A): Option[A] = None

      override def flatten[A](x: Option[Option[A]]): Option[A] = x.flatten

      override def flatMap[A, B](x: Option[A])(f: A => Option[B]): Option[B] = x.flatMap(f)

      override def ap[A, B](xs: Option[A])(fs: Option[A => B]): Option[B] =
        (xs, fs) match {
          case (Some(x), Some(f)) => Some(f(x))
          case _ => None
        }

      override def map[A, B](x: Option[A])(f: A => B): Option[B] = x.map(f)
    }
  }

  @Test
  def test() {
    import io.circe.Json
    val jsonString: Json = Json.fromString("scala exercises")
    val jsonDouble: Option[Json] = Json.fromDouble(1)
    val jsonBoolean: Json = Json.fromBoolean(true)
    val fieldList = List(
      ("key1", Json.fromString("value1")),
      ("key2", Json.fromInt(1)))

    val jsonFromFields: Json = Json.fromFields(fieldList)
    println(jsonFromFields.noSpaces)

    val jsonArray: Json = Json.fromValues(List(
      Json.fromFields(List(("field1", Json.fromInt(1)))),
      Json.fromFields(List(
        ("field1", Json.fromInt(200)),
        ("field2", Json.fromString("Having circe in Scala Exercises is awesome"))
      ))
    ))

    def transformJson(jsonArray: Json): Json =
      jsonArray mapArray { _.init }

    println(transformJson(jsonArray).noSpaces)

    import cats.syntax.either._
    import io.circe._, io.circe.parser._

    val json: String = """
     {
     "id": "c730433b-082c-4984-9d66-855c243266f0",
     "name": "Foo",
     "counts": [1, 2, 3],
     "values": {
     "bar": true,
     "baz": 100.001,
     "qux": ["a", "b"]
     }
    } """


    val doc: Json = parse(json).getOrElse(Json.Null)
    println(doc.hcursor.downField("values").downField("qux").downArray.right.as[String])

    println(Json.fromFields(List(
      ("name", Json.fromString("sample json"))
      ,
      ("data", Json.fromFields(List(("done", Json.fromBoolean(false)))))
    )).noSpaces)

    Assert.assertEquals("{\"key\":\"value\"}",
      Json.fromFields(List(("key", Json.fromString("value"))))
        .noSpaces)

    Assert.assertEquals("{\"name\":\"sample json\",\"data\":{\"done\":false}}",
      Json.fromFields(List(
        ("name", Json.fromString("sample json"))
        ,
        ("data", Json.fromFields(List(("done", Json.fromBoolean(false)))))
      )).noSpaces
    )

    Assert.assertEquals("[{\"x\":1}]",
      Json.fromValues(List(Json.fromFields(List(("x", Json.fromInt(1))))))
        .noSpaces)
  }
}
