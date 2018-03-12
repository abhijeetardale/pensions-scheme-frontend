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
import models.{Field, Required}
import models.register.establishers.company.CompanyPreviousAddress

class CompanyPreviousAddressFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new CompanyPreviousAddressFormProvider()()

  "CompanyPreviousAddress form" must {
    behave like questionForm(CompanyPreviousAddress("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "messages__companyPreviousAddress__error__field1_required"),
      Field("field2", Required -> "messages__companyPreviousAddress__error__field2_required")
    )
  }
}
