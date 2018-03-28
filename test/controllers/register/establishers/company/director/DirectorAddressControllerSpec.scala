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

package controllers.register.establishers.company.director

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import controllers.register.establishers.company.director.routes._
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorAddressId, DirectorDetailsId}
import models.address.Address
import models.register.establishers.company.director.DirectorDetails
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{CountryOptions, FakeNavigator, InputOption, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  val establisherIndex = Index(0)
  val directorIndex = Index(0)

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear

  val date = new LocalDate(year, month, day)
  val director = DirectorDetails("first", Some("middle"), "last", LocalDate.now())

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
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
    )
  )

  "DirectorAddress Controller" must {

    "render manualAddress from GET request" in {

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(retrieval),
        bind[CountryOptions].to(countryOptions)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[DirectorAddressController]

          val viewmodel = ManualAddressViewModel(
            controller.postCall(NormalMode, establisherIndex, directorIndex),
            countryOptions.options,
            Message(controller.title),
            Message(controller.heading),
            secondaryHeader = Some(director.directorName),
            Message(controller.hint)
          )

          def viewAsString(form: Form[_] = form): String = manualAddress(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

          val request = addToken(
            FakeRequest(DirectorAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex))
              .withHeaders("Csrf-Token" -> "nocheck")
          )

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual manualAddress(
            frontendAppConfig,
            form,
            viewmodel
          )(request, messages).toString

      }

    }

    "redirect to next page on POST request" which {
      "save address" in {

        val onwardCall = Call("GET", "/")

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postCode = Some("AB1 1AB"),
          country = "GB"
        )

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
          bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardCall)),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].to(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressFormProvider].to(formProvider)
        )) {
          implicit app =>

            val fakeRequest = addToken(FakeRequest(DirectorAddressController.onSubmit(NormalMode, establisherIndex, directorIndex))
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postCode.get),
                "country" -> address.country))

            val result = route(app, fakeRequest).value

            status(result) must be(SEE_OTHER)
            redirectLocation(result).value mustEqual onwardCall.url

            FakeDataCacheConnector.verify(DirectorAddressId(establisherIndex, directorIndex), address)
        }
      }
    }

  }
}