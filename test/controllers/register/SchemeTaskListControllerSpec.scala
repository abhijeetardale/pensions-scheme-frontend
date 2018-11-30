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

package controllers.register

import base.{JsonFileReader, SpecBase}
import controllers.ControllerSpecBase
import controllers.actions._
import models.NormalMode
import play.api.test.Helpers._
import viewmodels._
import views.html.schemeTaskList

class SchemeTaskListControllerSpec extends ControllerSpecBase {

  import SchemeTaskListControllerSpec._

  private val journeyTL = JourneyTaskList(expectedAboutSection, Seq.empty,
    Seq.empty, expectedWorkingKnowledgeSection, expectedDeclarationLink)

  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))

  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString(): String =
    schemeTaskList(
      frontendAppConfig, journeyTL
    )(fakeRequest, messages).toString()

  "SchemeTaskList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object SchemeTaskListControllerSpec extends SpecBase with JsonFileReader {
  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val expectedAboutSection = JourneyTaskListSection(
    Some(true),
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url),
    None)

  private val expectedWorkingKnowledgeSection = JourneyTaskListSection(
    Some(true),
    Link(messages("messages__schemeTaskList__working_knowledge_add_link"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url),
    None)

  private val expectedDeclarationLink = Some(Link(messages("messages__schemeTaskList__declaration_link"),
    controllers.register.routes.DeclarationController.onPageLoad().url))
}
