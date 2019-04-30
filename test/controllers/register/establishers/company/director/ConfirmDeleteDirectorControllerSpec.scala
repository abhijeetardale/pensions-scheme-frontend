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

package controllers.register.establishers.company.director

import services.FakeUserAnswersService
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import forms.register.establishers.company.director.ConfirmDeleteDirectorFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import models.person.PersonDetails
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, FakeSectionComplete}
import views.html.register.establishers.company.director.confirmDeleteDirector

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteDirectorControllerSpec._

  "ConfirmDeleteDirector Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onPageLoad(establisherIndex, directorIndex, NormalMode, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the director was already deleted" in {
      val data = new FakeDataRetrievalAction(Some(testData(directorDeleted)))
      val result = controller(data).onPageLoad(establisherIndex, directorIndex, NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AlreadyDeletedController.onPageLoad(establisherIndex, directorIndex, None).url)
    }

    "delete the director on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(DirectorDetailsId(establisherIndex, directorIndex), directorDetails.copy(isDeleted = true))
    }

    "dont delete the director on a POST if No selected" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, None)(postRequestForCancel)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(DirectorDetailsId(establisherIndex, directorIndex))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, directorIndex, NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(establisherIndex, directorIndex, NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "set the establisher as not complete when directors are deleted" in {
      FakeSectionComplete.reset()
      val validData: JsObject = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(companyName),
            "director" -> Json.arr(
              Json.obj(
                DirectorDetailsId.toString ->
                  PersonDetails("John", None, "Doe", LocalDate.now(), true)
              )
            )
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(establisherIndex, directorIndex, NormalMode, None)(postRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.userAnswer.get(IsEstablisherCompleteId(0)) mustBe Some(false)
    }
  }

}

object ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val companyName = "MyCo Ltd"
  private val directorName = "John Doe"
  private lazy val postCall = routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex, NormalMode, None)
  private lazy val cancelCall = AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex)
  private val directorDetails = PersonDetails("John", None, "Doe", LocalDate.now(), false)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))
  private val postRequestForCancel: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private def testData(directors: PersonDetails = directorDetails) = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              directors
          )
        )
      )
    )
  )

  val directorDeleted: PersonDetails = directorDetails.copy(isDeleted = true)

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new ConfirmDeleteDirectorFormProvider()
  private val form = formProvider.apply()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteDirectorController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeSectionComplete,
      formProvider
    )

  private def viewAsString() = confirmDeleteDirector(
    frontendAppConfig,
    form,
    directorName,
    postCall,
    None
  )(fakeRequest, messages).toString

}
