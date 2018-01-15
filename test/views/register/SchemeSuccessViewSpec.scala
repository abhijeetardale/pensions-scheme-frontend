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

import controllers.register.routes
import org.joda.time.LocalDate
import views.behaviours.ViewBehaviours
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.html.register.schemeSuccess

class SchemeSuccessViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "complete"

  val testScheme = "test scheme name"
  //TODO: Replace the harcoded application number to the actual application number
  def createView: () => HtmlFormat.Appendable = () => schemeSuccess(frontendAppConfig, Some(testScheme),
    LocalDate.now(), "1234")(fakeRequest, messages)

  "SchemeSuccess view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", testScheme),
      "email", "copy_1", "copy_2", "copy_3", "register_pensions_regulator", "register_vat")

    "have dynamic text for application number" in {
      Jsoup.parse(createView().toString()) must haveDynamicText("messages__complete__application_number_is", "1234")
    }

    "have link for register pensions regulator" in {
      Jsoup.parse(createView().toString()).select("a[id=register-pensions-regulator-link]") must haveLink(routes.SchemeSuccessController.onPageLoad().url)
    }

    "have link for complete register vat link" in {
      Jsoup.parse(createView().toString()).select("a[id=complete-register-vat-link]") must haveLink(routes.SchemeSuccessController.onPageLoad().url)
    }
    "have button link" in {
      Jsoup.parse(createView().toString()).select("a.button") must haveLink(routes.SchemeSuccessController.onPageLoad().url)
    }
  }
}