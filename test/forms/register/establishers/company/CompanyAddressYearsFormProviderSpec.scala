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

package forms.register.establishers.company

import forms.behaviours.FormBehaviours
import models.register.establishers.company.CompanyAddressYears
import models.{Field, Invalid, Required}

class CompanyAddressYearsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "value" -> CompanyAddressYears.options.head.value
  )

  val form = new AddressYearsFormProvider()()

  "CompanyAddressYears form" must {

    behave like questionForm[CompanyAddressYears](CompanyAddressYears.values.head)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> "messages__error__selection",
        Invalid -> "error.invalid"),
      CompanyAddressYears.options.map(_.value): _*)
  }
}