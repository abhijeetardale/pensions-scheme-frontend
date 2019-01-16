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

import controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController
import forms.register.establishers.company.director.ConfirmDeleteDirectorFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.company.director.confirmDeleteDirector

class ConfirmDeleteDirectorViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "confirmDeleteDirector"

  val form = new ConfirmDeleteDirectorFormProvider()()

  private val directorName = "John Doe"
  private val postCall = ConfirmDeleteDirectorController.onSubmit(0, 0)

  private def createView() = () =>
    confirmDeleteDirector(frontendAppConfig, form, directorName, postCall)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmDeleteDirector(frontendAppConfig, form, directorName, postCall)(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", directorName))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url)

    behave like pageWithReturnLink(createView(), getReturnLink)
  }
}
