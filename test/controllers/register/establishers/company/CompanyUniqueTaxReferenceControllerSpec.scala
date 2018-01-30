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

package controllers.register.establishers.company

import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import forms.register.establishers.company.CompanyUniqueTaxReferenceFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models._
import views.html.register.establishers.company.companyUniqueTaxReference
import controllers.ControllerSpecBase
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.individual.UniqueTaxReferenceId
import models.register.{SchemeDetails, SchemeType}
import models.register.establishers.individual.UniqueTaxReference
import play.api.mvc.Call

class CompanyUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val firstIndex = Index(0)
  val formProvider = new CompanyUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val companyName = "test company name"



  val validData = Json.obj(
    SchemeDetails.toString ->
      SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test company name", Some("123456"), Some("abcd")),
        CompanyUniqueTaxReferenceId.toString ->
          UniqueTaxReference.Yes("1234567891")
      ),
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test", Some("654321"), Some("dcba"))
      )
    )
  )


  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): CompanyUniqueTaxReferenceController =
    new CompanyUniqueTaxReferenceController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = companyUniqueTaxReference(frontendAppConfig, form, NormalMode, firstIndex,
    companyName)(fakeRequest, messages).toString

  "CompanyUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when company name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when company name is not present" in {
      val result = controller(getEmptyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired page when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, Index(2))(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
