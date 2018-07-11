/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import generators.Generators
import org.joda.time.LocalDate
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.{Form, FormError}
import utils.Enumerable
import wolfendale.scalacheck.regexp.RegexpGen

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends WordSpec with MustMatchers with OptionValues with Mappings with GeneratorDrivenPropertyChecks with Generators {

  import MappingsSpec._

  "text" must {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "trim spaces from a valid string" in {
      val gen = RegexpGen.from("""^\s+test\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe "test"
      }
    }

    "minimise internal spaces" in {
      val gen = RegexpGen.from("""^start\s+middle\s+end$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe "start middle end"
      }
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind a string containing only spaces" in {
      val gen = RegexpGen.from("""^\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.errors must contain(FormError("value", "error.required"))
      }
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "return a custom error message" in {
      val form = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "optionalText" must {

    val testForm: Form[Option[String]] =
      Form(
        "value" -> optionalText()
      )

    "bind a valid string" in {
      forAll(stringsLongerThan(0)) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some(s)
      }
    }

    "trim spaces from a valid string" in {
      val gen = RegexpGen.from("""^\s+test\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some("test")
      }
    }

    "minimise internal spaces" in {
      val gen = RegexpGen.from("""^start\s+middle\s+end$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some("start middle end")
      }
    }

    "bind an empty string to None" in {
      val result = testForm.bind(Map("value" -> ""))
      result.get mustBe None
    }

    "bind a string containing only spaces to None" in {
      val gen = RegexpGen.from("""^\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe None
      }
    }

    "bind an empty map to None" in {
      val result = testForm.bind(Map.empty[String, String])
      result.get mustBe None
    }

    "unbind some valid value" in {
      forAll(Gen.alphaStr) { s =>
        val result = testForm.fill(Some(s))
        result.apply("value").value mustBe Some(s)
      }
    }

    "unbind None to an empty string" in {
      val result = testForm.fill(None)
      result.apply("value").value mustBe Some("")
    }

  }

  "boolean" must {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" must {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" must {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "date" must {
    val testForm: Form[LocalDate] = Form("date"->dateMapping("messages__error__date"))

    "bind a valid date" in {
      val result = testForm.bind(Map("date.day" -> "1", "date.month" -> "5", "date.year" -> LocalDate.now().getYear.toString))
      result.get mustEqual new LocalDate(LocalDate.now().getYear, 5, 1)
    }

    "not bind an invalid Date" in {
      val result = testForm.bind(Map("date.day" -> "31", "date.month" -> "2", "date.year" -> LocalDate.now().getYear.toString))
      result.errors mustEqual Seq(FormError("date", "messages__error__date"))
    }

    "not bind an empty Map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors mustEqual Seq(FormError("date.day", "messages__error__date"),
        FormError("date.month", "messages__error__date"), FormError("date.year", "messages__error__date"))
    }

    "unbind a valid date" in {
      val result = testForm.fill(new LocalDate(LocalDate.now().getYear, 6, 1))
      result.apply("date.day").value.value mustEqual "1"
      result.apply("date.month").value.value mustEqual "6"
      result.apply("date.year").value.value mustEqual LocalDate.now().getYear.toString
    }
  }
}
