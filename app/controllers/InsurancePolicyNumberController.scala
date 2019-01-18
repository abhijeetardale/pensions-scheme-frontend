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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.InsurancePolicyNumberFormProvider
import identifiers.{InsuranceCompanyNameId, InsurancePolicyNumberId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.AboutBenefitsAndInsurance
import utils.{IDataFromRequest, Navigator, UserAnswers}
import views.html.insurancePolicyNumber

import scala.concurrent.{ExecutionContext, Future}

class InsurancePolicyNumberController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                dataCacheConnector: UserAnswersCacheConnector,
                                                @AboutBenefitsAndInsurance navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: InsurancePolicyNumberFormProvider
                                              )(implicit val ec: ExecutionContext) extends FrontendController with IDataFromRequest with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      InsuranceCompanyNameId.retrieve.right.map { companyName =>
        val preparedForm = request.userAnswers.get(InsurancePolicyNumberId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(insurancePolicyNumber(appConfig, preparedForm, mode, companyName, existingSchemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          InsuranceCompanyNameId.retrieve.right.map { companyName =>
            Future.successful(BadRequest(insurancePolicyNumber(appConfig, formWithErrors, mode, companyName, existingSchemeName)))
          },
        value =>
          dataCacheConnector.save(request.externalId, InsurancePolicyNumberId, value).map(cacheMap =>
            Redirect(navigator.nextPage(InsurancePolicyNumberId, mode, UserAnswers(cacheMap))))
      )
  }
}
