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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.actions._
import controllers.register.adviser.routes.{AdviserNameController, WorkingKnowledgeController}
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Toggles.enableHubV2
import views.html.whatYouWillNeedWorkingKnowledge

class WhatYouWillNeedWorkingKnowledgeController @Inject()(appConfig: FrontendAppConfig,
                                                          override val messagesApi: MessagesApi,
                                                          authenticate: AuthAction,
                                                          fs: FeatureSwitchManagementService
                                                         ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
      Ok(whatYouWillNeedWorkingKnowledge(appConfig))
  }

  def onSubmit: Action[AnyContent] = authenticate {
    implicit request =>
      Redirect(
        if (fs.get(enableHubV2)) {
          AdviserNameController.onPageLoad(NormalMode)
        } else {
          WorkingKnowledgeController.onPageLoad(NormalMode)
        }
      )
  }
}