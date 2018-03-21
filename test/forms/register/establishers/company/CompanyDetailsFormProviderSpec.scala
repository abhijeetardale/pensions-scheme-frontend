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

import forms.behaviours.{PayeBehaviours, StringFieldBehaviours, VatBehaviours}
import forms.mappings.Constraints
import org.scalatest.OptionValues
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen
class CompanyDetailsFormProviderSpec extends StringFieldBehaviours with Constraints with OptionValues with PayeBehaviours with VatBehaviours {

  val form = new CompanyDetailsFormProvider()()

  ".companyName" must {

    val fieldName = "companyName"
    val requiredKey = "messages__error__company_name"
    val lengthKey = "messages__error__company_name_length"
    val invalidKey = "messages__error__company_name_invalid"
    val maxLength = CompanyDetailsFormProvider.companyNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(safeTextRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "[invalid]",
      error = FormError(fieldName, invalidKey, Seq(safeTextRegex))
    )
  }

  ".vatRegistrationNumber" must {
    val fieldName = "vatNumber"
    val keyVatLength = "messages__error__vat_length"
    val keyVatInvalid = "messages__error__vat_invalid"

    behave like formWithVatField(
      form,
      fieldName,
      keyVatLength,
      keyVatInvalid
    )

  }

  ".payeEmployerReferenceNumber" must {

    val fieldName = "payeNumber"
    val keyPayeLength = "messages__company__paye_error_length"
    val keyPayeInvalid = "messages__company__paye_error_invalid"

    behave like formWithPayeField(
      form,
      fieldName,
      keyPayeLength,
      keyPayeInvalid
    )

  }

}
