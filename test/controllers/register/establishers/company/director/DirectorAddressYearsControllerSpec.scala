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

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.company.director.DirectorAddressYearsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorAddressYearsId, DirectorDetailsId}
import models.person.PersonDetails
import models.{AddressYears, Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

//scalastyle:off magic.number

class DirectorAddressYearsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DirectorAddressYearsFormProvider()
  val form = formProvider()
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(10)
  val director = PersonDetails("first", Some("middle"), "last", LocalDate.now())

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): DirectorAddressYearsController =
    new DirectorAddressYearsController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private def viewAsString(form: Form[_] = form) =
    addressYears(
      frontendAppConfig,
      form,
      AddressYearsViewModel(
        routes.DirectorAddressYearsController.onSubmit(NormalMode, establisherIndex, directorIndex),
        Message("messages__director_address_years__title"),
        Message("messages__director_address_years__heading"),
        Message("messages__director_address_years__heading"),
        Some(director.fullName)
      ),
      None
    )(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString -> director
          )
        )
      )
    )
  )

  "CompanyDirectorAddressYears Controller" must {

    "return OK and the correct view for a GET" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            "director" -> Json.arr(
              Json.obj(
                DirectorAddressYearsId.toString -> AddressYears.options.head.value.toString,
                DirectorDetailsId.toString -> director
              )
            )
          )
        )
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to session expired from a GET when the index is invalid" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
