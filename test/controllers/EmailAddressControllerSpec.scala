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

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.EmailFormProvider
import identifiers.TypedIdentifier
import models.CheckUpdateMode
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.EmailAddressViewModel
import views.html.emailAddress

import scala.concurrent.Future

class EmailAddressControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import EmailAddressControllerSpec._

  val viewmodel = EmailAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("legend")
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[EmailFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual emailAddress(appConfig, formProvider(), viewmodel, None)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[EmailFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)("test@test.com").get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual emailAddress(
            appConfig,
            formProvider().fill("test@test.com"),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      running(_.overrides(
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val request = FakeRequest().withFormUrlEncodedBody(
            ("email", "test@test.com")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.verify(FakeIdentifier, "test@test.com")
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[EmailFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual emailAddress(
            appConfig,
            formProvider().bind(Map.empty[String, String]),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }
}

object EmailAddressControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]
  val companyName = "test company name"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  formProvider: EmailFormProvider
                                ) extends EmailAddressController {

    def onPageLoad(viewmodel: EmailAddressViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(), viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))
    }

    def onSubmit(viewmodel: EmailAddressViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, CheckUpdateMode, formProvider(), viewmodel)(DataRequest(fakeRequest, "cacheId", answers, PsaId("A0000000")))
    }
  }
}





