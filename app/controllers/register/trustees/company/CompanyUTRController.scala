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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyUTRId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.TrusteesCompany
import viewmodels.{Message, UTRViewModel}

import scala.concurrent.ExecutionContext

class CompanyUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     override val userAnswersService: UserAnswersService,
                                     @TrusteesCompany override val navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     allowAccess: AllowAccessActionProvider,
                                     requireData: DataRequiredAction,
                                     formProvider: UTRFormProvider
                                    )(implicit val ec: ExecutionContext) extends UTRController {

  private def form: Form[String] = formProvider()

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): UTRViewModel = {
    UTRViewModel(
      postCall = routes.CompanyUTRController.onSubmit(mode, srn, index),
      title = Message("messages__companyUtr__title"),
      heading = Message("messages__companyUtr__heading", companyName),
      hint = Message("messages__companyUtr__hint"),
      subHeading = None,
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          get(CompanyUTRId(index), viewModel(mode, index, srn, companyName), form)
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          post(CompanyUTRId(index), mode, viewModel(mode, index, srn, companyName), form)
        }
    }
}
