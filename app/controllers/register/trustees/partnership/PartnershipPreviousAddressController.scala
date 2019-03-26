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

package controllers.register.trustees.partnership

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.trustees.partnership._
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.TrusteesPartnership
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PartnershipPreviousAddressController @Inject()(
                                                      val appConfig: FrontendAppConfig,
                                                      val messagesApi: MessagesApi,
                                                      val userAnswersService: UserAnswersService,
                                                      @TrusteesPartnership val navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: AddressFormProvider,
                                                      val countryOptions: CountryOptions,
                                                      val auditService: AuditService
                                                    ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.PartnershipPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__partnershipPreviousAddress__title"
  private[controllers] val heading: Message = "messages__partnershipPreviousAddress__heading"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              secondaryHeader = Some(details.name)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          get(PartnershipPreviousAddressId(index), PartnershipPreviousAddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          val context = s"Trustee Partnership Previous Address: ${vm.secondaryHeader.get}"
          post(PartnershipPreviousAddressId(index), PartnershipPreviousAddressListId(index), vm, mode, context,
            PartnershipPreviousAddressPostcodeLookupId(index)
          )
      }
  }


}
