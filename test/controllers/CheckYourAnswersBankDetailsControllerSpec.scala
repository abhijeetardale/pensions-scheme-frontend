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

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.{BankAccountDetailsId, IsAboutBankDetailsCompleteId, SchemeNameId, UKBankAccountId}
import models.register._
import models.{BankAccountDetails, CheckMode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator, FakeSectionComplete}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersBankDetailsControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersBankDetailsControllerSpec._

  "CheckYourAnswersBankDetailsController Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SchemeTaskListController.onPageLoad().url)
        FakeSectionComplete.verify(IsAboutBankDetailsCompleteId, true)
      }
    }

  }
}

object CheckYourAnswersBankDetailsControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersBankDetailsController =
    new CheckYourAnswersBankDetailsController(
      frontendAppConfigWithHubEnabled,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      FakeSectionComplete
    )

  private val postUrl = routes.CheckYourAnswersBankDetailsController.onSubmit()

  val bankDetails = BankAccountDetails("test bank name", "test account name",
    SortCode("34", "45", "67"), "test account number")

  private val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      BankAccountDetailsId.toString -> Json.toJson(bankDetails),
      UKBankAccountId.toString -> true,
      SchemeNameId.toString -> "Test Scheme Name"
    ))
  )


  private val bankAccountSection = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("uKBankAccount.hns_checkYourAnswersLabel", "Test Scheme Name"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(controllers.routes.UKBankAccountController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__hns_uKBankAccount", "Test Scheme Name")
      ),
      AnswerRow(
        messages("uKBankDetails.hns_checkYourAnswersLabel", "Test Scheme Name"),
        Seq(bankDetails.bankName,
          bankDetails.accountName,
          s"${bankDetails.sortCode.first}-${bankDetails.sortCode.second}-${bankDetails.sortCode.third}",
          bankDetails.accountNumber),
        answerIsMessageKey = false,
        Some(controllers.routes.BankAccountDetailsController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__hns_uKBankDetails", "Test Scheme Name")
      )
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfigWithHubEnabled, Seq(bankAccountSection), postUrl, None)(fakeRequest, messages).toString

}


