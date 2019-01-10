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

package controllers

import controllers.actions._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.whatYouWillNeedBenefitsInsurance

class WhatYouWillNeedBenefitsInsuranceControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.SessionExpiredController.onPageLoad

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedBenefitsInsuranceController =
    new WhatYouWillNeedBenefitsInsuranceController(frontendAppConfig,
      messagesApi,
      FakeAuthAction
    )

  def viewAsString(): String = whatYouWillNeedBenefitsInsurance(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeedBenefitsInsuranceController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to session redirect page" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}
