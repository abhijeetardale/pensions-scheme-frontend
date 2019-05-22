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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.establishers.partnership._
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.{CountryOptions, Navigator}
import utils.annotations.EstablisherPartnership
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel

class PartnershipConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                            val messagesApi: MessagesApi,
                                                            val dataCacheConnector: UserAnswersCacheConnector,
                                                            @EstablisherPartnership val navigator: Navigator,
                                                            authenticate: AuthAction,
                                                            allowAccess: AllowAccessActionProvider,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            val countryOptions: CountryOptions
                                                      ) extends ConfirmPreviousAddressController with I18nSupport with Retrievals {

  private[controllers] val postCall = routes.PartnershipConfirmPreviousAddressController.onSubmit _
  private[controllers] val pageTitle: Message = "confirmPreviousAddress.title"
  private[controllers] val heading: Message = "confirmPreviousAddress.heading"

  private def viewmodel(srn: Option[String], index: Int) =
    Retrieval(
      implicit request =>
        (PartnershipDetailsId(index) and ExistingCurrentAddressId(index)).retrieve.right.map {
          case details ~ address =>
            ConfirmAddressViewModel(
              routes.PartnershipConfirmPreviousAddressController.onSubmit(index, srn),
              Message(pageTitle),
              Message(heading, details.name),
              None,
              address.toTolerantAddress,
              details.name,
              srn
            )
        }
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(srn, index).retrieve.right.map { vm =>
        get(PartnershipConfirmPreviousAddressId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(srn, index).retrieve.right.map { vm =>
        post(PartnershipConfirmPreviousAddressId(index), PartnershipPreviousAddressId(index), vm, mode)
      }
  }
}

