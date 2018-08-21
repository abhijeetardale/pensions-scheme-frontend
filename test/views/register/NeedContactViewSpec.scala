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

import forms.register.NeedContactFormProvider
import play.api.data.Form
import views.behaviours.StringViewBehaviours
import views.html.register.needContact

class NeedContactViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "needContact"

  val form = new NeedContactFormProvider()()

  private def createView = () => needContact(frontendAppConfig, form)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[String]) => needContact(frontendAppConfig, form)(fakeRequest, messages)

  "NeedContact view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

    behave like stringPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = controllers.register.routes.NeedContactController.onSubmit.url,
      label = Some("messages__common__email"),
      id = "email")
  }
}