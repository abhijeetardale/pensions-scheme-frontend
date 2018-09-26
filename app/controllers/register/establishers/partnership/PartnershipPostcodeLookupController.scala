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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablisherPartnership
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class PartnershipPostcodeLookupController @Inject()(
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val cacheConnector: UserAnswersCacheConnector,
                                                     override val addressLookupConnector: AddressLookupConnector,
                                                     @EstablisherPartnership override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PostCodeLookupFormProvider
                                                   ) extends PostcodeLookupController {

  private val title: Message = "messages__partnershipPostcodeLookup__title"
  private val heading: Message = "messages__partnershipPostcodeLookup__heading"
  private val hint: Message = "messages__partnershipPostcodeLookup__hint"

  protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnershipPostcodeLookupController.onSubmit(mode, index),
              routes.PartnershipAddressController.onPageLoad(mode, index),
              title = Message(title),
              heading = Message(heading),
              subHeading = Some(details.name),
              hint = Some(Message(hint))
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right map get
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right.map {
          vm =>
            post(PartnershipPostcodeLookupId(index), vm, mode)
        }
    }
}
