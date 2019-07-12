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

package views.register.establishers.company.director

import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.director.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeedDirectors"

  val postCall = controllers.register.establishers.company.routes.WhatYouWillNeedCompanyDetailsController.onSubmit(NormalMode, None, index=Index(0))

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeed(frontendAppConfig, Some("testScheme"), postCall, None)(fakeRequest, messages)

  val messageKeys = (1 to 8).map(num => s"_item$num").toList

  "WhatYouWillNeedCompanyDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"), messageKeys:_*)

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}
