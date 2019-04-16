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

package controllers.register.establishers.company

import base.CSRFRequest
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.VatFormProvider
import models.{Index, NormalMode}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.EstablishersCompany
import utils.{FakeNavigator, Navigator}
import viewmodels.{Message, VatViewModel}
import views.html.vat

import scala.concurrent.Future

class CompanyVatControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import CompanyVatControllerSpec._
  "CompanyVatController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyVatController.onPageLoad(NormalMode, firstIndex, None))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe vat(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyVatController.onSubmit(NormalMode, firstIndex, None))
          .withFormUrlEncodedBody(("vat.hasVat", "true"), ("vat.vat", "123456789"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

  }

}
object CompanyVatControllerSpec extends CompanyVatControllerSpec {

  val form = new VatFormProvider()()
  val firstIndex = Index(0)

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = VatViewModel(
    routes.CompanyVatController.onSubmit(NormalMode, firstIndex, None),
    title = Message("messages__companyVat__title"),
    heading = Message("messages__companyVat__heading", "test company name"),
    hint = Message("messages__common__company_vat__hint", "test company name"),
    subHeading = None
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryEstablisherCompany),
      bind(classOf[Navigator]).qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersService].toInstance(FakeUserAnswersService)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}

