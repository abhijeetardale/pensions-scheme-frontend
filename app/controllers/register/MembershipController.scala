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

package controllers.register

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.MembershipFormProvider
import identifiers.register.MembershipId
import models.Mode
import models.register.Membership
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.membership

import scala.concurrent.Future
import play.api.libs.json._
import utils.annotations.Register

class MembershipController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     dataCacheConnector: DataCacheConnector,
                                     @Register navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     formProvider: MembershipFormProvider) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MembershipId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(membership(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(membership(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.save(request.externalId, MembershipId, value).map(cacheMap =>
            Redirect(navigator.nextPage(MembershipId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
