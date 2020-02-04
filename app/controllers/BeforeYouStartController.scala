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

package controllers

import config.FrontendAppConfig
import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.actions._
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.beforeYouStart

import scala.concurrent.{ExecutionContext, Future}

class BeforeYouStartController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         crypto: ApplicationCrypto,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         pensionAdministratorConnector: PensionAdministratorConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: businessType
                                      )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate.async {
    implicit request =>
      pensionAdministratorConnector.getPSAName.flatMap { psaName =>
        Future.successful(Ok(beforeYouStart(appConfig, psaName)))
      }
  }
}
