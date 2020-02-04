/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.trustees.individual.routes._
import forms.register.PersonNameFormProvider
import identifiers.register.trustees.individual.TrusteeNameId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.{ExecutionContext, Future}

class TrusteeNameController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      userAnswersService: UserAnswersService,
                                      navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      formProvider: PersonNameFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: businessType
                                      )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider("messages__error__trustees")

  private def viewmodel(mode: Mode, index: Index, srn: Option[String]) = CommonFormWithHintViewModel(
    TrusteeNameController.onSubmit(mode, index, srn),
    Message("messages__trusteeName__title"),
    Message("messages__trusteeName__heading")
  )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val updatedForm = request.userAnswers.get(TrusteeNameId(index)).fold(form)(form.fill)
        Future.successful(Ok(personName(appConfig, updatedForm, viewmodel(mode, index, srn), existingSchemeName)))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(personName(appConfig, formWithErrors, viewmodel(mode, index, srn), existingSchemeName))),
        value =>
          userAnswersService.save(mode, srn, TrusteeNameId(index), value).map { cacheMap =>
            Redirect(navigator.nextPage(TrusteeNameId(index), mode, UserAnswers(cacheMap), srn))
        }
      )
  }
}
