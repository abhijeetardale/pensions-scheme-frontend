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

package identifiers

import models.register.SchemeType
import models.register.SchemeType.{MasterTrust, SingleTrust}
import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.SchemeTypeCYA
import utils.{CountryOptions, UserAnswers}

object SchemeTypeId extends TypedIdentifier[SchemeType] {
  self =>
  override def toString: String = "schemeType"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages,
                   userAnswers: UserAnswers): CheckYourAnswers[self.type] =
    SchemeTypeCYA[self.type](
      label = Some(messages("schemeType.checkYourAnswersLabel", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(messages("messages__visuallyhidden__schemeType", userAnswers.get(SchemeNameId).getOrElse("")))
    )()

  private val singleOrMasterTrustTypes = Seq(SingleTrust, MasterTrust)

  override def cleanup(value: Option[SchemeType], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(st) if singleOrMasterTrustTypes.contains(st) =>
        userAnswers.remove(HaveAnyTrusteesId)
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
