/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import forms.InsurancePolicyNumberFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.insurancePolicyNumber

class InsurancePolicyNumberViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "insurance_policy_number"
  private val insuranceCompanyName = "test company"
  private val postCall = controllers.routes.BenefitsSecuredByInsuranceController.onSubmit(NormalMode, None)

  override val form = new InsurancePolicyNumberFormProvider()()

  def createView(companyName : Option[String] = None): () => HtmlFormat.Appendable = () =>
    insurancePolicyNumber(frontendAppConfig, form, NormalMode, companyName, None, postCall)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    insurancePolicyNumber(frontendAppConfig, form, NormalMode, Some(insuranceCompanyName), None, postCall)(fakeRequest, messages)

  "InsurancePolicyNumber view" must {

    behave like normalPage(createView(Some(insuranceCompanyName)), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", insuranceCompanyName))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, controllers.routes.InsurancePolicyNumberController.onSubmit(NormalMode, None).url,
      "policyNumber")

    behave like pageWithReturnLink(createView(Some(insuranceCompanyName)), getReturnLink)
  }

  "Insurance Policy Number view in change mode" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

  }
}