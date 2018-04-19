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

package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.SchemeDetailsFormProvider
import models.NormalMode
import models.register.{SchemeDetails, SchemeType}
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.schemeDetails

class SchemeDetailsViewSpec extends QuestionViewBehaviours[SchemeDetails] {

  val messageKeyPrefix = "scheme_details"

  override val form = new SchemeDetailsFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => schemeDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    schemeDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "SchemeDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("messages_cya_secondary_header"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.SchemeDetailsController.onSubmit(NormalMode).url,
      "schemeName")
  }

  "SchemeDetails view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- SchemeType.options) {
          assertContainsRadioButton(doc, s"schemeType_type-${option.value}", "schemeType.type", option.value, isChecked = false)
        }
      }

      for (option <- SchemeType.options) {
        s"rendered with a value of '${option.value}'" must {
          s"have the '${option.value}' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("schemeType.type" -> s"${option.value}"))))
            assertContainsRadioButton(doc, s"schemeType_type-${option.value}", "schemeType.type", option.value, isChecked = true)

            for (unselectedOption <- SchemeType.options.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"schemeType_type-${unselectedOption.value}", "schemeType.type", unselectedOption.value, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when the other is selected" in {
        val expectedValue = "some value"
        val doc = asDocument(createViewUsingForm(form.bind(Map("schemeType.type" -> "Other", "schemeType.schemeTypeDetails" -> expectedValue))))
        doc must haveLabelAndValue("schemeType_schemeTypeDetails", messages("messages__scheme_details__type_other_more"), expectedValue)
      }
    }
  }
}