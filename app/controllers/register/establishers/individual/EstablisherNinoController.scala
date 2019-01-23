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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.individual.EstablisherNinoFormProvider
import identifiers.register.establishers.individual.EstablisherNinoId
import javax.inject.Inject
import models.{Index, Mode, Nino}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.individual.establisherNino

import scala.concurrent.{ExecutionContext, Future}

class EstablisherNinoController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           @EstablishersIndividual navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EstablisherNinoFormProvider
                                         ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[Nino] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val redirectResult = request.userAnswers.get(EstablisherNinoId(index)) match {
            case None =>
              Ok(establisherNino(appConfig, form, mode, index, existingSchemeName))
            case Some(value) =>
              Ok(establisherNino(appConfig, form.fill(value), mode, index, existingSchemeName))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(establisherNino(appConfig, formWithErrors, mode, index, existingSchemeName))),
            (value) =>
              dataCacheConnector.save(
                request.externalId,
                EstablisherNinoId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(EstablisherNinoId(index), mode, new UserAnswers(json)))
              }
          )
      }
  }

}
