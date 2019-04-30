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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.director.DirectorUniqueTaxReferenceFormProvider
import identifiers.register.establishers.company.director.DirectorUniqueTaxReferenceId
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.director.directorUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class DirectorUniqueTaxReferenceController @Inject()(
                                                      appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      userAnswersService: UserAnswersService,
                                                      @EstablishersCompanyDirector navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      allowAccess: AllowAccessActionProvider,
                                                      requireData: DataRequiredAction,
                                                      formProvider: DirectorUniqueTaxReferenceFormProvider
                                                    )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorUniqueTaxReferenceId(establisherIndex, directorIndex).retrieve.right.map { value =>
          Future.successful(Ok(directorUniqueTaxReference(
            appConfig, form.fill(value), mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn))))
        }.left.map { _ =>
          Future.successful(Ok(directorUniqueTaxReference(
            appConfig, form, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn))))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorUniqueTaxReference(
              appConfig, formWithErrors, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn)))),
          (value) =>
            userAnswersService.save(
              mode,
              srn,
              DirectorUniqueTaxReferenceId(establisherIndex, directorIndex),
              value
            ).map {
              json =>
                Redirect(navigator.nextPage(DirectorUniqueTaxReferenceId(establisherIndex, directorIndex), mode, new UserAnswers(json), srn))
            }
        )
    }

  private def postCall: (Mode, Index, Index, Option[String]) => Call = routes.DirectorUniqueTaxReferenceController.onSubmit _

}
