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

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.CompanyRegistrationNumberVariationsFormProvider
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.CompanyRegistrationNumberId
import models._
import models.requests.DataRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.register.companyRegistrationNumberVariations

import scala.concurrent.Future

class CompanyRegistrationNumberVariationsControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import CompanyRegistrationNumberVariationsControllerSpec._

  val postCall = routes.CompanyRegistrationNumberController.onSubmit _

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(UserAnswers())
          val postCall = routes.CompanyRegistrationNumberController.onSubmit _

          status(result) mustEqual OK
          contentAsString(result) mustEqual companyRegistrationNumberVariations(
            appConfig,
            formProvider(),
            NormalMode,
            firstIndex,
            None,
            postCall(NormalMode, None, firstIndex),
            None
          )(request, messages).toString

      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(CompanyRegistrationNumberId(firstIndex))(CompanyRegistrationNumber.Yes("123456789")).get
          val result = controller.onPageLoad(answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual companyRegistrationNumberVariations(
            appConfig,
            formProvider().fill("123456789"),
            NormalMode,
            firstIndex,
            None,
            postCall(NormalMode, None, firstIndex),
            None
          )(request, messages).toString

      }
    }

  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      val userAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          when(
            userAnswersService.upsert(any(), any(), any())(any(), any(), any())
          ).thenReturn(Future.successful(Json.obj()))

          val request = FakeRequest().withFormUrlEncodedBody(
            ("companyRegistrationNumber", "12345678")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }
  }

  "return a bad request when the submitted data is invalid" in {

    running(_.overrides(
      bind[Navigator].toInstance(FakeNavigator)
    )) {
      app =>

        implicit val materializer: Materializer = app.materializer

        val appConfig = app.injector.instanceOf[FrontendAppConfig]
        val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
        val controller = app.injector.instanceOf[TestController]
        val request = FakeRequest().withFormUrlEncodedBody(("companyRegistrationNumber", "123456789012345"))

        val messages = app.injector.instanceOf[MessagesApi].preferred(request)

        val result = controller.onSubmit(UserAnswers(), request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual companyRegistrationNumberVariations(
          appConfig,
          formProvider().bind(Map("companyRegistrationNumber" -> "123456789012345")),
          NormalMode,
          firstIndex,
          None,
          postCall(NormalMode, None, firstIndex),
          None
        )(request, messages).toString
    }
  }
}

object CompanyRegistrationNumberVariationsControllerSpec {

  val firstIndex = Index(0)

  object FakeIdentifier extends TypedIdentifier[CompanyRegistrationNumber]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  formProvider: CompanyRegistrationNumberVariationsFormProvider
                                ) extends CompanyRegistrationNumberVariationsController {

    override protected val form: Form[String] = formProvider()

    def onPageLoad(answers: UserAnswers): Future[Result] = {
      get(NormalMode, None, firstIndex)(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))
    }

    def onSubmit(answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(NormalMode, None, firstIndex)(DataRequest(fakeRequest, "cacheId", answers, PsaId("A0000000")))
    }
  }

}
