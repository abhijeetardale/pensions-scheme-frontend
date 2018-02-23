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

package controllers.register.establishers.company

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.individual.PostCodeLookupFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPostCodeLookupId}
import models.addresslookup.Address
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, Result}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.companyPostCodeLookup

import scala.concurrent.Future

class CompanyPostCodeLookupController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          dataCacheConnector: DataCacheConnector,
                                          addressLookupConnector: AddressLookupConnector,
                                          navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: PostCodeLookupFormProvider
                                        ) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          Future.successful(Ok(companyPostCodeLookup(appConfig, form, mode, index, companyName)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(companyPostCodeLookup(appConfig, formWithErrors, mode, index, companyName))),
            (value) =>
              addressLookupConnector.addressLookupByPostCode(value).flatMap {
                case None =>
                  Future.successful(BadRequest(companyPostCodeLookup(appConfig, formWithError("invalid"), mode, index, companyName)))

                case Some(Nil) =>
                  Future.successful(BadRequest(companyPostCodeLookup(appConfig, formWithError("no_results"), mode, index, companyName)))

                case Some(addressSeq) =>
                  dataCacheConnector.save(
                    request.externalId,
                    CompanyPostCodeLookupId(index),
                    addressSeq.map(_.address)
                  ).map {
                    json =>
                      Redirect(navigator.nextPage(CompanyPostCodeLookupId(index), mode)(new UserAnswers(json)))
                  }
              }
          )
      }
  }

  private def retrieveCompanyName(index: Int)(block: String => Future[Result])
                                 (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(CompanyDetailsId(index)) match {
      case Some(value) =>
        block(value.companyName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}