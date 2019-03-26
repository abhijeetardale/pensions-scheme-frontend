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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.company._
import javax.inject.Inject
import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.EstablishersCompany
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            implicit val countryOptions: CountryOptions,
                                            @EstablishersCompany navigator: Navigator,
                                            sectionComplete: SectionComplete
                                          )(implicit val ec: ExecutionContext) extends FrontendController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val companyDetails = AnswerSection(
        Some("messages__common__company_details__title"),
        CompanyDetailsId(index).row(routes.CompanyDetailsController.onPageLoad(CheckMode, srn, index).url) ++
          CompanyRegistrationNumberId(index).row(routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, srn, Index(index)).url) ++
          CompanyUniqueTaxReferenceId(index).row(routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, srn, Index(index)).url) ++
          IsCompanyDormantId(index).row(routes.IsCompanyDormantController.onPageLoad(CheckMode, srn, Index(index)).url)
      )

      val companyContactDetails = AnswerSection(
        Some("messages__establisher_company_contact_details__title"),
        CompanyAddressId(index).row(routes.CompanyAddressController.onPageLoad(CheckMode, srn, Index(index)).url) ++
          CompanyAddressYearsId(index).row(routes.CompanyAddressYearsController.onPageLoad(CheckMode, srn, index).url) ++
          CompanyPreviousAddressId(index).row(routes.CompanyPreviousAddressController.onPageLoad(CheckMode, srn, index).url) ++
          CompanyContactDetailsId(index).row(routes.CompanyContactDetailsController.onPageLoad(CheckMode, srn, index).url)
      )

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(companyDetails, companyContactDetails),
        routes.CheckYourAnswersController.onSubmit(mode, srn, index),
        existingSchemeName))
      )
  }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsCompanyCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(index), NormalMode, request.userAnswers))
      }
  }

}
