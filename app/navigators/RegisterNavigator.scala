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

package navigators

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.register._
import identifiers.{IsBeforeYouStartCompleteId, UserResearchDetailsId, VariationDeclarationId}
import models.{NormalMode, UpdateMode}
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                  appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case ContinueRegistrationId =>
        continueRegistration(from.userAnswers)
      case DeclarationDormantId =>
        NavigateTo.dontSave(controllers.register.routes.DeclarationController.onPageLoad())
      case DeclarationId =>
        NavigateTo.dontSave(controllers.register.routes.SchemeSuccessController.onPageLoad())
      case UserResearchDetailsId => NavigateTo.dontSave(appConfig.managePensionsSchemeOverviewUrl)
      case _ => None
    }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case _ => None
    }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = from.id match {
    case VariationDeclarationId => NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode , srn))
    case _ => None
  }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  private def continueRegistration(userAnswers: UserAnswers): Option[NavigateTo] =
    userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) =>
        NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None))
      case _ =>
        NavigateTo.dontSave(controllers.routes.BeforeYouStartController.onPageLoad())
    }
}
